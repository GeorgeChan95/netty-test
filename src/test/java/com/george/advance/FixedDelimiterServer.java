package com.george.advance;

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
 *     固定分隔符切分消息
 * </p>
 *
 * @author George
 * @date 2023.10.28 15:41
 */
@Slf4j
public class FixedDelimiterServer {
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
                        // 服务端添加固定长度解码器，设置解析字节长度为10字节。
                        ByteBuf delimiter1 = ByteBufAllocator.DEFAULT.buffer().writeBytes("\n".getBytes());
                        ByteBuf delimiter2 = ByteBufAllocator.DEFAULT.buffer().writeBytes("\t".getBytes());
                        ByteBuf delimiter3 = ByteBufAllocator.DEFAULT.buffer().writeBytes("@".getBytes());
                        /**
                         * 参数1024表示单条消息的最大长度，当达到该长度仍然没有找到分隔符就抛出TooLongFrame异常，第二个参数就是分隔符
                         * 第一个true：表示要对解码后的消息去掉分隔符
                         * 第二个true：表示如果为true，则只要解码器注意到帧的长度将超过maxFrameLength，就会立即抛出TooLongFrameException。
                         *             如果为false，则在超过的整个帧之后引发TooLongFrameException
                         * delimiter1, delimiter2, delimiter3：表示自定义的多个分隔符，默认分隔符为：\n 或者 \r\n
                         */
                        ch.pipeline().addLast(new DelimiterBasedFrameDecoder(1024, true, true, delimiter1, delimiter2, delimiter3));
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
