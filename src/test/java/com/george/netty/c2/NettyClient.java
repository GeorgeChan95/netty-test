package com.george.netty.c2;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 *     Netty客户端
 * </p>
 *
 * @author George
 * @date 2023.10.16 16:11
 */
@Slf4j
public class NettyClient {
    public static void main(String[] args) throws InterruptedException {
        // netty客户端启动类 Bootstrap
        new Bootstrap()
                // 添加 EventLoop
                .group(new NioEventLoopGroup())
                // 选择客户端Channel的实现
                .channel(NioSocketChannel.class)
                // 添加处理器
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    // 在连接建立后被调用
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        // 添加字节编码器
                        ch.pipeline().addLast(new StringEncoder());
                    }
                })
                // 指定连接到服务端
                .connect("127.0.0.1", 8080)
                // 阻塞等待连接完成
                .sync()
                // 获取已连接的通道
                .channel()
                // 向服务器发送数据
                .writeAndFlush("hello netty");
    }
}
