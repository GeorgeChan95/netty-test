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
 *     测试半包现象
 *     客户端分1次向服务端发送160字节的数据，服务端却分两次接收到了消息，一次是20字节，一次是140字节
 * </p>
 *
 * @author George
 * @date 2023.10.25 21:15
 */
@Slf4j
public class HalfBagClient {
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
                                    ByteBuf buffer = ctx.alloc().buffer();
                                    // 客户端一次性向服务端发送160字节的数据，服务端却分两次接收到了消息
                                    for (int i = 0; i < 10; i++) {
                                        buffer.writeBytes(new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15});
                                    }
                                    ctx.writeAndFlush(buffer);
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
