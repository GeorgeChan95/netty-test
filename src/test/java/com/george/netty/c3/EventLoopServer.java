package com.george.netty.c3;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;

/**
 * <p>
 *     测试 netty中的 BossEventLoop和WorkerEventLoop
 *     以及不同阶段的handler绑定不同的EventLoop
 * </p>
 *
 * @author George
 * @date 2023.10.17 09:38
 */
@Slf4j
public class EventLoopServer {
    public static void main(String[] args) {
        EventLoopGroup nioGroup = new NioEventLoopGroup(2);
        EventLoopGroup defaultGroup = new DefaultEventLoopGroup();
        // 服务端启动类
        new ServerBootstrap()
                // 设置EventLoopGroup，前一个为BossGroup，后一个为WorkerGroup
                // bossEventLoop负责处理连接请求，workerEventLoop负责处理读写请求
                .group(new NioEventLoopGroup(), nioGroup)
                // 服务端Channel的实现
                .channel(NioServerSocketChannel.class)
                //
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new LoggingHandler(LogLevel.DEBUG));
                        // handler1绑定默认的EventLoopGroup
                        ch.pipeline().addLast("handler1", new ChannelInboundHandlerAdapter() {
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                ByteBuf byteBuf = (ByteBuf) msg;
                                String msg1 = byteBuf.toString(Charset.forName("UTF-8"));
                                log.info("hander1接受数据：{}\n", msg1);
                                // super.channelRead(ctx, msg); 将消息处理完后传递给下一个handler继续处理
                                // super.channelRead(ctx, msg);
                                // ctx.fireChannelRead(msg); 作用也是将消息向下传递给下一个handler处理
                                ctx.fireChannelRead(msg);
                            }
                        });
                        // handler2绑定自定义EventLoopGroup
                        ch.pipeline().addLast(defaultGroup, "handler2", new ChannelInboundHandlerAdapter() {
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                ByteBuf byteBuf = (ByteBuf) msg;
                                log.info("handler2接受数据：{}\n", byteBuf.toString(Charset.forName("UTF-8")));
                                // 没有后续的handler，消息无需再向下传递
//                                super.channelRead(ctx, msg);
                            }
                        });
                    }
                })
                .bind(8080);
    }
}
