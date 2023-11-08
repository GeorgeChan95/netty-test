package com.george.client;

import com.george.protocol.MessageCodecSharable;
import com.george.protocol.ProcotolFrameDecoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * <p></p>
 *
 * @author George
 * @date 2023.11.04 15:41
 */
@Slf4j
public class ChatClient {

    public static void main(String[] args) {
        NioEventLoopGroup group = new NioEventLoopGroup();
        LoggingHandler logHandler = new LoggingHandler(LogLevel.DEBUG);
        MessageCodecSharable codecSharable = new MessageCodecSharable();

        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.group(group);
            bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    // 添加长度帧解码器
                    ch.pipeline().addLast(new ProcotolFrameDecoder());
                    // 日志打印
                    ch.pipeline().addLast(logHandler);
                    // 自定义消息转换编/解码器
                    ch.pipeline().addLast(codecSharable);
                    /**
                     * 心跳检测
                     * readerIdleTimeSeconds – 状态为 IdleState.READER_IDLE 的 IdleStateEvent，当在指定时间段内没有执行读取时将触发。 指定 0 禁用
                     * writerIdleTimeSeconds – 当指定时间段内没有执行写入操作时，将触发状态为 IdleState.WRITER_IDLE 的 IdleStateEvent。 指定 0 禁用。
                     * allIdleTimeSeconds – 状态为 IdleState.ALL_IDLE 的 IdleStateEvent，当指定时间段内未执行任何读取或写入操作时，将触发该事件。 指定 0 禁用
                     */
                    ch.pipeline().addLast(new IdleStateHandler(0, 3, 0));
                    // 自定义ChannelDuplexHandler 同时可作为入站和出站处理器
                    ch.pipeline().addLast(new ClientDuplexHandler().getDuplexHandler());
                }
            });
            Channel channel = bootstrap.connect("127.0.0.1", 8080).sync().channel();
            channel.closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
        }
    }

}
