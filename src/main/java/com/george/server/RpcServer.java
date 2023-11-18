package com.george.server;

import com.george.protocol.MessageCodecSharable;
import com.george.protocol.ProcotolFrameDecoder;
import com.george.server.handler.RpcRequestMessageHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 *     netty rpc通信服务端启动类
 * </p>
 *
 * @author George
 * @date 2023.11.14 17:34
 */
@Slf4j
public class RpcServer {
    public static void main(String[] args) {
        NioEventLoopGroup boss = new NioEventLoopGroup();
        NioEventLoopGroup worker = new NioEventLoopGroup(2);
        LoggingHandler LOGGING_HANDLER = new LoggingHandler();
        MessageCodecSharable CODEC_HANDLER = new MessageCodecSharable();
        RpcRequestMessageHandler RPC_HANDLER = new RpcRequestMessageHandler();
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.channel(NioServerSocketChannel.class);
            serverBootstrap.group(boss, worker);
            serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    // 基于长度帧解码器
                    ch.pipeline().addLast(new ProcotolFrameDecoder());
                    // 日志打印
                    ch.pipeline().addLast(LOGGING_HANDLER);
                    // 自定义消息编解码器
                    ch.pipeline().addLast(CODEC_HANDLER);
                    // RPC请求处理器
                    ch.pipeline().addLast(RPC_HANDLER);
                }
            });
            Channel channel = serverBootstrap.bind(8080).sync().channel();
            System.out.println("服务端启动完成...");
            channel.closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("服务端异常，", e);
        } finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }
}
