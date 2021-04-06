package man.who.scan.my.app.die.a.mother.vpn.server;

import android.util.Base64;

import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.nio.ByteBuffer;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpVersion;
import man.who.scan.my.app.die.a.mother.vpn.LocalVpnService;
import man.who.scan.my.app.die.a.mother.vpn.dns.DnsPacket;
import man.who.scan.my.app.die.a.mother.vpn.dns.Question;
import man.who.scan.my.app.die.a.mother.vpn.dns.Resource;
import man.who.scan.my.app.die.a.mother.vpn.ip.CommonMethods;
import man.who.scan.my.app.die.a.mother.vpn.server.handler.DoHInitializer;
import man.who.scan.my.app.die.a.mother.vpn.server.nat.NATSession;
import man.who.scan.my.app.die.a.mother.vpn.util.CommonUtil;

public class DoH {

    final String dohDomain;
    final String dohHost;
    final String preUrl;

    public DoH(String dohDomain, String dohHost, String dohPath) {
        this.dohDomain = dohDomain;
        this.dohHost = dohHost;
        preUrl = String.format("https://%s%s?dns=", dohDomain, dohPath);
    }

    private EventLoopGroup group;

    public void init() {
        group = new NioEventLoopGroup();
    }

    public void stop() {
        group.shutdownGracefully();
    }

    public String test() {
        HttpURLConnection conn = null;
        try {
//            String url = preUrl + "AAIBAAABAAAAAAAAA3d3dwViYWlkdQNjb20AAAEAAQ"; // www.baidu.com
            String url = preUrl + "AAIBAAABAAAAAAAAA3d3dwZnb29nbGUDY29tAAABAAE"; // www.google.com
            URL realUrl = new URL(url);
            conn = (HttpURLConnection) realUrl.openConnection();
            conn.setConnectTimeout(5000);
            conn.setRequestProperty("Accept", "application/dns-message");
            conn.connect();
            if (conn.getResponseCode() != 200) {
                return "HTTP 状态码： " + conn.getResponseCode();
            }
            InputStream in = conn.getInputStream();
            byte[] buffer = new byte[512];
            int len = in.read(buffer);
            DnsPacket dnsPacket = DnsPacket.FromBytes(ByteBuffer.wrap(buffer, 0, len));
            Question question = dnsPacket.Questions[0];
            String domain = question.Domain;
            StringBuilder sb = new StringBuilder();
            for (Resource resource : dnsPacket.Resources) {
                if (resource.Type == 1)
                    sb.append(question.Domain).append(":").append(CommonMethods.ipBytesToString(resource.Data)).append("\n");
                if (resource.Type == 5)
                    sb.append(question.Domain).append(":").append(new String(resource.Data)).append("\n");
            }
            return sb.toString().trim();
        } catch (Exception e) {
            e.printStackTrace();
            return e.toString();
        } finally {
            conn.disconnect();
        }
    }

    public void dealDNSRequest(DatagramSocket replySocket, int replyPort, NATSession session, DatagramPacket dataPacket, byte[] buffer) {
//        DnsPacket dnsPacket = DnsPacket.FromBytes(ByteBuffer.wrap(buffer, 28, dataPacket.getLength()));
//        Question question = dnsPacket.Questions[0];
//        String domain = question.Domain;
        String query = Base64.encodeToString(buffer, 28, dataPacket.getLength(), Base64.NO_PADDING | Base64.URL_SAFE).trim();
        final String url = preUrl + query;
        Bootstrap b = new Bootstrap();
        b.group(group)
                .channel(NioSocketChannel.class)
                .handler(new DoHInitializer(dohDomain, replySocket, replyPort));
        ChannelFuture f = b.connect(dohHost, 443);
        Socket socket = CommonUtil.socketOf(f.channel());
        if (socket != null) {
            LocalVpnService.Instance.protect(socket);
            f.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) {
                    if (future.isSuccess()) {
                        // Prepare the HTTP request.
//                        System.out.println("与DoH的连接建立成功");
                        HttpRequest request = new DefaultFullHttpRequest(
                                HttpVersion.HTTP_1_1, HttpMethod.GET, url, Unpooled.EMPTY_BUFFER);
                        request.headers().set(HttpHeaderNames.HOST, dohDomain);
//                        request.headers().set(HttpHeaderNames.USER_AGENT, "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:86.0) Gecko/20100101 Firefox/86.0");
//                        request.headers().set(HttpHeaderNames.ACCEPT, "*/*");
//                        request.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
//                        System.out.println(request.toString());
                        future.channel().writeAndFlush(request);
                    } else {
//                        System.out.println("与DoH的连接建立失败");
                    }
                }
            });
        }


    }
}
