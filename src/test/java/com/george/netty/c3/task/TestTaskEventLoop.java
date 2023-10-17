package com.george.netty.c3.task;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * <p>
 *     测试使用EventLoop执行普通任务、间隔任务
 * </p>
 *
 * @author George
 * @date 2023.10.17 13:58
 */
@Slf4j
public class TestTaskEventLoop {
    public static void main(String[] args) {

        EventLoopGroup group = new NioEventLoopGroup(2);

        group.execute(() -> {
            log.info("event loop 执行普通任务");
        });

        /**
         * 创建一个给定初始延迟的间隔性的任务，之后的下次执行时间是上一次任务从执行到结束所需要的时间+给定的间隔时间。
         * 举个例子：比如我给定任务的初始延迟(long initialdelay)是12:00， 间隔为1分钟 。
         * 那么这个任务会在12:00 首次创建并执行，如果该任务从执行到结束所需要消耗的时间为1分钟，
         * 那么下次任务执行的时间理应从12：01 再加上设定的间隔1分钟，那么下次任务执行时间是12:02 。
         * 这里的间隔时间（delay） 是从上次任务执行结束开始算起的。
         */
        group.scheduleWithFixedDelay(() -> {
            log.info("延迟定长时间执行任务");
        }, 1, 3, TimeUnit.SECONDS);

        /**
         * 创建一个给定初始延迟的间隔性的任务，之后的每次任务执行时间为 初始延迟 + N * delay(间隔)  。
         * 这里的N为首次任务执行之后的第N个任务，N从1开始，
         * 意思就是 首次执行任务的时间为12:00 那么下次任务的执行时间是固定的 是12:01 下下次为12:02。
         * 与scheduleWithFixedDelay 最大的区别就是 ，scheduleAtFixedRate  不受任务执行时间的影响。
         */
        group.scheduleAtFixedRate(() -> {
            log.info("按照指定的时间周期执行任务");
        }, 1, 2, TimeUnit.SECONDS);
    }
}
