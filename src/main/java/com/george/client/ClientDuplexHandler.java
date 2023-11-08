package com.george.client;

import com.george.message.PingMessage;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

public class ClientDuplexHandler {
    public ChannelDuplexHandler getDuplexHandler() {
        return new ChannelDuplexHandler() {
            @Override
            public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
                // 触发的事件
                IdleStateEvent event = (IdleStateEvent) evt;
                if (event.state() == IdleState.WRITER_IDLE) { // 写空闲事件
                    // 向服务端写入一个ping消息
                    ctx.writeAndFlush(new PingMessage());
                }
            }
        };
    }
}