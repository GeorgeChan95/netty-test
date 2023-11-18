package com.george.message;

import lombok.Data;
import lombok.ToString;

/**
 * <p></p>
 *
 * @author George
 * @date 2023.11.14 15:38
 */
@Data
@ToString(callSuper = true)
public class RpcRequestMessage extends Message{

    /**
     * 接口全限定名
     */
    private String interfaceName;

    /**
     * 方法名
     */
    private String methodName;

    /**
     * 返回值类型
     */
    private Class<?> returnType;

    /**
     * 参数类型数据数组
     */
    private Class[] parameterTypes;

    /**
     * 参数值数组
     */
    private Object[] parameterValue;

    public RpcRequestMessage() {
    }

    public RpcRequestMessage(Integer sequenceId,
                             String interfaceName,
                             String methodName,
                             Class<?> returnType,
                             Class[] parameterTypes,
                             Object[] parameterValue) {
        super.setSequenceId(sequenceId);
        this.interfaceName = interfaceName;
        this.methodName = methodName;
        this.returnType = returnType;
        this.parameterTypes = parameterTypes;
        this.parameterValue = parameterValue;
    }

    @Override
    public int getMessageType() {
        return RPC_MESSAGE_TYPE_REQUEST;
    }
}
