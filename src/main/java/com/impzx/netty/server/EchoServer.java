package com.impzx.netty.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.InetSocketAddress;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class EchoServer {

	private Log LOG = LogFactory.getLog(EchoServer.class);
	
	private final int port;
	
	public EchoServer(int port) {
		this.port = port;
	}
	
	public void start() throws Exception {
		EventLoopGroup group = new NioEventLoopGroup();
		try {
			ServerBootstrap serverBootstrap = new ServerBootstrap();
			serverBootstrap.group(group)
						   .channel(NioServerSocketChannel.class)
						   .localAddress(new InetSocketAddress(port))
						   .childHandler(new ChannelInitializer<SocketChannel>() {
							   protected void initChannel(SocketChannel channel) throws Exception {
								   channel.pipeline().addLast(new EchoServerHandler());
							   };
						});
			ChannelFuture future = serverBootstrap.bind().sync();
			LOG.info(EchoServer.class.getName() + " started and listen on " + future.channel().localAddress());
			future.channel().closeFuture().sync();
			LOG.info("close it");
		} finally {
			group.shutdownGracefully().sync();
		}
	}
	
	public static void main(String[] args) throws Exception {
		new EchoServer(9999).start();
	}
	
	@Sharable
	private class EchoServerHandler extends ChannelInboundHandlerAdapter{
		
		@Override
		public void channelActive(ChannelHandlerContext ctx) throws Exception {
			LOG.info("channelActive");
		}
		
		@Override
		public void channelRead(ChannelHandlerContext ctx, Object msg)
				throws Exception {
			LOG.info("Server received: " + msg);
			ctx.write(msg);
		}
		
		@Override
		public void channelReadComplete(ChannelHandlerContext ctx)
				throws Exception {
			LOG.info("channelReadComplete");
			ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
		}
		
		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
				throws Exception {
			LOG.error(cause.getMessage(), cause);
			ctx.close();
		}
	}
}
