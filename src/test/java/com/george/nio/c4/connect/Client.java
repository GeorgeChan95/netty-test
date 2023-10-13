package com.george.nio.c4.connect;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

/**
 * <p></p>
 *
 * @author George
 * @date 2023.10.12 20:18
 */
@Slf4j
public class Client {
    public static void main(String[] args) {
        try {
            SocketChannel sc = SocketChannel.open(new InetSocketAddress("127.0.0.1", 8080));
            ByteBuffer buffer = StandardCharsets.UTF_8.encode("hello");
            // writer()方法从buffer中读向SocketChannel中写
            sc.write(buffer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
