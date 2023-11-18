package com.george.client;

import com.george.client.handler.RpcResponseMessageHandler;
import com.george.message.RpcRequestMessage;
import com.george.protocol.MessageCodecSharable;
import com.george.protocol.ProcotolFrameDecoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 *     netty rpc通信客户端启动类
 * </p>
 *
 * @author George
 * @date 2023.11.14 17:47
 */
@Slf4j
public class RpcClient {
    public static void main(String[] args) {
        NioEventLoopGroup worker = new NioEventLoopGroup();
        LoggingHandler LOGGING_HANDLER = new LoggingHandler();
        MessageCodecSharable CODEC_HANDLER = new MessageCodecSharable();
        RpcResponseMessageHandler RPC_HANDLER = new RpcResponseMessageHandler();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.group(worker);
            bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    // 基于长度帧的解码器
                    ch.pipeline().addLast(new ProcotolFrameDecoder());
                    // 消息日志打印
                    ch.pipeline().addLast(LOGGING_HANDLER);
                    // 自定义编解码器
                    ch.pipeline().addLast(CODEC_HANDLER);
                    // RPC响应处理器
                    ch.pipeline().addLast(RPC_HANDLER);
                }
            });
            Channel channel = bootstrap.connect("127.0.0.1", 8080).sync().channel();
            System.out.println("RpcClient客户端启动成功...");

            RpcRequestMessage sayHello = new RpcRequestMessage(1, "com.george.server.service.HelloService", "sayHello", String.class, new Class[]{String.class}, new Object[]{"张三"});
            ChannelFuture channelFuture = channel.writeAndFlush(sayHello);
            channelFuture.addListener((ChannelFutureListener) future -> {
                if (!future.isSuccess()) {
                    Throwable cause = future.cause();
                    log.error("异常消息：{}", cause);
                }
            });

            channel.closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("RpcClient客户端出现异常, 异常信息：", e);
        } finally {
            worker.shutdownGracefully();
        }

    }
}
