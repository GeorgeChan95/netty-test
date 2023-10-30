package com.george.advance;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.FixedLengthFrameDecoder;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;

/**
 * <p>
 *     基于长度字段帧的解析
 *     https://www.cnblogs.com/java-chen-hao/p/11571229.html
 * </p>
 *
 * @author George
 * @date 2023.10.28 18:04
 */
@Slf4j
public class LengthFieldServer {
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
                        // 基于长度字段帧的解析，具体见：https://www.cnblogs.com/java-chen-hao/p/11571229.html
                        /*
                        lengthFieldOffset=0：开始的1个字节就是长度域，所以不需要长度域偏移。
                        lengthFieldLength=4：长度域4个字节。
                        lengthAdjustment=-4：数据长度修正为-4，因为长度域除了包含数据的长度12，还包含了长度字段本身4字节，所以需要减4。
                        initialBytesToStrip=4：发送的数据有消息本身和消息长度(4字节)，而接收的数据只有需要消息，所以需要跳过4
                        1030字节。
                         */
                        ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(1024, 0, 4, -4, 4));
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
