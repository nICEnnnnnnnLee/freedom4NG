package man.who.scan.my.app.die.a.mother.vpn.server.handler;

import java.net.InetSocketAddress;
import java.net.Socket;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelOption;
import man.who.scan.my.app.die.a.mother.Global;
import man.who.scan.my.app.die.a.mother.vpn.LocalVpnService;
import man.who.scan.my.app.die.a.mother.vpn.server.nat.NATSession;
import man.who.scan.my.app.die.a.mother.vpn.server.nat.NATSessionManager;
import man.who.scan.my.app.die.a.mother.vpn.util.CNIPRecognizer;
import man.who.scan.my.app.die.a.mother.vpn.util.CommonUtil;

public class FrontendPipHandler extends ChannelInboundHandlerAdapter {

    private Channel outboundChannel;

    public FrontendPipHandler() {
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
//        System.out.println("-- local server has received a connection --");
        final Channel inboundChannel = ctx.channel();
        InetSocketAddress insocket = (InetSocketAddress) inboundChannel.remoteAddress();
        NATSession session = NATSessionManager.getSession("tcp", insocket.getPort());
        if (session == null) {
//			System.out.println("当前没有这个连接");
            inboundChannel.close();
            return;
        }

//        System.out.printf("-- remote (%s: %s) SSL: %s--\n", Global.vpnConfig.remoteHost, Global.vpnConfig.remotePort, Global.vpnConfig.useSSL);
        if (Global.vpnConfig.directAll || (Global.vpnConfig.directIfCN && CNIPRecognizer.isCNIP(session.RemoteHost))) {
            Bootstrap b = new Bootstrap();
            b.group(inboundChannel.eventLoop()).channel(ctx.channel().getClass())
                    .handler(new BackendInitializer(inboundChannel))
                    .option(ChannelOption.AUTO_READ, false);
            ChannelFuture f = b.connect(session.RemoteHost, session.RemotePort);
            outboundChannel = f.channel();
            addListner(f, inboundChannel);
        } else {
            Bootstrap b = new Bootstrap();
            b.group(inboundChannel.eventLoop()).channel(ctx.channel().getClass())
                    .handler(new BackendInitializer(inboundChannel, session.RemoteHost, "" + session.RemotePort))
                    .option(ChannelOption.AUTO_READ, false);
            ChannelFuture f = b.connect(Global.vpnConfig.remoteHost, Global.vpnConfig.remotePort);
            outboundChannel = f.channel();
            addListner(f, inboundChannel);
        }
        Socket socket = CommonUtil.socketOf(outboundChannel);
        if (socket != null) {
            LocalVpnService.Instance.protect(socket);
        } else {
            inboundChannel.close();
            return;
        }
//		System.out.println("----尝试连接中");

    }

    private void addListner(ChannelFuture f, Channel in0) {
        final Channel in = in0;
        f.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) {
                if (future.isSuccess()) {
//					System.out.println("----连接成功");
//					inboundChannel.read();
                } else {
                    in.close();
                }
            }
        });
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, Object msg) {
//        System.out.println("-- local server channelRead --");
        if (outboundChannel != null && outboundChannel.isActive()) {
            outboundChannel.writeAndFlush(msg).addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) {
                    if (future.isSuccess()) {
                        ctx.channel().read();
                    } else {
                        future.channel().close();
                    }
                }
            });
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof Channel && outboundChannel == null) {
            outboundChannel = (Channel) evt;
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
//        System.out.println("-- local server channelInactive --");
        if (outboundChannel != null) {
            closeOnFlush(outboundChannel);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        closeOnFlush(ctx.channel());
    }

    /**
     * Closes the specified channel after all queued write requests are flushed.
     */
    static void closeOnFlush(Channel ch) {
//        System.out.println("-- local server closeOnFlush --");
        if (ch.isActive()) {
            ch.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        }
    }
}