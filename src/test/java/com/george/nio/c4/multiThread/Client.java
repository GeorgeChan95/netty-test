package com.george.nio.c4.multiThread;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

/**
 * <p></p>
 *
 * @author George
 * @date 2023.10.14 14:19
 */
@Slf4j
public class Client {
    public static void main(String[] args) {
        try {
            SocketChannel socketChannel = SocketChannel.open();
            socketChannel.connect(new InetSocketAddress("127.0.0.1", 8080));
            boolean connected = socketChannel.isConnected();
            if (connected) {
                log.info("服务端连接成功");
            }
            socketChannel.write(StandardCharsets.UTF_8.encode("world"));
            for (int i = 0; i < 100; i++) {
                socketChannel.write(StandardCharsets.UTF_8.encode("hello"));
                Thread.sleep(1000);
            }
            // 关闭与服务端的连接
            socketChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
