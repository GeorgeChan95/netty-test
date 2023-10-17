package com.george.netty.c3.promise;

import io.netty.channel.EventLoop;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.DefaultPromise;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 *     异步处理任务失败
 * </p>
 *
 * @author George
 * @date 2023.10.17 17:30
 */
@Slf4j
public class Test5 {
    public static void main(String[] args) {
        NioEventLoopGroup group = new NioEventLoopGroup();
        EventLoop eventLoop = group.next();
        DefaultPromise<Integer> promise = new DefaultPromise<>(eventLoop);

        promise.addListener(future -> {
            boolean success = future.isSuccess();
            if (success) {
                Integer data = (Integer) future.getNow();
                log.info("操作成功,结果：{}\n", data);
            } else {
                Throwable cause = future.cause();
                log.error("操作异常：{}", cause.toString());
            }
        });

        eventLoop.execute(() -> {
            try {
                Thread.sleep(1000);
                int i = 1 / 0;
                log.info("success...");
                promise.setSuccess(10);
            } catch (InterruptedException e) {
                promise.setFailure(e);
            } catch (Exception e) {
                promise.setFailure(e);
            }
        });
    }
}
