package man.who.scan.my.app.die.a.mother.vpn.server.handler;

import com.google.protobuf.ByteString;

import java.io.IOException;

import io.grpc.stub.StreamObserver;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import man.who.scan.my.app.die.a.mother.vpn.grpc.Stream;

public class FrontendPipGrpcHandler extends ChannelInboundHandlerAdapter {

    final private StreamObserver<Stream.FreedomRequest> requestStreamObserver;
    final private GrpcBackendClient backendClient;

    public FrontendPipGrpcHandler(GrpcBackendClient backendClient, StreamObserver<Stream.FreedomRequest> requestStreamObserver) {
        this.requestStreamObserver = requestStreamObserver;
        this.backendClient = backendClient;
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, Object msg) {
        ByteBuf buf = (ByteBuf) msg;
        ByteString bytes = ByteString.copyFrom(buf.nioBuffer());
//        System.out.println("FrontendPipGrpcHandler write: " + bytes.toStringUtf8());
        requestStreamObserver.onNext(Stream.FreedomRequest.newBuilder().setData(bytes).build());
        ctx.channel().read();
        buf.release();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
//		System.err.println("FrontendPipGrpcHandler channelInactive");
        requestStreamObserver.onCompleted();
        backendClient.shutdown();
        closeOnFlush(ctx.channel());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if (!(cause instanceof IOException))
            cause.printStackTrace();
        channelInactive(ctx);
    }

    static void closeOnFlush(Channel ch) {
        if (ch.isActive()) {
            ch.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        }
    }
}