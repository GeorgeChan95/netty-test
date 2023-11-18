package com.george.client;

import com.george.client.handler.RpcResponseMessageHandler;
import com.george.message.RpcRequestMessage;
import com.george.protocol.MessageCodecSharable;
import com.george.protocol.ProcotolFrameDecoder;
import com.george.server.service.HelloService;
import com.george.utils.SequenceIdGenerator;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.DefaultPromise;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Proxy;

/**
 * <p>
 *     使用JDK动态代理实现RPC远程调用
 * </p>
 *
 * @author George
 * @date 2023.11.18 10:59
 */
@Slf4j
public class RpcClientManager {
    public static void main(String[] args) {
        HelloService service = getProxyService(HelloService.class);
        service.sayHello("zhangsan");
    }

    /**
     * 根据接口类型获取接口的代理实现类
     * @param serviceClass
     * @return
     * @param <T> 接口类型的泛型
     */
    private static <T> T getProxyService(Class<T> serviceClass) {
        ClassLoader loader = serviceClass.getClassLoader();

        Class<?>[] interfaceClases = new Class[] {serviceClass};

        Object instance = Proxy.newProxyInstance(loader, interfaceClases, (proxy, method, args) -> {
            // 将方法调用，转换为消息对象
            int sequenceId  = SequenceIdGenerator.nextId();
            RpcRequestMessage requestMessage = new RpcRequestMessage(sequenceId, serviceClass.getName(), method.getName(), method.getReturnType(), method.getParameterTypes(), args);
            // 将消息发送出去
            getChannel().writeAndFlush(requestMessage);

            // 3. 准备一个空 Promise 对象，来接收结果             指定 promise 对象异步接收结果线程
            DefaultPromise<Object> promise = new DefaultPromise<>(getChannel().eventLoop());
            RpcResponseMessageHandler.PROMISES.put(sequenceId, promise);

            // 异步接收promise执行结果
//            promise.addListener(future -> {
//                boolean success = future.isSuccess();
//                if (success) {
//                    Object now = future.getNow();
//                } else {
//                    Throwable cause = future.cause();
//                    log.error("执行出现异常：", cause);
//                    throw new RuntimeException(cause);
//                }
//            });
//            return proxy;

            // 阻塞等待promise响应结果
            promise.await();
            if (promise.isSuccess()) {
                return promise.getNow();
            } else {
                Throwable cause = promise.cause();
                log.error("执行出现异常：", cause);
                throw new RuntimeException(cause);
            }

        });
        return (T) instance;
    }


    private static Channel channel = null;
    private static final Object LOCK = new Object();

    // 获取唯一的 channel 对象
    public static Channel getChannel() {
        if (channel != null) {
            return channel;
        }
        synchronized (LOCK) { //  t2
            if (channel != null) { // t1
                return channel;
            }
            initChannel();
            return channel;
        }
    }

    // 初始化 channel 方法
    private static void initChannel() {
        NioEventLoopGroup group = new NioEventLoopGroup();
        LoggingHandler LOGGING_HANDLER = new LoggingHandler(LogLevel.DEBUG);
        MessageCodecSharable MESSAGE_CODEC = new MessageCodecSharable();
        RpcResponseMessageHandler RPC_HANDLER = new RpcResponseMessageHandler();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.group(group);
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline().addLast(new ProcotolFrameDecoder());
                ch.pipeline().addLast(LOGGING_HANDLER);
                ch.pipeline().addLast(MESSAGE_CODEC);
                ch.pipeline().addLast(RPC_HANDLER);
            }
        });
        try {
            channel = bootstrap.connect("localhost", 8080).sync().channel();
            channel.closeFuture().addListener(future -> {
                group.shutdownGracefully();
            });
        } catch (Exception e) {
            log.error("client error", e);
        }
    }
}
