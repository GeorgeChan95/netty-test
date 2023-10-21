package com.george.netty.c3.byteBuf;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.CompositeByteBuf;
import lombok.extern.slf4j.Slf4j;

import static io.netty.buffer.ByteBufUtil.appendPrettyHexDump;
import static io.netty.util.internal.StringUtil.NEWLINE;

/**
 * <p>
 *     CompositeByteBuf
 *     可以将多个 ByteBuf 合并为一个逻辑上的 ByteBuf，避免拷贝
 * </p>
 *
 * @author George
 * @date 2023.10.21 14:24
 */
@Slf4j
public class CompositeByteBufTest {
    public static void main(String[] args) {
        ByteBuf buf1 = ByteBufAllocator.DEFAULT.buffer(10);
        buf1.writeBytes(new byte[] {1, 2, 3, 4, 5});

        ByteBuf buf2 = ByteBufAllocator.DEFAULT.buffer(10);
        buf2.writeBytes(new byte[] {6, 7, 8, 9, 10});

        // 将 buf1 和 buf2 合并成一个ByteBuf
        CompositeByteBuf compositeBuffer = ByteBufAllocator.DEFAULT.compositeBuffer();
        // true 表示增加新的 ByteBuf 自动递增 write index, 否则 write index 会始终为 0
        compositeBuffer.addComponents(true, buf1, buf2);
        log(compositeBuffer);
        /*打印结果如下：
read index:0 write index:10 capacity:10
         +-------------------------------------------------+
         |  0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f |
+--------+-------------------------------------------------+----------------+
|00000000| 01 02 03 04 05 06 07 08 09 0a                   |..........      |
+--------+-------------------------------------------------+----------------+
         */
    }

    private static void log(ByteBuf buffer) {
        int length = buffer.readableBytes();
        int rows = length / 16 + (length % 15 == 0 ? 0 : 1) + 4;
        StringBuilder buf = new StringBuilder(rows * 80 * 2)
                .append("read index:").append(buffer.readerIndex())
                .append(" write index:").append(buffer.writerIndex())
                .append(" capacity:").append(buffer.capacity())
                .append(NEWLINE);
        appendPrettyHexDump(buf, buffer);
        System.out.println(buf.toString());
    }
}
