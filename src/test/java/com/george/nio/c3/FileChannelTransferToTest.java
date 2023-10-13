package com.george.nio.c3;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

/**
 * <p>
 *     测试FileChannel的零拷贝
 *     效率高，底层会利用操作系统的零拷贝进行优化, 文件大小不能超过 2G
 * </p>
 *
 * @author George
 * @date 2023.10.12 16:07
 */
public class FileChannelTransferToTest {
    public static void main(String[] args) {
        try (FileChannel from = new RandomAccessFile("data.txt", "rw").getChannel();
             FileChannel to = new RandomAccessFile("to.txt", "rw").getChannel()) {
            // 源文件大小
            long size = from.size();
            long position = from.position();
            // 文件复制（零拷贝） 小文件直接复制
//            from.transferTo(position, size, to);

            // left 变量代表还剩余多少字节（超过2G的文件复制）
            for (long left = size; left > 0; ) {
                System.out.println("position:" + (size - left) + " left:" + left);
                left -= from.transferTo((size - left), left, to);
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
