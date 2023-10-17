package com.george.netty.c3.promise;

import io.netty.channel.DefaultEventLoop;
import io.netty.util.concurrent.DefaultPromise;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * await 死锁检查
 * </p>
 *
 * @author George
 * @date 2023.10.17 20:23
 */
@Slf4j
public class Test6 {
    public static void main(String[] args) {
        DefaultEventLoop eventExecutors = new DefaultEventLoop();
        DefaultPromise<Integer> promise = new DefaultPromise<>(eventExecutors);
        eventExecutors.submit(() -> {
            System.out.println("1");
            try {
                promise.await();
                // 注意不能仅捕获 InterruptedException 异常
                // 否则 死锁检查抛出的 BlockingOperationException 会继续向上传播
                // 而提交的任务会被包装为 PromiseTask，它的 run 方法中会 catch 所有异常然后设置为 Promise 的失败结果而不会抛出
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("2");
        });
        eventExecutors.submit(() -> {
            System.out.println("3");
            try {
                promise.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("4");
        });
    }

    /**
     打印结果：
     1
     2
     3
     4
     io.netty.util.concurrent.BlockingOperationException: DefaultPromise@3be26768(incomplete)
     at io.netty.util.concurrent.DefaultPromise.checkDeadLock(DefaultPromise.java:384)
     at io.netty.util.concurrent.DefaultPromise.await(DefaultPromise.java:212)
     at com.george.netty.c3.promise.Test6.lambda$main$0(Test6.java:23)
     at io.netty.util.concurrent.PromiseTask$RunnableAdapter.call(PromiseTask.java:38)
     at io.netty.util.concurrent.PromiseTask.run(PromiseTask.java:73)
     at io.netty.channel.DefaultEventLoop.run(DefaultEventLoop.java:54)
     at io.netty.util.concurrent.SingleThreadEventExecutor$5.run(SingleThreadEventExecutor.java:918)
     at io.netty.util.internal.ThreadExecutorMap$2.run(ThreadExecutorMap.java:74)
     at io.netty.util.concurrent.FastThreadLocalRunnable.run(FastThreadLocalRunnable.java:30)
     at java.lang.Thread.run(Thread.java:748)
     io.netty.util.concurrent.BlockingOperationException: DefaultPromise@3be26768(incomplete)
     at io.netty.util.concurrent.DefaultPromise.checkDeadLock(DefaultPromise.java:384)
     at io.netty.util.concurrent.DefaultPromise.await(DefaultPromise.java:212)
     at com.george.netty.c3.promise.Test6.lambda$main$1(Test6.java:38)
     at io.netty.util.concurrent.PromiseTask$RunnableAdapter.call(PromiseTask.java:38)
     at io.netty.util.concurrent.PromiseTask.run(PromiseTask.java:73)
     at io.netty.channel.DefaultEventLoop.run(DefaultEventLoop.java:54)
     at io.netty.util.concurrent.SingleThreadEventExecutor$5.run(SingleThreadEventExecutor.java:918)
     at io.netty.util.internal.ThreadExecutorMap$2.run(ThreadExecutorMap.java:74)
     at io.netty.util.concurrent.FastThreadLocalRunnable.run(FastThreadLocalRunnable.java:30)
     at java.lang.Thread.run(Thread.java:748)
     */
}
