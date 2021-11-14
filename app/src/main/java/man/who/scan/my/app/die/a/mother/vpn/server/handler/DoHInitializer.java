package man.who.scan.my.app.die.a.mother.vpn.server.handler;

import java.net.DatagramSocket;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLParameters;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpContentDecompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.ssl.SslHandler;
import man.who.scan.my.app.die.a.mother.Global;
import man.who.scan.my.app.die.a.mother.vpn.util.TrustAllSSLUtil;

public class DoHInitializer extends ChannelInitializer<SocketChannel> {

    final DatagramSocket replySocket;
    final int replyPort;
    final String dohDomain;

    public DoHInitializer(String dohDomain, DatagramSocket replySocket, int replyPort) {
        this.replySocket = replySocket;
        this.replyPort = replyPort;
        this.dohDomain = dohDomain;
    }

    @Override
    public void initChannel(SocketChannel ch) {
        ChannelPipeline p = ch.pipeline();

        // Enable HTTPS if necessary.
//		if (sslCtx != null) {
//			p.addLast(sslCtx.newHandler(ch.alloc()));
//		}
        SSLContext context = TrustAllSSLUtil.getSSLContext(Global.vpnConfig.verifySSL);
        SSLEngine engine = context.createSSLEngine(dohDomain, 443);
        engine.setUseClientMode(true);
        engine.setNeedClientAuth(false);
        if(Global.vpnConfig.verifySSL){
            // 域名校验
            // https://netty.io/4.1/api/io/netty/handler/ssl/SslContext.html#newHandler-io.netty.buffer.ByteBufAllocator-java.util.concurrent.Executor-
            SSLParameters sslParameters = engine.getSSLParameters();
            // only available since Java 7
            sslParameters.setEndpointIdentificationAlgorithm("HTTPS");
            engine.setSSLParameters(sslParameters);
        }
        p.addLast("ssl", new SslHandler(engine));
        p.addLast(new HttpClientCodec());
        p.addLast(new HttpContentDecompressor());
        p.addLast(new HttpObjectAggregator(1024));
        p.addLast(new DoHHandler(replySocket, replyPort));
    }
}