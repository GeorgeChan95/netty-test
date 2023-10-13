package com.george.nio.c2;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * <p>
 *     测试一个文件的内容读取到多个ByteBuffer中
 * </p>
 *
 * @author George
 * @date 2023.10.12 13:46
 */
public class BufferReadTest1 {
    public static void main(String[] args) {
        try (RandomAccessFile file = new RandomAccessFile("part.txt", "r")){
            // 获取文件读写通道
            FileChannel channel = file.getChannel();
            // 创建三个读取缓存，并指定读取数据的长度
            ByteBuffer a = ByteBuffer.allocate(3);
            ByteBuffer b = ByteBuffer.allocate(3);
            ByteBuffer c = ByteBuffer.allocate(3);
            // 从channel中读，向buffer中写
            channel.read(a);
            channel.read(b);
            channel.read(c);

            // buffer切换到读模式
            a.flip();
            b.flip();
            c.flip();

            String s = a.toString();
            ByteBufferUtil.debugRead(a);
            ByteBufferUtil.debugRead(b);
            ByteBufferUtil.debugRead(c);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
