package com.george.server;

import com.george.message.Message;
import com.george.protocol.MessageCodecSharable;
import com.george.protocol.ProcotolFrameDecoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * <p></p>
 *
 * @author George
 * @date 2023.11.04 15:51
 */
@Slf4j
public class ChatServer {
    public static void main(String[] args) {
        NioEventLoopGroup boss = new NioEventLoopGroup();
        NioEventLoopGroup worker = new NioEventLoopGroup();
        LoggingHandler LOGGING_HANDLER = new LoggingHandler(LogLevel.DEBUG);
        MessageCodecSharable MESSAGE_CODEC = new MessageCodecSharable();
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.channel(NioServerSocketChannel.class);
            serverBootstrap.group(boss, worker);
            serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new ProcotolFrameDecoder());
                    ch.pipeline().addLast(LOGGING_HANDLER);
                    ch.pipeline().addLast(MESSAGE_CODEC);
                    /**
                     * 心跳检测
                     * readerIdleTimeSeconds – 状态为 IdleState.READER_IDLE 的 IdleStateEvent，当在指定时间段内没有执行读取时将触发。 指定 0 禁用
                     * writerIdleTimeSeconds – 当指定时间段内没有执行写入操作时，将触发状态为 IdleState.WRITER_IDLE 的 IdleStateEvent。 指定 0 禁用。
                     * allIdleTimeSeconds – 状态为 IdleState.ALL_IDLE 的 IdleStateEvent，当指定时间段内未执行任何读取或写入操作时，将触发该事件。 指定 0 禁用
                     */
                    ch.pipeline().addLast(new IdleStateHandler(5, 0, 0));
                    // 自定义Handler，同时可作为入站和出站处理器
                    ch.pipeline().addLast(new ServerDuplexHandler().getDuplexHandler());
                    ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                        @Override
                        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                            Message message = (Message) msg;
                            log.info("接收到消息：{}", msg);
                        }
                    });
                }
            });
            Channel channel = serverBootstrap.bind(8080).sync().channel();
            channel.closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }

}
