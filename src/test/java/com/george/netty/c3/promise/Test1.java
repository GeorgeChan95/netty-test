package com.george.netty.c3.promise;

import io.netty.channel.DefaultEventLoop;
import io.netty.util.concurrent.DefaultPromise;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutionException;

/**
 * <p>
 *     同步处理任务成功
 * </p>
 *
 * @author George
 * @date 2023.10.17 16:12
 */
@Slf4j
public class Test1 {
    public static void main(String[] args) {
        DefaultEventLoop eventLoop = new DefaultEventLoop();
        DefaultPromise<Integer> promise = new DefaultPromise<>(eventLoop);

        eventLoop.execute(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            log.info("promise同步处理成功");
            promise.setSuccess(10);
        });

        Integer now = promise.getNow(); // null
        log.info("getNow() ==> {}", now);
        try {
            Integer integer = promise.get(); // 10
            log.info("get() ==> {}", integer);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

    }
}
