package man.who.scan.my.app.die.a.mother.vpn.server.handler;

import com.google.protobuf.ByteString;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import io.grpc.stub.StreamObserver;
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
import man.who.scan.my.app.die.a.mother.vpn.grpc.Stream;
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

    private void addListener(ChannelFuture f, Channel in0, boolean isDirect, ChannelHandlerContext ctx) {
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
            InetSocketAddress insocket = (InetSocketAddress) inboundChannel.remoteAddress();
            NATSession session = NATSessionManager.getSession("tcp", insocket.getPort());
            String sni = null;
            if (Global.vpnConfig.detectSNI || Global.vpnConfig.useGeoDomain) { // GeoDomain 必须要使用流量探测
                ByteBuf buf = (ByteBuf) msg;
                sni = SNIHelper.getSNIOrNull(buf.array(), buf.readableBytes());
            }
            if (session == null) {
                inboundChannel.close();
                return;
            }

            Boolean isDirectResult = null;
            if (Global.vpnConfig.directAll) {
                isDirectResult = true;
            }
            // 使用流量探测 + PAC分析
            if (isDirectResult == null && Global.vpnConfig.useGeoDomain && LocalVpnService.Instance.geoDomain != null) {
                if (sni != null) {
                    Global.hostTableRuntime.putIfAbsent(sni, session.RemoteHost);
                    isDirectResult = LocalVpnService.Instance.geoDomain.isDirect(sni);
//                    System.out.printf("sni: %s, direct: %s\n", sni, isDirectResult);
                } else {
                    isDirectResult = LocalVpnService.Instance.geoDomain.isDirect(session.RemoteHost);
                }
            }
            // 使用GeoIP
            if (isDirectResult == null) {
                isDirectResult = Global.vpnConfig.directIfCN && CNIPRecognizer.isCNIP(session.RemoteHost);
            }

            if (!isDirectResult && Global.vpnConfig.proxyMode.equals("grpc")) {
                // 走GRPC
                GrpcBackendClient backend = null;
                try {
                    String remoteHost = Global.vpnConfig.transportSNI2Remote && sni != null ? sni : session.RemoteHost;
                    backend = new GrpcBackendClient(inboundChannel, remoteHost, "" + session.RemotePort);
                    StreamObserver<Stream.FreedomRequest> requestStreamObserver = backend.connect();
                    //backend.setupHandlers(backend, requestStreamObserver);
                    ByteBuf buf = (ByteBuf) msg;
                    ByteString bytes = ByteString.copyFrom(buf.nioBuffer());
                    requestStreamObserver.onNext(Stream.FreedomRequest.newBuilder().setData(bytes).build());
//                    System.out.println("FrontendPipHandler Grpc write: " + bytes.toStringUtf8());
                    buf.release();
                    ctx.pipeline().remove(FrontendPipHandler.class);
                    ctx.pipeline().addLast("pip", new FrontendPipGrpcHandler(backend, requestStreamObserver));
                    ctx.read();
                } catch (Exception e) {
                    e.printStackTrace();
                    if (backend != null)
                        backend.shutdown();
                    inboundChannel.close();
                    return;
                }
            } else {
                // 直连，或者 走WebSocket
                ChannelFuture f = null;
                if (isDirectResult) {
                    Bootstrap b = new Bootstrap();
                    b.group(inboundChannel.eventLoop()).channel(ctx.channel().getClass())
                            .handler(new BackendInitializer(inboundChannel))
                            .option(ChannelOption.AUTO_READ, false);
                    f = b.connect(session.RemoteHost, session.RemotePort);
                    addListener(f, inboundChannel, true, ctx);
                } else {
                    String remoteHost = Global.vpnConfig.transportSNI2Remote && sni != null ? sni : session.RemoteHost;
                    Bootstrap b = new Bootstrap();
                    b.group(inboundChannel.eventLoop()).channel(ctx.channel().getClass())
                            .handler(new BackendInitializer(inboundChannel, remoteHost, "" + session.RemotePort, ctx))
                            .option(ChannelOption.AUTO_READ, false);
                    f = b.connect(Global.vpnConfig.remoteHost, Global.vpnConfig.remotePort);
                    addListener(f, inboundChannel, false, ctx);
                }
                Socket socket = CommonUtil.socketOf(f.channel());
                if (socket != null) {
                    LocalVpnService.Instance.protect(socket);
                } else {
                    inboundChannel.close();
                    return;
                }
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