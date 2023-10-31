package com.george.advance.c1;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.FixedLengthFrameDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;

/**
 * <p>
 * 固定长度分隔
 * </p>
 *
 * @author George
 * @date 2023.10.28 09:31
 */
@Slf4j
public class FixedLengthServer {
    public static void main(String[] args) {
        NioEventLoopGroup boss = new NioEventLoopGroup();
        NioEventLoopGroup worker = new NioEventLoopGroup();
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        ChannelFuture channelFuture = serverBootstrap.group(boss, worker)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new LoggingHandler(LogLevel.DEBUG));
                        ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                            /**
                             * 连接成功后调用
                             * @param ctx
                             * @throws Exception
                             */
                            @Override
                            public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                log.info("connected...");
                                super.channelActive(ctx);
                            }

                            /**
                             * 断开连接调用
                             * @param ctx
                             * @throws Exception
                             */
                            @Override
                            public void channelInactive(ChannelHandlerContext ctx) throws Exception {
                                log.info("disconnect...");
                                super.channelInactive(ctx);
                            }

                            /**
                             * 接收到客户端消息调用
                             * @param ctx
                             * @param msg
                             * @throws Exception
                             */
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                log.info("received message...");
                                ByteBuf byteBuf = (ByteBuf) msg;
                                String data = byteBuf.toString(Charset.defaultCharset());
                                log.info("接收到数据：{}\n", data);
                                super.channelRead(ctx, msg);
                            }
                        });
                    }
                }).bind(8080);

        channelFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                Channel channel = future.channel();
                log.info("connected channel:{}\n", channel);
            }
        });
    }
}
