package man.who.scan.my.app.die.a.mother.vpn.util;

import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SNIHelper {

    private static Pattern pattern;
    public static String getHostOrNull(byte[] data, int dataLen) {
        if (data[0] >= 'A' && data[0] <= 'Z'
                && data[1] >= 'A' && data[1] <= 'Z'
                && data[2] >= 'A' && data[2] <= 'Z'
        ) {
            String http_header = new String(data, 0, dataLen);
            if(http_header.startsWith("CONNECT"))
                return null;
            if(pattern == null)
                pattern = Pattern.compile("Host *: *([^: \r\n]+)", Pattern.CASE_INSENSITIVE);
            Matcher m = pattern.matcher(http_header);
            if(m.find()) {
                return m.group(1);
            }
        }
        return null;
    }

    public static String getSNIOrNull(byte[] data, int dataLen) {
        String result = getSNIOrNull_(data, dataLen);
        if(result != null)
            return result;
        else
            return getHostOrNull(data, dataLen);
    }

    public static String getSNIOrNull_(byte[] data, int dataLen) {
        /**
         struct {
         ContentType type; 			1字节
         ProtocolVersion version; 	2字节
         uint16 length;
         opaque fragment[SSLPlaintext.length];
         } SSLPlaintext;
         */
        if (data[0] != 0x16)
            return null;

        int offset = 3;
        if (offset > dataLen)
            return null;

        int len = read2Int(data, offset);
        offset += 2;
        // System.out.printf("offset: %s, len: %s, dataLen: %s \n", offset, len,
        // dataLen);
        if (offset + len != dataLen)
            return null;
        /**
         struct {
         HandshakeType msg_type; 1字节
         uint24 length; 3字节
         select(HandshakeType) {
         ...
         case client_hello: ClientHello;
         ...
         }
         body;
         } Handshake;
         */
        if (data[offset] != 0x01)
            return null;

        offset += 1;
        // System.out.printf("%s %s %s \n", data[offset], data[offset+1],
        // data[offset+2]);
        len = read3Int(data, offset);
        offset += 3;
        // System.out.printf("offset: %s, len: %s, dataLen: %s \n", offset, len,
        // dataLen);
        if (offset + len != dataLen)
            return null;

        /**
         struct {
         ProtocolVersion client_version; 				2字节
         Random random; 									32字节
         SessionID session_id; opaque type 				skipOpaqueType1
         CipherSuite cipher_suites<2..2^16-1>;			skipOpaqueType2
         CompressionMethod compression_methods<1..2^8-1>;skipOpaqueType1
         Extension client_hello_extension_list<0..2^16-1>;
         } ClientHello;
         */
        offset += 34;
        offset = skipOpaqueType1(data, offset);
        offset = skipOpaqueType2(data, offset);
        offset = skipOpaqueType1(data, offset);
        if (offset > dataLen)
            return null;

        len = read2Int(data, offset);
        offset += 2;
        // System.out.printf("offset: %s, len: %s, dataLen: %s \n", offset, len,
        // dataLen);
        if (offset + len != dataLen)
            return null;

        /**
         struct {
         ExtensionType extension_type;			2字节 0x00 表示为SNI类型
         opaque extension_data<0..2^16-1>;		skipOpaqueType2
         } Extension;
         */
        int exLen = len, exOffsetStart = offset;
        int exType = read2Int(data, offset);
        offset += 2;
        while (exType != 0x00) {
            offset = skipOpaqueType2(data, offset);
            if (offset > dataLen)
                return null;
            exType = read2Int(data, offset);
            offset += 2;
        }
		/*
		struct {
		    ServerName server_name_list<1..2^16-1>
		} ServerNameList; OpaqueType2
		 */
        len = read2Int(data, offset);
        offset += 2;
        // System.out.printf("offset: %s, len: %s, dataLen: %s \n", offset, len,
        // dataLen);
        if (offset + len - exOffsetStart > exLen)
            return null;

        /**
         struct {
         NameType name_type;					2字节
         select (name_type) {
         case host_name: HostName;
         } name;
         } ServerName; OpaqueType2
         */
        len = read2Int(data, offset);
        offset += 2;
        // System.out.printf("offset: %s, len: %s, dataLen: %s \n", offset, len,
        // dataLen);
        if (offset + len - exOffsetStart > exLen)
            return null;
        int nameType = data[offset];
        offset += 1;
        if (nameType != 0x00)
            return null;

        len = read2Int(data, offset);
        offset += 2;
        // System.out.printf("offset: %s, len: %s, dataLen: %s \n", offset, len,
        // dataLen);
        if (offset + len - exOffsetStart > exLen)
            return null;
        String hostName = new String(data, offset, len, Charset.forName("ascii"));
        return hostName;
    }
    /**
     * 第一个字节为 内容长度 len, 后面为len个字节的内容
     *
     * @param data
     * @param offsetBegin
     * @return
     */
    private static int skipOpaqueType1(byte[] data, int offsetBegin) {

        int len = data[offsetBegin] & 0xff;
        // System.out.printf("skipOpaqueType1-> offset: %s, len: %s, offsetAfter: %s
        // \n", offsetBegin, len, offsetBegin + 1 + len);
        return offsetBegin + 1 + len;
    }

    /**
     * 前两个字节为 内容长度 len, 后面为len个字节的内容
     *
     * @param data
     * @param offsetBegin
     * @return
     */
    private static int skipOpaqueType2(byte[] data, int offsetBegin) {
        int len = (data[offsetBegin] & 0xff) << 8 | (data[offsetBegin + 1] & 0xff);
        // System.out.printf("skipOpaqueType2-> offset: %s, len: %s, offsetAfter: %s
        // \n", offsetBegin, len, offsetBegin + 2 + len);
        return offsetBegin + 2 + len;
    }

    private static int read2Int(byte[] data, int offsetBegin) {
        int number = (data[offsetBegin] & 0xff) << 8 | (data[offsetBegin + 1] & 0xff);
        return number;
    }

    private static int read3Int(byte[] data, int offsetBegin) {
        int number = (data[offsetBegin] & 0xff) << 16 | (data[offsetBegin + 1] & 0xff) << 8
                | (data[offsetBegin + 2] & 0xff);
        return number;
    }

}

