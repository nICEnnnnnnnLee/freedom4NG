package man.who.scan.my.app.die.a.mother.vpn.server.handler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
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
import man.who.scan.my.app.die.a.mother.vpn.util.SNIHelper;

public class FrontendPipHandler extends ChannelInboundHandlerAdapter {

    private Channel outboundChannel;
    boolean isFirstRead;
    Object firstMsg;

    public FrontendPipHandler() {
        isFirstRead = true;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        ctx.read();
    }

    private void addListner(ChannelFuture f, Channel in0, boolean isDirect, ChannelHandlerContext ctx) {
        final Channel inboundChannel = in0;
        final boolean isDirectf = isDirect;
        final ChannelHandlerContext ctxf = ctx;
        f.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) {
                if (future.isSuccess()) {
                    outboundChannel = future.channel();
                    if (isDirectf) {
                        try {
                            userEventTriggered(ctxf, "write first Msg");
                        } catch (Exception e) {
                        }
                    }
                } else {
                    inboundChannel.close();
                }
            }
        });
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, Object msg) {
//        System.out.println("--------------------FrontendPipHandler channelRead");
        if (isFirstRead) {
            final Channel inboundChannel = ctx.channel();
            ByteBuf buf = (ByteBuf) msg;
            String sni = SNIHelper.getSNIOrNull(buf.array(), buf.readableBytes());
            System.out.println("----------sni:" + sni);

            InetSocketAddress insocket = (InetSocketAddress) inboundChannel.remoteAddress();
            NATSession session = NATSessionManager.getSession("tcp", insocket.getPort());
            if (session == null) {
                inboundChannel.close();
                return;
            }

//        System.out.printf("-- remote (%s: %s) SSL: %s--\n", Global.vpnConfig.remoteHost, Global.vpnConfig.remotePort, Global.vpnConfig.useSSL);
            ChannelFuture f = null;
            if (Global.vpnConfig.directAll || (Global.vpnConfig.directIfCN && CNIPRecognizer.isCNIP(session.RemoteHost))) {
                Bootstrap b = new Bootstrap();
                b.group(inboundChannel.eventLoop()).channel(ctx.channel().getClass())
                        .handler(new BackendInitializer(inboundChannel))
                        .option(ChannelOption.AUTO_READ, false);
                f = b.connect(session.RemoteHost, session.RemotePort);
                addListner(f, inboundChannel, true, ctx);
            } else {
                Bootstrap b = new Bootstrap();
                b.group(inboundChannel.eventLoop()).channel(ctx.channel().getClass())
                        .handler(new BackendInitializer(inboundChannel, session.RemoteHost, "" + session.RemotePort, ctx))
                        .option(ChannelOption.AUTO_READ, false);
                f = b.connect(Global.vpnConfig.remoteHost, Global.vpnConfig.remotePort);
                addListner(f, inboundChannel, false, ctx);
            }
            Socket socket = CommonUtil.socketOf(f.channel());
            if (socket != null) {
                LocalVpnService.Instance.protect(socket);
            } else {
                inboundChannel.close();
                return;
            }
            firstMsg = msg;
            isFirstRead = false;
        } else {
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
            } else {
                ByteBuf buf = (ByteBuf) msg;
                System.err.println("-- 收到了不该收到的数据 --" + buf.readableBytes());
                buf.release();
            }
        }


//        System.out.println("-- local server channelRead --");
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (firstMsg != null) {
            final Channel inboundChannel = ctx.channel();
            outboundChannel.writeAndFlush(firstMsg).addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) {
                    if (future.isSuccess()) {
                        inboundChannel.read();
                    } else {
                        future.channel().close();
                    }
                    firstMsg = null;
                }
            });

        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
//        System.out.println("-- local server channelInactive --");
        if (outboundChannel != null) {
            closeOnFlush(outboundChannel);
        }
        closeOnFlush(ctx.channel());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if (!(cause instanceof IOException))
            cause.printStackTrace();
        channelInactive(ctx);
    }

    /**
     * Closes the specified channel after all queued write requests are flushed.
     */
    static void closeOnFlush(Channel ch) {
        if (ch.isActive()) {
            ch.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        }
    }
}