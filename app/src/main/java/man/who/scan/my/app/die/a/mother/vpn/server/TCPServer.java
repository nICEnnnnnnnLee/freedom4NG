package man.who.scan.my.app.die.a.mother.vpn.server;

import java.util.logging.Level;
import java.util.logging.Logger;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.logging.JdkLoggerFactory;
import man.who.scan.my.app.die.a.mother.Global;
import man.who.scan.my.app.die.a.mother.vpn.LocalVpnService;
import man.who.scan.my.app.die.a.mother.vpn.server.handler.FrontendInitializer;

public final class TCPServer extends Thread {

    static {
        InternalLoggerFactory.setDefaultFactory(JdkLoggerFactory.INSTANCE);
        Logger.getLogger("io.netty").setLevel(Level.OFF);
    }

    EventLoopGroup bossGroup;
    EventLoopGroup workerGroup;

    public static void main(String[] args) throws Exception {
        new TCPServer().run();
    }

    public void cancel() {
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }

    @Override
    public void run() {

        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            LocalVpnService.Instance.protect(Global.vpnConfig.localPort);
            b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                    .childHandler(new FrontendInitializer())
                    .childOption(ChannelOption.AUTO_READ, false).bind(Global.vpnConfig.localPort).sync().channel().closeFuture()
                    .sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }

    }
}