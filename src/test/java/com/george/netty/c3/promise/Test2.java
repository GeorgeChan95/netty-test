package com.george.netty.c3.promise;

import io.netty.channel.EventLoop;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 *     异步处理任务成功
 * </p>
 *
 * @author George
 * @date 2023.10.17 16:24
 */
@Slf4j
public class Test2 {
    public static void main(String[] args) {
        NioEventLoopGroup group = new NioEventLoopGroup(2);
        EventLoop eventLoop = group.next();
        DefaultPromise<Integer> promise = new DefaultPromise<>(eventLoop);

        eventLoop.execute(() -> {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            log.info("set success, {}", 10);
            promise.setSuccess(10);
        });

        // 设置回调，异步接收结果
        promise.addListener(future -> {
            // 这里的 future 就是上面的 promise
            Object now = future.getNow();
            log.info("异步处理成功,getNow() ==> {}", now);
        });
    }
}
