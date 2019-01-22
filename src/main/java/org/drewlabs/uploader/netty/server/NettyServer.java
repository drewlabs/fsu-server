package org.drewlabs.uploader.netty.server;

import org.drewlabs.uploader.netty.server.handler.WriteBytesHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;


public class NettyServer {
    private static final String IP_ADDRESS = "localhost";
    public static final int port = 9999;

    public static void main(String args[]) throws InterruptedException, IOException, ClassNotFoundException {
        SocketServer socServer = new SocketServer();
        System.out.println("Starting SocketServer for metadata receiving");
        Thread tSocketServer = new Thread(() -> {
            Class clazz;
            try{
                clazz = Class.forName(SocketServer.class.getName());
                Method main1 = clazz.getMethod("main", String[].class);
                String[] parameters = {null};
                main1.invoke(null, parameters);
            } catch(Exception e){
                e.printStackTrace();
            }
        });
        tSocketServer.start();
        System.out.println("Started SocketServer for metadata receiving");
        EventLoopGroup group = new NioEventLoopGroup();
        try{
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(group);
            serverBootstrap.channel(NioServerSocketChannel.class);
            serverBootstrap.localAddress(new InetSocketAddress(IP_ADDRESS, port));

            serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
                protected void initChannel(SocketChannel socketChannel) throws Exception {
                    socketChannel.pipeline().addLast(new WriteBytesHandler());
                }
            });
            ChannelFuture channelFuture = serverBootstrap.bind().sync();
            System.out.println("Starting Netty server on IP address " + IP_ADDRESS + " at port number " + port);
            channelFuture.channel().closeFuture().sync();
        } catch(Exception e){
            e.printStackTrace();
        } finally {
            group.shutdownGracefully().sync();
        }
    }
}
