package man.who.scan.my.app.die.a.mother.vpn.server.handler;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.ssl.SslHandler;
import man.who.scan.my.app.die.a.mother.Global;
import man.who.scan.my.app.die.a.mother.vpn.util.TrustAllSSLUitl;
//import man.who.scan.my.app.die.a.mother.vpn.util.TrustAllSSLUitl;

public class BackendInitializer extends ChannelInitializer<SocketChannel> {
//	final static

    final Channel inboundChannel;
    final String host;
    final String port;
    final ChannelHandlerContext ctxOfFrontHandler;

    public BackendInitializer(Channel inboundChannel) {
        this(inboundChannel, null, null, null);
    }

    public BackendInitializer(Channel inboundChannel, String host, String port) {
        this(inboundChannel, host, port, null);
    }

    public BackendInitializer(Channel inboundChannel, String host, String port, ChannelHandlerContext ctx) {
        this.inboundChannel = inboundChannel;
        this.host = host;
        this.port = port;
        this.ctxOfFrontHandler = ctx;
    }

    @Override
    public void initChannel(SocketChannel ch) {
        ChannelPipeline pipeline = ch.pipeline();
//		pipeline.addLast("log", new LoggingHandler(LogLevel.ERROR));
        if (host != null) {
            if (Global.vpnConfig.useSSL) {
                SSLContext context = TrustAllSSLUitl.getSSLContext(Global.vpnConfig.verifySSL);
                SSLEngine engine = context.createSSLEngine(Global.vpnConfig.domain, Global.vpnConfig.remotePort);
                engine.setUseClientMode(true);
                engine.setNeedClientAuth(false);
                ch.pipeline().addLast("ssl", new SslHandler(engine));
            }
//		pipeline.addLast(new HttpClientCodec());
//		pipeline.addLast(new HttpContentDecompressor());
            pipeline.addLast("header", new BackendAuthHandler(host, port, ctxOfFrontHandler));
        }
        pipeline.addLast("pip", new BackendPipHandler(inboundChannel));
    }
}