package com.george.nio.c4.connect;

import com.george.nio.c2.ByteBufferUtil;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.List;

/**
 * <p></p>
 *
 * @author George
 * @date 2023.10.12 20:07
 */
@Slf4j
public class Server {
    public static void main(String[] args) {
        try {
            // 创建ByteBuffer
            ByteBuffer byteBuffer = ByteBuffer.allocate(10);
            // 打开一个ServerSocketChannel连接
            ServerSocketChannel ssc = ServerSocketChannel.open();
            // 绑定端口
            ssc.bind(new InetSocketAddress(8080));
            // server端与client端连接集合
            List<SocketChannel> channels = Lists.newArrayList();
            while (true) {
                // accept()与客户端建立连接，SocketChannel用来与客户端通信
                log.info("connecting...");
                SocketChannel sc = ssc.accept();
                channels.add(sc);
                for (SocketChannel channel : channels) {
                    // 接受客户端发来数据
                    log.info("before reading... {}", channel);
                    channel.read(byteBuffer);
                    // 切换到读模式
                    byteBuffer.flip();
                    // 打印从客户端读取到的内容
                    ByteBufferUtil.debugRead(byteBuffer);
                    byteBuffer.clear();
                    log.info("after reading...{}", channel);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
