package man.who.scan.my.app.die.a.mother.vpn.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.SSLSocketFactory;

import man.who.scan.my.app.die.a.mother.Global;
import man.who.scan.my.app.die.a.mother.model.VPNConfig;
import man.who.scan.my.app.die.a.mother.vpn.server.handler.BackendAuthHandler;

public class TestUtil {

    static Pattern statusCodePattern;

    public static int testVPNConfig(VPNConfig vpn) throws Exception {
        Socket socket = null;
        try {
            try{
                socket = new Socket();
                socket.setSoTimeout(3000);
                socket.connect(new InetSocketAddress(vpn.remoteHost, vpn.remotePort), 3000);
                if (vpn.useSSL) {
                    SSLSocketFactory factory = TrustAllSSLUtil.getSSLContext(vpn.verifySSL).getSocketFactory();
                    socket = factory.createSocket(socket, vpn.domain, vpn.remotePort, true);
                }
            }catch (Exception e){
                return -1; // 连接出了问题
            }
            InputStream in = socket.getInputStream();
            OutputStream out = socket.getOutputStream();
            try{
                HashMap<String, String> newCookie = new HashMap<>(Global.cookies);
                String currentTime = String.valueOf(System.currentTimeMillis());
                String token = new StringBuilder(vpn.password).append(vpn.salt).append(currentTime)
                        .toString();
                newCookie.put("my_domain", "www.baidu.com");
                newCookie.put("my_port", "443");
                newCookie.put("my_time", currentTime);
                newCookie.put("my_token", CommonUtil.MD5(token));
                newCookie.put("my_type", "1");
                newCookie.put("my_username", vpn.username);
                StringBuilder sb = new StringBuilder();
                sb.append(String.format("GET %s HTTP/%s\r\n", vpn.path, vpn.http_version))
                        .append(String.format("Host: %s:%s\r\n", vpn.domain, vpn.port))
//					.append(String.format("Host: %s\r\n", vpn.domain))
                        .append(String.format("User-Agent: %s\r\n", vpn.userAgent, vpn.http_version))
                        .append("Accept: */*\r\n")
                        .append("Accept-Language: zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2\r\n")
                        .append("Sec-WebSocket-Version: 13\r\n").append("Sec-WebSocket-Extensions: permessage-deflate\r\n")
                        .append(String.format("Origin: https://%s\r\n", vpn.domain))
                        .append("Upgrade: websocket\r\n").append("Connection: keep-alive, Upgrade\r\n")
                        .append("Pragma: no-cache\r\n").append("Cache-Control: no-cache\r\n")
                        .append(String.format("Cookie: %s\r\n", BackendAuthHandler.genCookie(newCookie))).append("Sec-WebSocket-Key: ")
                        .append(CommonUtil.getRandomString(24)).append("\r\n\r\n");
                System.out.println(sb.toString());
                out.write(sb.toString().getBytes(StandardCharsets.UTF_8));

                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                String line = reader.readLine();
                if (statusCodePattern == null) {
                    statusCodePattern = Pattern.compile("^HTTP/[0-9.]{1,3} (\\d+) ");
                }
                Matcher m = statusCodePattern.matcher(line);
                if (m.find()) {
                    return Integer.parseInt(m.group(1));
                } else {
                    return -2; // 返回内容不符合预期
                }
            }catch (Exception e){
                return -3; // 鉴权出了问题
            }

//            while (line != null && !line.isEmpty()) {
//                System.out.println(line);
//                line = reader.readLine();
//            }
        } finally {
            if (socket != null)
                socket.close();
        }
    }
}
