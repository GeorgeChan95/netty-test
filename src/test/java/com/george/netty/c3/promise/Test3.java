package com.george.netty.c3.promise;

import io.netty.channel.EventLoop;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.DefaultPromise;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutionException;

/**
 * <p>
 *     同步处理任务失败 - sync & get
 * </p>
 *
 * @author George
 * @date 2023.10.17 16:38
 */
@Slf4j
public class Test3 {
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
            boolean success = promise.isSuccess();
            if (success) {
                log.info("同步获取结果：{}", promise.get());
            } else {
                Throwable cause = promise.cause(); // 同步获取到的 cause 为 NULL
                String msg = cause.toString(); // java.lang.NullPointerException
                log.error("发生异常：{}", msg);
            }
        } catch (InterruptedException e) {
            log.error("=========== 1111 ===========");
            e.printStackTrace();
        } catch (Exception e) {
            log.error("=========== 2222 ===========");
            e.printStackTrace();
        }
    }
}
