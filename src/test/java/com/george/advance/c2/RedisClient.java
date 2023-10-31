package com.george.advance.c2;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;

/**
 * <p>
 * netty作为客户端与redis交互
 * 参考：https://www.cnblogs.com/demingblog/p/9989699.html
 * </p>
 *
 * @author George
 * @date 2023.10.31 13:06
 */
@Slf4j
public class RedisClient {
    public static void main(String[] args) {
        // 表示 '\r\n'
        final byte[] LINE = {13, 10};
        NioEventLoopGroup worker = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        Channel channel = null;
        try {
            channel = bootstrap.group(worker)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {

                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new LoggingHandler(LogLevel.DEBUG));
                            ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                                @Override
                                public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                    set(ctx);
                                    get(ctx);
                                    super.channelActive(ctx);
                                }

                                /**
                                 * 向redis添加数据
                                 * set zhangsan 20
                                 * 最终通过写到redis服务端为：*3\r\n$3\r\nSET\r\n$8\r\nzhangsan\r\n$2\r\n20\r\n
                                 * @param ctx
                                 */
                                private void set(ChannelHandlerContext ctx) {


                                    // 向 redis服务端写入命令 set zhangsan 20
                                    ByteBuf buffer = ctx.alloc().buffer();
                                    // 密码认证
                                    auth(buffer);
                                    // *3：表示整个命令是由3部分组成的
                                    buffer.writeBytes("*3".getBytes());
                                    buffer.writeBytes(LINE);
                                    // 命令字节数
                                    buffer.writeBytes("$3".getBytes());
                                    buffer.writeBytes(LINE);
                                    buffer.writeBytes("set".getBytes());
                                    buffer.writeBytes(LINE);
                                    buffer.writeBytes("$8".getBytes());
                                    buffer.writeBytes(LINE);
                                    buffer.writeBytes("zhangsan".getBytes());
                                    buffer.writeBytes(LINE);
                                    buffer.writeBytes("$2".getBytes());
                                    buffer.writeBytes(LINE);
                                    buffer.writeBytes("20".getBytes());
                                    buffer.writeBytes(LINE);

                                    // 向redis服务端发送指令
                                    ctx.writeAndFlush(buffer);
                                }

                                /**
                                 * 从redis获取数据
                                 * get zhangsan
                                 * 最终写到redis服务端命令为：*2\r\n$3\r\nGET\r\n$8\r\nzhangsan\r\n
                                 * @param ctx
                                 */
                                private void get(ChannelHandlerContext ctx) {
                                    // 向redis服务端发送命令
                                    ByteBuf buffer = ctx.alloc().buffer();
                                    // 密码认证
                                    auth(buffer);
                                    // 调用命令
                                    buffer.writeBytes("*2".getBytes());
                                    buffer.writeBytes(LINE);
                                    buffer.writeBytes("$3".getBytes());
                                    buffer.writeBytes(LINE);
                                    buffer.writeBytes("get".getBytes());
                                    buffer.writeBytes(LINE);
                                    buffer.writeBytes("$8".getBytes());
                                    buffer.writeBytes(LINE);
                                    buffer.writeBytes("zhangsan".getBytes());
                                    buffer.writeBytes(LINE);

                                    // 向redis服务端发送指令
                                    ctx.writeAndFlush(buffer);
                                }

                                /**
                                 * redis密码认证
                                 * @param buffer
                                 */
                                private void auth(ByteBuf buffer) {
                                    // 认证命令
                                    buffer.writeBytes("*2".getBytes());
                                    buffer.writeBytes(LINE);
                                    buffer.writeBytes("$4".getBytes());
                                    buffer.writeBytes(LINE);
                                    buffer.writeBytes("auth".getBytes());
                                    buffer.writeBytes(LINE);
                                    buffer.writeBytes("$6".getBytes());
                                    buffer.writeBytes(LINE);
                                    // 这里是密码 要先认证再发命令
                                    buffer.writeBytes("123456".getBytes());
                                    buffer.writeBytes(LINE);
                                }

                                @Override
                                public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                    ByteBuf byteBuf = (ByteBuf) msg;
                                    String data = byteBuf.toString(Charset.forName("utf-8"));
                                    log.info("读取消息：\n{}", data);
                                    /*
                                    打印结果：
                                        +OK
                                        +OK
                                        +OK
                                        $2
                                        20
                                     */
                                    super.channelRead(ctx, msg);
                                }
                            });
                        }
                    })
                    .connect("192.168.6.204", 6379)
                    .sync()
                    .channel();
            // 等待关闭
            channel.closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            worker.shutdownGracefully();
        }
    }
}
