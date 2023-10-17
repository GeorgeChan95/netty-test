package com.george.netty.c3;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringEncoder;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 *
 * </p>
 *
 * @author George
 * @date 2023.10.17 10:18
 */
@Slf4j
public class EventLoopClient {
    public static void main(String[] args) throws InterruptedException {
        // 客户端启动类
        new Bootstrap()
                // 客户端EventLoopGroup
                .group(new NioEventLoopGroup())
                // 客户端Channel的实现
                .channel(NioSocketChannel.class)
                // 添加客户端处理器
                .handler(new ChannelInitializer<NioSocketChannel>() {

                    @Override // initChannel在连接建立后被调用
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        // 添加handler
                        ch.pipeline().addLast(new StringEncoder());
                    }
                })
                // 与server端的连接Ip和端口
                .connect("127.0.0.1", 8080)
                // 阻塞等待客户端的连接
                .sync()
                // 获取与服务端的连接
                .channel()
                // 向服务端发送消息，该消息会经过handler进行编码，转换成ByteBuf
                .writeAndFlush("789");
    }
}
