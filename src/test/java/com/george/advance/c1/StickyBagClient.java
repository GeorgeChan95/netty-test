package com.george.advance.c1;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.Random;

/**
 * <p>
 * 粘包测试-客户端
 * 客户端分10次向服务端发送160字节的数据，
 * 服务端却仅是一次接收到160字节数据
 * 此为：粘包
 * </p>
 *
 * @author George
 * @date 2023.10.24 12:52
 */
@Slf4j
public class StickyBagClient {
    public static void main(String[] args) {
        NioEventLoopGroup worker = new NioEventLoopGroup();
        try {
            ChannelFuture channelFuture = new Bootstrap()
                    .group(worker)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new LoggingHandler());
                            ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                                @Override
                                public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                    Channel channel = ctx.channel();
                                    log.info("client connected, channel:{}\n", channel);
                                    log.info("向客户端发送消息...");
                                    Random r = new Random();
                                    char c = 'a';
                                    // 客户端分10次向服务端发送160字节的数据，服务端却仅是一次接收到160字节数据
                                    for (int i = 0; i < 10; i++) {
                                        ByteBuf buffer = ctx.alloc().buffer();
                                        buffer.writeBytes(new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15});
                                        ctx.writeAndFlush(buffer);
                                    }
                                    super.channelActive(ctx);
                                    // 发送完消息，断开与服务端的连接
//                                    ctx.close();
                                }

                                @Override
                                public void channelInactive(ChannelHandlerContext ctx) throws Exception {
                                    Channel channel = ctx.channel();
                                    log.info("client disconnect, channel:{}", channel);
                                    super.channelInactive(ctx);
                                }

                                @Override
                                public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                    super.channelRead(ctx, msg);
                                }
                            });
                        }
                    })
                    .connect("127.0.0.1", 8080)
                    .sync();
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            worker.shutdownGracefully();
        }
    }
}
