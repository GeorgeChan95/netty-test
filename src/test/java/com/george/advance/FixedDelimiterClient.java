package com.george.advance;

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
 *     固定分隔符切分消息
 * </p>
 *
 * @author George
 * @date 2023.10.28 15:41
 */
@Slf4j
public class FixedDelimiterClient {
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
                                // 发送内容随机的数据包
                                Random r = new Random();
                                char c = 'a';
                                ByteBuf buffer = ctx.alloc().buffer();
                                for (int i = 0; i < 10; i++) {
                                    for (int j = 0; j < r.nextInt(8)+1; j++) {
                                        buffer.writeByte((byte)c);
                                    }
                                    buffer.writeByte((byte)'@');
//                                    buffer.writeByte((byte)'\n');
//                                    buffer.writeByte((byte)'\t');
                                    c++;
                                }
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
