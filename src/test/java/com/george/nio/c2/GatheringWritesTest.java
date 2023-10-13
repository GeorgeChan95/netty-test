package com.george.nio.c2;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;

/**
 * <p>
 *     测试多个ByteBuffer向一个chanel中写数据
 * </p>
 *
 * @author George
 * @date 2023.10.12 13:58
 */
public class GatheringWritesTest {
    public static void main(String[] args) {
        ByteBuffer a = StandardCharsets.UTF_8.encode("hello");
        ByteBuffer b = StandardCharsets.UTF_8.encode("world");
        ByteBuffer c = StandardCharsets.UTF_8.encode("你好");

        try (RandomAccessFile file = new RandomAccessFile("gather.txt", "rw")) {
            FileChannel channel = file.getChannel();
            channel.write(new ByteBuffer[]{a, b,c});
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
