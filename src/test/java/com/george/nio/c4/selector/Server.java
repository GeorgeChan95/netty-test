package com.george.nio.c4.selector;

import com.george.nio.c2.ByteBufferUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;

/**
 * <p>
 *     测试使用Selector实现单个Selector管理多个Channel
 * </p>
 *
 * @author George
 * @date 2023.10.12 20:34
 */
@Slf4j
public class Server {
    public static void main(String[] args) {
        try {
            // 创建Selector
            Selector selector = Selector.open();

            // 开启服务端ServerSocketChannel
            ServerSocketChannel ssc = ServerSocketChannel.open();

            // 将ServerSocketChannel设置为非阻塞
            ssc.configureBlocking(false);

            // 将ServerSocketChannel与Selector绑定, 并
            SelectionKey sscKey = ssc.register(selector, 0, null);

            // 设置ServerSocketChannel关注的事件的类型
            sscKey.interestOps(SelectionKey.OP_ACCEPT);
            log.info("sscKey ===> {}", sscKey);

            // 设置ServerSocketChannel绑定的端口
            ssc.bind(new InetSocketAddress(8080));

            while (true) {
                // 返回值代表有多少 channel 发生了事件
                int count = selector.select();
                // 获取所有事件，遍历事件，逐一处理
                Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
                while (iter.hasNext()) {
                    SelectionKey selectionKey = iter.next();
                    // 处理完之后要把key移除
                    iter.remove();
                    if (selectionKey.isAcceptable()) { // 如果是连接事件
                        // 从连接事件中获取ServerSocketChannel通道
                        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) selectionKey.channel();
                        // 建立与客户端的连接
                        SocketChannel sc = serverSocketChannel.accept();
                        sc.configureBlocking(false);
                        // 将客户端的连接注册到Selector上，并设置关注可读事件
                        sc.register(selector, SelectionKey.OP_READ, null);
                        log.info("连接已建立....{}", sc);
                    } else if (selectionKey.isReadable()) { // 如果是可读事件
                        SocketChannel channel = (SocketChannel) selectionKey.channel();
                        log.info("reading... {}", channel);
                        ByteBuffer byteBuffer = ByteBuffer.allocate(10);
                        int read = channel.read(byteBuffer);
                        if (read == -1) {
                            log.info("\n断开客户端连接，===> {}", channel);
                            selectionKey.cancel();
                        } else {
                            // 切换到读模式
                            byteBuffer.flip();
                            // 打印读取到的内容
                            ByteBufferUtil.debugRead(byteBuffer);
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
