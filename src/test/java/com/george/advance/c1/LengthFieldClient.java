package com.george.advance.c1;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.Random;

/**
 * <p>
 *     基于长度字段帧的解析
 *     https://www.cnblogs.com/java-chen-hao/p/11571229.html
 * </p>
 *
 * @author George
 * @date 2023.10.28 18:09
 */
@Slf4j
public class LengthFieldClient {
    public static void main(String[] args) {
        NioEventLoopGroup worker = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        ChannelFuture channelFuture = bootstrap.group(worker)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        log.info("initChannel...");
                        ch.pipeline().addLast(new LoggingHandler(LogLevel.DEBUG));
                        ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                            @Override
                            public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                log.info("InboundHandler channelActive...");
                                log.debug("sending...");
                                ByteBuf buffer = ctx.alloc().buffer();
                                // 写入长度字段（长度字段占4字节 + 内容占12字节）
                                int length = "Hello, World".length() + 4;
                                buffer.writeInt(length);
                                // 写入消息体， 12个字节长度
                                buffer.writeBytes("Hello, World".getBytes());
                                ctx.writeAndFlush(buffer);
                                super.channelActive(ctx);
//                                ctx.close();
                            }

                            @Override
                            public void channelInactive(ChannelHandlerContext ctx) throws Exception {
                                log.info("InboundHandler channelInactive...");
                                super.channelInactive(ctx);
                            }

                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                log.info("InboundHandler channelRead...");
                                super.channelRead(ctx, msg);
                            }
                        });
                    }
                }).connect("127.0.0.1", 8080);
        channelFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                Channel channel = future.channel();
                log.info("已连接 channel: {}\n", channel);

                channel.closeFuture().addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        Channel closeChannel = future.channel();
                        log.info("断开连接 channel:{}\n", closeChannel);
                    }
                });
            }
        });
    }
}
