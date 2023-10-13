package com.george.nio.c2;

import lombok.extern.slf4j.Slf4j;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;


/**
 * <p></p>
 *
 * @author George
 * @date 2023.10.12 09:25
 */
@Slf4j
public class ChannelDemo1 {
    public static void main(String[] args) {
        try (RandomAccessFile file = new RandomAccessFile("data.txt", "rw")) {
            FileChannel channel = file.getChannel();
            // 设置指定大小的ByteBuffer字节缓冲区
            ByteBuffer buffer = ByteBuffer.allocate(10);
            while (true) {
                // 从channel中读数据向buffer中写数据， 返回读到的字节长度
                int read = channel.read(buffer);
                log.info("\n读到的字节数：{}", read);
                if (read <= 0) {
                    break;
                }
                // 将buffer切换到读模式
                buffer.flip();
                // 当buffer中还有数据
                while (buffer.hasRemaining()) {
                    // 打印读取到的数据
                    log.info("读取数据：{}", (char)buffer.get());
                }
                // 将buffer切换到读模式(注意，clear不会清除buffer中的数据，只会将position重置到0)
                buffer.clear();
                // 方式二：以压缩方式切换到读模式，未读的数据依然保留在buffer中
//                buffer.compact();
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
