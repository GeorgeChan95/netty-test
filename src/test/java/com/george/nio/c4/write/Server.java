package com.george.nio.c4.write;

import com.george.nio.c2.ByteBufferUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

/**
 * <p>
 *     测试Server端可写事件
 * </p>
 *
 * @author George
 * @date 2023.10.13 15:08
 */
@Slf4j
public class Server {
    public static void main(String[] args) {
        try {
            ServerSocketChannel ssc = ServerSocketChannel.open();
            ssc.configureBlocking(false);
            ssc.bind(new InetSocketAddress(8080));

            Selector selector = Selector.open();
            ssc.register(selector, SelectionKey.OP_ACCEPT, null);

            while (true) {
                selector.select();
                Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
                while (iter.hasNext()) {
                    SelectionKey key = iter.next();
                    iter.remove();

                    if (key.isAcceptable()) { // 可读事件
                        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
                        // 与客户端建立连接，返回客户端SocketChannel
                        SocketChannel channel = serverChannel.accept();
                        channel.configureBlocking(false);
                        // 注册客户端通道，默认监听 read 事件
                        SelectionKey selectionKey = channel.register(selector, SelectionKey.OP_READ, null);
                        // 模拟Server端向Client端写数据
                        StringBuilder sb = new StringBuilder();
                        for (int i = 0; i < 10000000; i++) {
                            sb.append("a");
                        }
                        ByteBuffer writeBuffer = StandardCharsets.UTF_8.encode(sb.toString());
                        // 返回本次向客户端通道写入的字节数
                        int write = channel.write(writeBuffer);
                        log.info("实际写入字节数：{} ===> channel: {}", write, channel);
                        // 如果buffer一次没能读取完，还有剩余，则给channel添加write事件
                        if (writeBuffer.hasRemaining()) {
                            // 原有监听的事件 + 新添加的事件
                            selectionKey.interestOps(selectionKey.interestOps() + SelectionKey.OP_WRITE);
                            // 将未读取完的buffer添加到selectionKey中
                            selectionKey.attach(writeBuffer);
                        }
                    } else if (key.isWritable()) { // 可写事件
                        SocketChannel channel = (SocketChannel) key.channel();
                        ByteBuffer writeBuffer = (ByteBuffer) key.attachment();
                        int write = channel.write(writeBuffer);
                        log.info("实际写入字节数：{} ===> channel: {}", write, channel);
                        // buffer中的数据全部写完
                        if (!writeBuffer.hasRemaining()) {
                            // 只要向 channel 发送数据时，socket 缓冲可写，这个事件会频繁触发，
                            // 因此应当只在 socket 缓冲区写不下时再关注可写事件，数据写完之后再取消关注
                            key.interestOps(key.interestOps() - SelectionKey.OP_WRITE);
                            key.attach(null);
                        }
                    } else if (key.isReadable()) {
                        SocketChannel channel = (SocketChannel) key.channel();
                        ByteBuffer buffer = ByteBuffer.allocate(32);
                        int read = channel.read(buffer);
                        if (read == -1) {
                            key.cancel();
                            log.info("断开连接... {}", channel);
                        } else {
                            // 打印接收到的数据
                            ByteBufferUtil.debugAll(buffer);
                            buffer.clear();
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
