package com.george.nio.c4.split;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

/**
 * <p></p>
 *
 * @author George
 * @date 2023.10.13 14:11
 */
@Slf4j
public class Client {
    public static void main(String[] args) {
        try {
            SocketChannel channel = SocketChannel.open();
            channel.connect(new InetSocketAddress("127.0.0.1", 8080));
            // 写入数据超过服务端默认字节buffer设定的大小
            channel.write(StandardCharsets.UTF_8.encode("1234\nabcdefghijklmno"));
            channel.write(StandardCharsets.UTF_8.encode("pqrstuvwxyz\n"));
            channel.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
