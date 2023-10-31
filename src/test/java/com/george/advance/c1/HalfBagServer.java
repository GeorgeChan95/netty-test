package com.george.advance.c1;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.SocketAddress;

/**
 * <p>
 *     测试半包现象
 *     客户端分1次向服务端发送160字节的数据，服务端却分两次接收到了消息，一次是20字节，一次是140字节
 * </p>
 *
 * @author George
 * @date 2023.10.24 12:28
 */
@Slf4j
public class HalfBagServer {
    public static void main(String[] args) {
        NioEventLoopGroup boss = new NioEventLoopGroup();
        NioEventLoopGroup worker = new NioEventLoopGroup();

        ServerBootstrap serverBootstrap = new ServerBootstrap();
        // 设置服务端接收数据缓冲区大小
        serverBootstrap.option(ChannelOption.SO_RCVBUF, 10);
        ChannelFuture channelFuture = serverBootstrap
                .group(boss, worker)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new LoggingHandler());
                        ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                            /**
                             * 连接建立时
                             * @param ctx
                             * @throws Exception
                             */
                            @Override
                            public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                log.info("connected...");
                                Channel channel = ctx.channel();
                                SocketAddress socketAddress = channel.remoteAddress();
                                log.info("当前连接, channel:{}\t address:{}\n", channel, socketAddress);
                                super.channelActive(ctx);
                            }

                            /**
                             * 连接断开时
                             * @param ctx
                             * @throws Exception
                             */
                            @Override
                            public void channelInactive(ChannelHandlerContext ctx) throws Exception {
                                log.info("disconnect...");
                                super.channelInactive(ctx);
                            }
                        });
                    }
                })
                .bind(8080);
        try {
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
            log.info("server stoped ......");
        }
    }
}
