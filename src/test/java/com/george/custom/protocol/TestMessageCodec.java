package com.george.custom.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

/**
 * <p>
 *     测试自定义协议，完成消息的编解码
 * </p>
 *
 * @author George
 * @date 2023.11.01 20:50
 */
public class TestMessageCodec {
    public static void main(String[] args) throws Exception {
        EmbeddedChannel channel = new EmbeddedChannel(
                new LoggingHandler(LogLevel.DEBUG),
                // 配合LengthFieldBasedFrameDecoder 一起使用，确保接到的 ByteBuf 消息是完整的
                new LengthFieldBasedFrameDecoder(1024, 12, 4, 0, 0),
                new MessageCodec()
        );

        // 测试消息编码 encode
        Message message = new LoginRequestMessage("zhangsan", "123456");
//        channel.writeOutbound(message);

        // 测试消息解码 encode、decode
        ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer();
        new MessageCodec().encode(null, message, buffer);
        channel.writeInbound(buffer);
    }
}
