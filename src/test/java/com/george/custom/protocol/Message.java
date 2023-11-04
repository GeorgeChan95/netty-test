package com.george.custom.protocol;

import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 *     自定义消息父类，用于定义消息类型和抽象方法，供子类实现
 * </p>
 *
 * @author George
 * @date 2023.11.01 20:03
 */
@Data
public abstract class Message implements Serializable {

    public static Class<? extends  Message> getMessageClass(int messageType) {
        return messageClasses.get(messageType);
    }

    /**
     * 请求序号
     */
    private int sequenceId;

    /**
     * 消息类型
     */
    private int messageType;

    /**
     * 抽象方法，等待子类实现
     * @param messageType
     */
    public abstract int getMessageType(int messageType);

    /**
     * 登录请求消息类型设定为：0
     */
    public static final int LoginRequestMessage = 0;

    /**
     * 定义map，存储已知的消息类型
     */
    private static final Map<Integer, Class<? extends Message>> messageClasses = new HashMap<>();

    /**
     * 静态代码块，执行向messageClasses写入数据
     */
    static {
        messageClasses.put(LoginRequestMessage, LoginRequestMessage.class);
    }
}
