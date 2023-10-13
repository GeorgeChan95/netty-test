package com.george.nio.c4.selector;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

/**
 * <p></p>
 *
 * @author George
 * @date 2023.10.12 21:13
 */
@Slf4j
public class Client {
    public static void main(String[] args) throws IOException {
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.connect(new InetSocketAddress("127.0.0.1", 8080));
        boolean connected = socketChannel.isConnected();
        if (connected) {
            log.info("服务端连接成功");
        }
        socketChannel.write(StandardCharsets.UTF_8.encode("hello"));
        socketChannel.write(StandardCharsets.UTF_8.encode("world"));
        // 关闭与服务端的连接
        socketChannel.close();
    }
}
