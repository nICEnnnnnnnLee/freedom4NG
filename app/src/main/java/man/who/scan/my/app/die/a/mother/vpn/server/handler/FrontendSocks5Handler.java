package man.who.scan.my.app.die.a.mother.vpn.server.handler;

import java.net.Inet4Address;
import java.net.InetSocketAddress;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelOption;
import man.who.scan.my.app.die.a.mother.Global;
import man.who.scan.my.app.die.a.mother.vpn.util.CommonUtil;

public class FrontendSocks5Handler extends ChannelInboundHandlerAdapter {

	private int steps = 0;
	private InetSocketAddress localAddr;
	public FrontendSocks5Handler() {
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) {
		System.out.println("-- FrontendSocks5Handler: local server has received a connection --");
		ctx.channel().read();
	}

	/**
	 * @param ctx
	 */
	private void connectToProxyServer(ChannelHandlerContext ctx0, String remoteIp, short remotePort) {
		final ChannelHandlerContext ctx = ctx0;
		final Channel inboundChannel = ctx.channel();
		Bootstrap b = new Bootstrap();
		b.group(inboundChannel.eventLoop()).channel(ctx.channel().getClass())
				.handler(new BackendInitializer(inboundChannel, remoteIp, "" + remotePort, ctx))
				.option(ChannelOption.AUTO_READ, false);
		ChannelFuture f = b.connect(Global.vpnConfig.remoteHost, Global.vpnConfig.remotePort);
		f.addListener(new ChannelFutureListener() {
			@Override
			public void operationComplete(ChannelFuture future) {
				if (future.isSuccess()) {
					ctx.fireUserEventTriggered(future.channel());
//					inboundChannel.read();
				} else {
					inboundChannel.close();
				}
			}
		});
	}

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		if (evt instanceof InetSocketAddress) {
			System.out.println("-- FrontendSocks5Handler: userEventTriggered --");
			localAddr = (InetSocketAddress) evt;
			try {
				ByteBuf newMsg = ctx.alloc().buffer(10);
				byte[] reply = {0x05, 0x00, 0x00, 0x01};
				Inet4Address addr = (Inet4Address) localAddr.getAddress();
				byte[] ipBytes = addr.getAddress();
				newMsg.writeBytes(reply);
				newMsg.writeBytes(ipBytes);
				newMsg.writeShort((short)localAddr.getPort());
				ctx.channel().writeAndFlush(newMsg).await();
				
				ctx.pipeline().remove(this);
				
				ctx.channel().read();
			} catch (InterruptedException e) {
				e.printStackTrace();
				ctx.close();
			}
		} else {
			ctx.fireUserEventTriggered(evt);
		}
	}

	@Override
	public void channelRead(final ChannelHandlerContext ctx, Object msg) {
//		System.out.println("-- FrontendSocks5Handler: local server channelRead --");
//		System.out.println("steps: " + steps);
		Channel channel = ctx.channel();
		ByteBuf data = (ByteBuf) msg;
//		System.out.println("data size: " + data.readableBytes());
		switch (steps) {
		case 0:
			if(data.readByte() == 0x05 && data.readByte() == 0x01 && data.readByte() == 0x00) {
//				System.out.println("socks5 header received");
				steps = 1;
				byte[] content = {0x05, 0x00};
				ByteBuf newMsg = ctx.alloc().buffer(content.length);
				newMsg.writeBytes(content);
				try {
					channel.writeAndFlush(newMsg).await();
					channel.read();
				} catch (InterruptedException e) {
					ctx.close();
				}
			}else {
				System.out.println("socks5 header not correct");
				ctx.close();
			}
			break;
		case 1:
			steps = 2;
			byte[] buffer = new byte[4];
			data.readBytes(buffer);
//			System.out.println("mode: " + buffer[1]);
//			System.out.println("addrtype: " + buffer[3]);
			if(buffer[1] == 1) {
				String remoteIp = null;
				if(buffer[3] == 1) {
					data.readBytes(buffer);
					remoteIp = CommonUtil.ipBytesToString(buffer);
				}else if(buffer[3] == 3){
					int addrLen = data.readableBytes();
					byte[] domain = new byte[addrLen];
					data.readBytes(domain);
					remoteIp = new String(domain);
				}else {
					ctx.close();
					break;
				}
				data.readBytes(buffer,0, 2);
				short remotePort = CommonUtil.readShort(buffer, 0);
//				System.out.println(remoteIp + ": " + remotePort);
				connectToProxyServer(ctx, remoteIp, remotePort);
				break;
			}
		default:
			ctx.close();
			break;
		}
		
	}

}