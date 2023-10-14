package com.george.nio.c4.multiThread;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <p>
 *     测试多线程读写NIO数据
 * </p>
 *
 * @author George
 * @date 2023.10.14 12:51
 */
@Slf4j
public class BossServer {
    public static void main(String[] args) {
        Thread.currentThread().setName("boss");
        try {
            Selector bossSelector = Selector.open();
            ServerSocketChannel ssc = ServerSocketChannel.open();
            ssc.configureBlocking(false);
            ssc.register(bossSelector, SelectionKey.OP_ACCEPT, null);
            ssc.bind(new InetSocketAddress(8080));
            // 创建固定数量的worker并初始化
            // Runtime.getRuntime().availableProcessors() 获取电脑CPU的核心数
            WorkerServer[] workers = new WorkerServer[Runtime.getRuntime().availableProcessors()];
            for (int i = 0; i < workers.length; i++) {
                workers[i] = new WorkerServer("worker-" + i);
            }
            AtomicInteger index = new AtomicInteger();
            while (true) {
                // 阻塞等待真正的客户端连接事件进来时，分配给worker
                bossSelector.select();
                Iterator<SelectionKey> iter = bossSelector.selectedKeys().iterator();
                while (iter.hasNext()) {
                    SelectionKey key = iter.next();
                    iter.remove();
                    if (key.isAcceptable()) {
                        // 服务端连接客户端，获取客户端连接通道
                        SocketChannel clientChannel = ssc.accept();
                        clientChannel.configureBlocking(false);
                        // index.getAndIncrement() % workers.length 轮询算法
                        // 将客户端连接通道注册到worker
                        log.info("before register...{}", clientChannel.getRemoteAddress());
                        workers[index.getAndIncrement() % workers.length].register(clientChannel);
                        log.info("after register...{}", clientChannel.getRemoteAddress());
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
