package com.george.message;

import lombok.Data;
import lombok.ToString;

/**
 * <p></p>
 *
 * @author George
 * @date 2023.11.14 16:00
 */
@Data
@ToString(callSuper = true)
public class RpcResponseMessage extends Message {

    /**
     * 返回值
     */
    private Object returnValue;
    /**
     * 异常值
     */
    private Exception exceptionValue;

    @Override
    public int getMessageType() {
        return RPC_MESSAGE_TYPE_RESPONSE;
    }
}
