package com.george.nio.c4.split;

import com.george.nio.c2.ByteBufferUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;

import static java.nio.channels.SelectionKey.OP_ACCEPT;
import static java.nio.channels.SelectionKey.OP_READ;

/**
 * <p>
 *     测试服务端读取客户端数据，数据过大超过服务端字节缓冲区，需要对字节缓冲区进行扩容的情况
 * </p>
 *
 * @author George
 * @date 2023.10.13 10:07
 */
@Slf4j
public class Server {
    public static void main(String[] args) {
        try {
            // 创建一个selector
            Selector selector = Selector.open();
            // 开启服务端连接通道
            ServerSocketChannel ssc = ServerSocketChannel.open();
            // 设置 serverSocketChannel为非阻塞模式
            ssc.configureBlocking(false);

            // 将ServerSocketChannel注册到Selector，指定关注的事件类型，并添加附件
            ssc.register(selector, OP_ACCEPT, null);
            // 绑定服务端端口
            ssc.bind(new InetSocketAddress(8080));
            while (true) {
                // 阻塞监听客户端的事件
                int count = selector.select();
                // 获取服务端所有事件，逐一遍历处理
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    // 将获取到的事件，主动移除，否则服务端会重复处理
                    SelectionKey key = iterator.next();
                    iterator.remove();
                    if (key.isAcceptable()) { // accept请求
                        // 服务端连接通道
                        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
                        // 建立与客户端的连接，并返回客户端连接通道
                        SocketChannel channel = serverChannel.accept();
                        // 设置客户端连接非阻塞
                        channel.configureBlocking(false);
                        // 创建ByteBuffer，并初始化大小，此ByteBuffer作为channel的附件
                        ByteBuffer buffer = ByteBuffer.allocate(16);
                        // 将客户端连接通道注册到selector
                        SelectionKey selectionKey = channel.register(selector, OP_READ, buffer);
//                        selectionKey.interestOps(OP_READ);
                        log.info("accept channel ===> {}", channel);
                        log.info("accept selectionKey ===> {}", selectionKey);
                    } else if (key.isReadable()) { // read请求
                        // 获取read事件对应的客户端连接通道
                        SocketChannel channel = (SocketChannel) key.channel();
                        // 获取read事件附带的ByteBuffer
                        ByteBuffer byteBuffer = (ByteBuffer) key.attachment();
                        int read = channel.read(byteBuffer);
                        if (read == -1) { // 连接终端
                            key.cancel();
                            log.error("read Channel退出连接 ===> {}", channel);
                        } else {
                            splitBuffer(byteBuffer);
                            // byteBuffer需要扩容
                            if (byteBuffer.position() == byteBuffer.limit()) { // 数据过大，byteBuffer满了，position与limit相同
                                // 创建新的ByteBuffer，容量是原容量的两倍
                                ByteBuffer newBuffer = ByteBuffer.allocate(byteBuffer.limit() * 2);
                                log.info("byteBuffer扩容...");
                                // 旧byteBuffer切换到读模式
                                byteBuffer.flip();
                                // 将旧的buffer数据写入到新的buffer中
                                newBuffer.put(byteBuffer);
                                // key的buffer附件设置成新的buffer
                                key.attach(newBuffer);
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 切分
     * @param sourceBuffer
     */
    private static void splitBuffer(ByteBuffer sourceBuffer) {
        // 切换到读模式
        sourceBuffer.flip();
        int limit = sourceBuffer.limit();
        for (int i = 0; i < limit; i++) {
            // get(i) 方法获取指定下标的字节数据，但是不会增加position的值
            if (sourceBuffer.get(i) == '\n') { // 当读取到换行符，表示需要切分
                int length = i + 1 - sourceBuffer.position();
                // 创建新的ByteBuffer，存储切分的字节数据
                ByteBuffer targetBuffer = ByteBuffer.allocate(length);
                for (int j = 0; j < length; j++) {
                    // get()方法获取当前位置的字节数据，并将position的位置向前移动
                    targetBuffer.put(sourceBuffer.get());
                }
                ByteBufferUtil.debugAll(targetBuffer);
            }
        }
        // 将未读取处理的字节，压缩，并将ByteBuffer切换到写模式
        sourceBuffer.compact();
    }
}
