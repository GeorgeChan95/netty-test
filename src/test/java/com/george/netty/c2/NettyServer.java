package com.george.netty.c2;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

/**
 * <p>
 * Netty服务端
 * </p>
 *
 * @author George
 * @date 2023.10.16 16:11
 */
@Slf4j
public class NettyServer {
    public static void main(String[] args) {
        // 服务端启动类 ServerBootstrap
        new ServerBootstrap()
                // 添加 EventLoop
                .group(new NioEventLoopGroup())
                // 选择 服务器的 ServerSocketChannel 实现
                .channel(NioServerSocketChannel.class)
                // 服务端使用 NioServerSocketChannel
                .childHandler(
                        // channel 代表和客户端进行数据读写的通道 Initializer 初始化，负责添加别的 handler
                        new ChannelInitializer<NioSocketChannel>() {
                            @Override
                            protected void initChannel(NioSocketChannel ch) throws Exception {
                                // netty自带的日志处理handler
                                ch.pipeline().addLast(new LoggingHandler());
                                // 解码器，将ByteBuf转成字符串
                                ch.pipeline().addLast(new StringDecoder());
                                // 自定义 handler
                                ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                                    @Override
                                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                        // 打印接收到的字符串
                                        System.out.println(msg);
                                    }
                                });
                            }
                        })
                // 绑定监听的端口
                .bind(new InetSocketAddress(8080));
    }
}
