package org.drewlabs.uploader.netty.server;

import org.drewlabs.uploader.netty.server.handler.PingHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.InetSocketAddress;

public class PingServer {
    private static final String IP_ADDRESS = "localhost";
    public static final int port = 10000;
    public static void main(String args[]) throws InterruptedException {
        EventLoopGroup group = new NioEventLoopGroup();
        try{
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(group);
            serverBootstrap.channel(NioServerSocketChannel.class);
            serverBootstrap.localAddress(new InetSocketAddress(IP_ADDRESS, port));

            serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
                protected void initChannel(SocketChannel socketChannel) throws Exception {
                    socketChannel.pipeline().addLast(new PingHandler());
                }
            });
            ChannelFuture channelFuture = serverBootstrap.bind().sync();
            System.out.println("Starting Ping server on IP address " + IP_ADDRESS + " at port number " + port);
            channelFuture.channel().closeFuture().sync();
        } catch(Exception e){
            e.printStackTrace();
        } finally {
            group.shutdownGracefully().sync();
        }
    }
}
