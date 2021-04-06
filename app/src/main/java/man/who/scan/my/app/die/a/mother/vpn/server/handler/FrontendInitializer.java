package man.who.scan.my.app.die.a.mother.vpn.server.handler;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class FrontendInitializer extends ChannelInitializer<SocketChannel> {

	public FrontendInitializer() {
	}

	@Override
	public void initChannel(SocketChannel ch) {
		ChannelPipeline pipeline = ch.pipeline();
//		pipeline.addLast("log", new LoggingHandler(LogLevel.ERROR));
//		pipeline.addLast("socks5", new FrontendSocks5Handler());
		pipeline.addLast("pip", new FrontendPipHandler());
	}
}