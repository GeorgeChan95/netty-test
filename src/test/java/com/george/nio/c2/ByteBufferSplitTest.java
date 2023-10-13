package com.george.nio.c2;

import java.nio.ByteBuffer;

/**
 * <p>
 *     测试解决 撵包、半包问题
 * </p>
 *
 * @author George
 * @date 2023.10.12 14:22
 */
public class ByteBufferSplitTest {
    public static void main(String[] args) {
        ByteBuffer source = ByteBuffer.allocate(32);
        source.put("hello\nworld\ni am ".getBytes());
        splitBuffer(source);

        source.put("george\n".getBytes());
        splitBuffer(source);
    }

    /**
     * ByteBuffer切分
     * @param source
     */
    private static void splitBuffer(ByteBuffer source) {
        // 首先将buffer切换到读模式
        source.flip();
        // buffer读取的限制，切换到读模式后，position为最后一个字节所在的下标
        int limit = source.limit();

        for (int i = 0; i < limit; i++) {
            if (source.get(i) == '\n') {
                int position = source.position();
                int length = i + 1 - position;
                ByteBuffer target = ByteBuffer.allocate(length);
                // 将source的limit设置为可读到的位置，'\n' 所在下标后的位置
                source.limit(i + 1);
                target.put(source);
                target.flip();
                // 打印
                ByteBufferUtil.debugRead(target);

                // 将source的limit还原
                source.limit(limit);
            }
        }
        // 将未读取的buffer数据压缩
        source.compact();
    }
}
