package com.george.advance.c1;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;

/**
 * <p>
 * 测试粘包现象
 * 客户端分10次向服务端发送160字节的数据，服务端却仅是一次接收到160字节数据
 * </p>
 *
 * @author George
 * @date 2023.10.24 12:24
 */
@Slf4j
public class StickyBagServer {
    public static void main(String[] args) {
        NioEventLoopGroup boss = new NioEventLoopGroup();
        NioEventLoopGroup worker = new NioEventLoopGroup();

        ChannelFuture channelFuture = new ServerBootstrap()
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

                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                log.info("channel read...");
                                ByteBuf msgBuf = (ByteBuf) msg;
                                String msgStr = msgBuf.toString(StandardCharsets.UTF_8);
                                log.info("{}", msgStr);
                                super.channelRead(ctx, msg);
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
