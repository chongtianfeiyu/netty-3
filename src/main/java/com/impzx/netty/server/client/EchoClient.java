package com.impzx.netty.server.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.CharsetUtil;

import java.net.InetSocketAddress;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class EchoClient {

	private static final Log LOG = LogFactory.getLog(EchoClient.class);
	private final String host;
	private final int port;
	
	public EchoClient(String host, int port) {
		this.host = host;
		this.port = port;
	}
	
	public void start() throws Exception {
		EventLoopGroup group = new NioEventLoopGroup();
		try {
			Bootstrap boostrap = new Bootstrap();
			
			boostrap.group(group)
					.channel(NioSocketChannel.class)
					.remoteAddress(new InetSocketAddress(host, port))
					.handler(new ChannelInitializer<SocketChannel>() {
						@Override
						protected void initChannel(SocketChannel channel)
								throws Exception {
							channel.pipeline().addLast(new EchoClientHandler());
						};
					});
			ChannelFuture future = boostrap.connect().sync();
			future.channel().closeFuture().sync();
		} finally {
			group.shutdownGracefully().sync();
		}
	}
	
	@Sharable
	private class EchoClientHandler extends SimpleChannelInboundHandler<ByteBuf> {
		@Override
		public void channelActive(ChannelHandlerContext ctx) throws Exception {
			LOG.info("write data to server");
			ctx.write(Unpooled.copiedBuffer("Netty rocks!", CharsetUtil.UTF_8));
			//must add, otherwise client will block
			ctx.flush();
			LOG.info("write close");
		}

		/**
		 * once data received
		 * it not guarantedd that all 5 bytes will be received at once
		 * for 5 bytes, this method called twice,the only grarantee is that the
		 * bytes will be received int the same order as they're send
		 * 
		 */
		@Override
		protected void channelRead0(ChannelHandlerContext arg0, ByteBuf byteBuf)
				throws Exception {
			LOG.info("Client received: " + ByteBufUtil.hexDump(byteBuf.readBytes(byteBuf.readableBytes())));
		}
		
		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
				throws Exception {
			LOG.error(cause.getMessage(), cause);
			ctx.close();
		}
	}
	
	public static void main(String[] args) throws Exception {
		new EchoClient("localhost", 9999).start();
	}
}
