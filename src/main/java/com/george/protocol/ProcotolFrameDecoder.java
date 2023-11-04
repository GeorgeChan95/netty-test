package com.george.protocol;

import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

/**
 * <p>
 *     自定义基于长度帧的解码器
 * </p>
 *
 * @author George
 * @date 2023.11.04 15:22
 */
public class ProcotolFrameDecoder extends LengthFieldBasedFrameDecoder {

    public ProcotolFrameDecoder() {
        this(1024, 12, 4, 0, 0);
    }


    public ProcotolFrameDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength, int lengthAdjustment, int initialBytesToStrip) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip);
    }

}
