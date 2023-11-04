package com.george.custom.protocol;

import lombok.Data;
import lombok.ToString;

/**
 * <p>
 *     模拟Message子类型实现之一：登录请求Message
 * </p>
 *
 * @author George
 * @date 2023.11.01 20:06
 */
@Data
@ToString(callSuper = true)
public class LoginRequestMessage extends Message {

    private String username;
    private String password;

    public LoginRequestMessage() {
    }

    public LoginRequestMessage(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public int getMessageType(int messageType) {
        return LoginRequestMessage;
    }
}
