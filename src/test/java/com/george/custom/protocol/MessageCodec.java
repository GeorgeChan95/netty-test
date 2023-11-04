package com.george.custom.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

/**
 * <p>
 *     自定义消息的编码和解码
 *     注意：继承接口 ByteToMessageCodec 不能使用注解：@ChannelHandler.Sharable
 *
 *     自定义消息由7部分组成：
 *          1、魔数，      用来在第一时间判定是否是无效数据包
 *          2、版本号，    可以支持协议的升级
 *          3、序列化算法， 消息正文到底采用哪种序列化反序列化方式，可以由此扩展，例如：json、protobuf、hessian、jdk
 *          4、指令类型，   是登录、注册、单聊、群聊... 跟业务相关
 *          5、请求序号，   为了双工通信，提供异步能力
 *          6、正文长度
 *          7、消息正文
 * </p>
 *
 *  测试类见：
 *
 * @author George
 * @date 2023.11.01 20:19
 */
@Slf4j
public class MessageCodec extends ByteToMessageCodec<Message> {

    /**
     * 消息编码
     * @param ctx
     * @param msg
     * @param out
     * @throws Exception
     */
    @Override
    protected void encode(ChannelHandlerContext ctx, Message msg, ByteBuf out) throws Exception {
        log.info("++++++++++++ 进入encode方法");
        // 1. 4字节的魔数
        out.writeBytes(new byte[]{1,2,3,4});
        // 2. 1字节的版本号
        out.writeByte(1);
        // 3. 1字节的序列化类型(比如：0-jdk序列化 1-json序列化)
        out.writeByte(0);
        // 4. 1字节的指令类型(例如：0-登录请求 1-登录响应)
        out.writeByte(msg.getMessageType());
        // 5. 4字节的请求序号
        out.writeInt(msg.getSequenceId());
        // 无意义，消息填充，保证除了正文以外的内容长度为 2^n
        out.writeByte(0xff);

        // 将消息对象序列化成字节流数组
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(msg);
        byte[] bytes = bos.toByteArray();
        // 6. 正文长度
        out.writeInt(bytes.length);
        // 7. 消息正文内容
        out.writeBytes(bytes);
    }

    /**
     * 消息解码
     * @param ctx
     * @param in
     * @param out
     * @throws Exception
     */
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        log.info("=========== 进入decode方法");
        // 1. 4字节的魔数
        int magicNum = in.readInt();
        // 2. 1字节的版本号
        byte version = in.readByte();
        // 3. 1字节的序列化类型(比如：0-jdk序列化 1-json序列化)
        byte serializerType = in.readByte();
        // 4. 1字节的指令类型(例如：0-登录请求 1-登录响应)
        byte messageType = in.readByte();
        // 5. 4字节的请求序号
        int sequenceId = in.readInt();
        // 无意义，消息填充内容
        in.readByte();
        // 6. 正文长度
        int length = in.readInt();
        // 读取内容，写入到字节数组中
        byte[] bytes = new byte[length];
        in.readBytes(bytes, 0, length);
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(bis);
        // 将字节数组反序列化成原消息
        Message message = (Message) ois.readObject();
        log.info("获取到消息：{},\t{},\t{},\t{},\t{}\n", magicNum, version, serializerType, messageType, sequenceId);
        log.info("反序列化消息内容：{}", message.toString());
        // 7. 解析出来的结果要放到 List<Object> 中给下一个Handler用，否则下面的handler获取不到消息
        out.add(message);
    }
}
