package com.george.netty.c3.promise;

import io.netty.channel.EventLoop;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.DefaultPromise;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutionException;

/**
 * <p></p>
 *
 * @author George
 * @date 2023.10.17 17:10
 */
@Slf4j
public class Test4 {
    public static void main(String[] args) {
        NioEventLoopGroup group = new NioEventLoopGroup(2);
        EventLoop eventLoop = group.next();
        DefaultPromise<Integer> promise = new DefaultPromise<>(eventLoop);

        eventLoop.execute(() -> {
            try {
                Thread.sleep(3000);
                // 这里有除零异常
                int i = 1/0;
                log.info("set success, {}", 10);
                promise.setSuccess(10);
            } catch (InterruptedException e) {
                promise.setFailure(e);
            } catch (Exception e) {
                promise.setFailure(new RuntimeException(e));
            }
        });

        Integer getNow = promise.getNow(); // null
        log.info("getNow ==> {}", getNow);

        try {
            // 阻塞线程，等待结果返回
            promise.await(); // 与 sync 和 get 区别在于，不会抛异常
            boolean success = promise.isSuccess();
            if (success) {
                log.info("同步获取结果：{}", promise.get());
            } else {
                Throwable cause = promise.cause(); // 调用了await()方法，这里的 cause不为null
                String msg = cause.toString(); // 发生异常：java.lang.RuntimeException: java.lang.ArithmeticException: / by zero
                log.error("发生异常：{}", msg);
                cause.printStackTrace();
            }
        } catch (InterruptedException e) {
            log.error("=========== 1111 ===========");
            e.printStackTrace();
        } catch (ExecutionException e) {
            log.error("=========== 2222 ===========");
            e.printStackTrace();
        }
    }
}
