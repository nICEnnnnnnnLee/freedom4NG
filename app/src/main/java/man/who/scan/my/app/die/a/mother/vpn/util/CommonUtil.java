package man.who.scan.my.app.die.a.mother.vpn.util;

import java.lang.reflect.Method;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.channels.SocketChannel;
import java.security.MessageDigest;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class CommonUtil {

	private static Method method;
	static {
		try {
			method = NioSocketChannel.class.getDeclaredMethod("javaChannel");
			method.setAccessible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public  static Socket socketOf(Channel channel){
		try{
			SocketChannel javaSocketChannel = (SocketChannel) method.invoke(channel);
			return javaSocketChannel.socket();
		}catch (Exception e){
			e.printStackTrace();
			return null;
		}
	}
	public static String MD5(String dataStr) {
		try {
			MessageDigest m = MessageDigest.getInstance("MD5");
			m.update(dataStr.getBytes("UTF8"));
			byte s[] = m.digest();
			StringBuffer result = new StringBuffer();
			for (int i = 0; i < s.length; i++) {
				result.append(Integer.toHexString((0x000000FF & s[i]) | 0xFFFFFF00).substring(6));
			}
			return result.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return "";
	}

	public static String ByteBufToString(ByteBuf buf) {
		String str;
		if (buf.hasArray()) { // 处理堆缓冲区
			str = new String(buf.array(), buf.arrayOffset() + buf.readerIndex(), buf.readableBytes());
		} else { // 处理直接缓冲区以及复合缓冲区
			byte[] bytes = new byte[buf.readableBytes()];
			buf.getBytes(buf.readerIndex(), bytes);
			str = new String(bytes, 0, buf.readableBytes());
		}
		return str;
	}

	public static InetAddress ipIntToInet4Address(int ip) {
		byte[] ipAddress = new byte[4];
		writeInt(ipAddress, 0, ip);
		try {
			return Inet4Address.getByAddress(ipAddress);
		} catch (UnknownHostException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String ipIntToString(int ip) {
		return String.format("%s.%s.%s.%s", (ip >> 24) & 0x00FF, (ip >> 16) & 0x00FF, (ip >> 8) & 0x00FF, ip & 0x00FF);
	}

	public static String ipBytesToString(byte[] ip) {
		return String.format("%s.%s.%s.%s", ip[0] & 0x00FF, ip[1] & 0x00FF, ip[2] & 0x00FF, ip[3] & 0x00FF);
	}

	public static int readInt(byte[] data, int offset) {
		int r = ((data[offset] & 0xFF) << 24) | ((data[offset + 1] & 0xFF) << 16) | ((data[offset + 2] & 0xFF) << 8)
				| (data[offset + 3] & 0xFF);
		return r;
	}

	public static short readShort(byte[] data, int offset) {
		int r = ((data[offset] & 0xFF) << 8) | (data[offset + 1] & 0xFF);
		return (short) r;
	}

	public static void writeInt(byte[] data, int offset, int value) {
		data[offset] = (byte) (value >> 24);
		data[offset + 1] = (byte) (value >> 16);
		data[offset + 2] = (byte) (value >> 8);
		data[offset + 3] = (byte) (value);
	}

	public static void writeShort(byte[] data, int offset, short value) {
		data[offset] = (byte) (value >> 8);
		data[offset + 1] = (byte) (value);
	}

}
