package com.george.nio.c4.multiThread;

import com.george.nio.c2.ByteBufferUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 * <p>
 *     测试多线程读写NIO数据
 * </p>
 *
 * @author George
 * @date 2023.10.14 13:11
 */
@Slf4j
public class WorkerServer implements Runnable{
    /**
     * worker线程名称
     */
    private String name;
    /**
     * worker对应的线程
     */
    private Thread thread;
    /**
     * worker的selector
     */
    private Selector selector;
    /**
     * 是否已初始化过
     */
    private boolean initialize;

    public WorkerServer() {
    }

    public WorkerServer(String name) {
        this.name = name;
    }

    /**
     * 客户端连接通道注册到workerServer
     * @param clientChannel
     */
    public void register(SocketChannel clientChannel) {
        try {
            log.info("客户端注册到worker，channel: {}\n", clientChannel);
            // 仅在线程、seletor未初始化时执行
            if (!initialize) {
                selector = Selector.open();
                // this表start
                thread = new Thread(this, name);
                thread.start();
                initialize = true;
            }
            // 多线程环境中，环境select()阻塞操作，确保新的channel可以注册到Selector中
            selector.wakeup();
            // 客户端channel注册到 worker selector中
            clientChannel.register(selector, SelectionKey.OP_READ, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                // 阻塞获取客户端事件
                selector.select();
                Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
                while (iter.hasNext()) {
                    log.info("worker thread ===> {}\n", Thread.currentThread().getName());
                    SelectionKey key = iter.next();
                    iter.remove();
                    if (key.isReadable()) {
                        SocketChannel channel = (SocketChannel) key.channel();
                        log.debug("read...{}", channel.getRemoteAddress());
                        ByteBuffer byteBuffer = ByteBuffer.allocate(16);
                        int read = channel.read(byteBuffer);
                        if (read == -1) {
                            key.cancel();
                            log.info("客户端断开连接 channel: {}\n", channel);
                        } else {
                            // 切换到读模式
                            byteBuffer.flip();
                            ByteBufferUtil.debugRead(byteBuffer);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
