package com.george.netty.c3.future;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringEncoder;
import lombok.extern.slf4j.Slf4j;

import java.util.Scanner;

/**
 * <p></p>
 *
 * @author George
 * @date 2023.10.17 14:29
 */
@Slf4j
public class ClientFutureTest {
    public static void main(String[] args) {
        NioEventLoopGroup group = new NioEventLoopGroup();
        ChannelFuture connectFuture = new Bootstrap()
                .group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new StringEncoder());
                    }
                })
                .connect("127.0.0.1", 8080);
        Channel channel = connectFuture.channel();
        log.info("连接到服务端前的Channel：{}", channel); // 连接到服务端前的Channel：[id: 0x28adb811]
        // 监听连接后的操作
        connectFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                Channel channel = future.channel();
                log.info("连接到服务端后的Channel：{}", channel); // 连接到服务端后的Channel：[id: 0x28adb811, L:/127.0.0.1:65413 - R:/127.0.0.1:8080]
                // 连接建立后调用键盘输入
                inputKey(channel);

                //监听关闭之后的操作
                ChannelFuture closeFuture = channel.closeFuture();
                closeFuture.addListener((ChannelFutureListener) future1 -> {
                    log.info("处理关闭之后的操作");
                    // 优雅的关闭
                    group.shutdownGracefully();
                });
            }
        });
    }

    private static void inputKey(Channel channel) {
        new Thread(()->{
            Scanner scanner = new Scanner(System.in);
            while (true) {
                String line = scanner.nextLine();
                if ("q".equals(line)) {
                    channel.close();// close 异步操作 1s 之后
                    break;
                }
                channel.writeAndFlush(line);
            }
        }, "input").start();
    }
}
