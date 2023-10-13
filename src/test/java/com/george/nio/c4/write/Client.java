package com.george.nio.c4.write;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

/**
 * <p></p>
 *
 * @author George
 * @date 2023.10.13 15:35
 */
@Slf4j
public class Client {
    public static void main(String[] args) {
        try {
            Selector selector = Selector.open();

            SocketChannel socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false);
            SelectionKey selectionKey = socketChannel.register(selector, SelectionKey.OP_READ + SelectionKey.OP_WRITE + SelectionKey.OP_CONNECT, null);
            // 连接服务端
            socketChannel.connect(new InetSocketAddress("127.0.0.1", 8080));

            int count = 0;
            while (true) {
                // 阻塞等待
                selector.select();
                Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
                while (iter.hasNext()) {
                    SelectionKey key = iter.next();
                    iter.remove();
                    if (key.isConnectable()) {
                        log.info("client connect...");
                        // 处理连接事件，不处理会一直调用
                        socketChannel.finishConnect();
                    } else if (key.isReadable()) {
                        ByteBuffer byteBuffer = ByteBuffer.allocate(1024* 1024);
                        count += socketChannel.read(byteBuffer);
                        log.info("客户端读取字节：{}", count);
                        byteBuffer.clear();
                    } else if (key.isWritable()) {
                        if (count >= 10000000) {
                            // 只要向 channel 发送数据时，socket 缓冲可写，这个事件会频繁触发，
                            // 因此应当只在 socket 缓冲区写不下时再关注可写事件，数据写完之后再取消关注
                            selectionKey.interestOps(selectionKey.interestOps() - SelectionKey.OP_WRITE);
                        } else {
                            socketChannel.write(StandardCharsets.UTF_8.encode("client received byte"));
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
