package com.george.server.handler;

import com.george.message.RpcRequestMessage;
import com.george.message.RpcResponseMessage;
import com.george.server.service.HelloService;
import com.george.server.service.ServicesFactory;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;

/**
 * <p>
 *     RPC请求处理器，负责处理客户端发来的请求
 * </p>
 *
 * @author George
 * @date 2023.11.16 20:23
 */
@Slf4j
@ChannelHandler.Sharable
public class RpcRequestMessageHandler extends SimpleChannelInboundHandler<RpcRequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequestMessage message) throws Exception {
        log.info("接收到消息请求：{}", message);
        RpcResponseMessage response = new RpcResponseMessage();
        int sequenceId = message.getSequenceId();
        response.setSequenceId(sequenceId);

        try {
            // 通过反射获取消息接口
            HelloService helloService = (HelloService) ServicesFactory.getService(Class.forName(message.getInterfaceName()));
            // 获取要调用的方法
            Method method = helloService.getClass().getMethod(message.getMethodName(), message.getParameterTypes());
            // 执行方法调用,获取返回结果
            Object invoke = method.invoke(helloService, message.getParameterValue());
            // 将返回结果放到响应消息中
            response.setReturnValue(invoke);
        } catch (Exception e) {
            e.printStackTrace();
            // 将异常放到响应消息中
            // 为避免异常消息体太长，这里对异常消息做包装
            String error = e.getCause().getMessage();
            response.setExceptionValue(new Exception(error));
        }
        // 将响应消息发送出去
        ctx.writeAndFlush(response);
    }
}
