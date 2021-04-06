package man.who.scan.my.app.die.a.mother.vpn.server.handler;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.LastHttpContent;
import man.who.scan.my.app.die.a.mother.vpn.LocalVpnService;

public class DoHHandler extends SimpleChannelInboundHandler<HttpObject> {

    final DatagramSocket replySocket;
    final int replyPort;

    public DoHHandler(DatagramSocket replySocket, int replyPort) {
        this.replySocket = replySocket;
        this.replyPort = replyPort;
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, HttpObject msg) {
        if (msg instanceof HttpResponse) {
            HttpResponse response = (HttpResponse) msg;
//            System.err.println("STATUS: " + response.status());
//            System.err.println("VERSION: " + response.protocolVersion());
//            System.err.println();
        }
        if (msg instanceof HttpContent) {
            HttpContent content = (HttpContent) msg;
            ByteBuf buf = content.content();
            DatagramPacket packet = null;
            if (buf.hasArray()) {
                packet = new DatagramPacket(buf.array(), buf.arrayOffset() + buf.readerIndex(), buf.readableBytes(),
                        new InetSocketAddress(LocalVpnService.Instance.uniqueIp, replyPort));
            } else {
                byte[] bytes = new byte[buf.readableBytes()];
                buf.getBytes(buf.readerIndex(), bytes);
                packet = new DatagramPacket(bytes, 0, bytes.length,
                        new InetSocketAddress(LocalVpnService.Instance.uniqueIp, replyPort));
            }
            try {
                synchronized (replySocket){
                    replySocket.send(packet);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (content instanceof LastHttpContent) {
                ctx.close();
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}