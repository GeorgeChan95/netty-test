package com.george.nio.c4.udp;

import com.george.nio.c2.ByteBufferUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

/**
 * <p>
 *     测试UDP发送和接收数据
 * </p>
 *
 * @author George
 * @date 2023.10.14 15:15
 */
@Slf4j
public class UdpServer {
    public static void main(String[] args) {
        try (DatagramChannel channel = DatagramChannel.open()) {
            channel.socket().bind(new InetSocketAddress(9999));
            System.out.println("waiting...");
            ByteBuffer buffer = ByteBuffer.allocate(32);
            channel.receive(buffer);
            buffer.flip();
            ByteBufferUtil.debugRead(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
