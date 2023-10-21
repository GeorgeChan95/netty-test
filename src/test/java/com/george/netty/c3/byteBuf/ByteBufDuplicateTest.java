package com.george.netty.c3.byteBuf;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import lombok.extern.slf4j.Slf4j;

import static io.netty.buffer.ByteBufUtil.appendPrettyHexDump;
import static io.netty.util.internal.StringUtil.NEWLINE;

/**
 * <p>
 *     duplicate
 *     【零拷贝】的体现之一，就好比截取了原始 ByteBuf 所有内容，并且没有 max capacity 的限制，也是与原始 ByteBuf 使用同一块底层内存，只是读写指针是独立的
 * </p>
 *
 * @author George
 * @date 2023.10.21 14:37
 */
@Slf4j
public class ByteBufDuplicateTest {
    public static void main(String[] args) {
        ByteBuf origin = ByteBufAllocator.DEFAULT.buffer(10);
        origin.writeBytes(new byte[] {1, 2, 3, 4, 5});
        log.info("复制前：原始ByteBuf .... ");
        log(origin);
        /*
read index:0 write index:5 capacity:10
         +-------------------------------------------------+
         |  0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f |
+--------+-------------------------------------------------+----------------+
|00000000| 01 02 03 04 05                                  |.....           |
+--------+-------------------------------------------------+----------------+
         */



        ByteBuf duplicate = origin.duplicate();
        duplicate.writeByte(6);

        log.info("复制后：原始ByteBuf .... ");
        log(origin);
        log.info("原始ByteBuf 下标为5的字节：{}", origin.getByte(5));
        /*
read index:0 write index:5 capacity:10
         +-------------------------------------------------+
         |  0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f |
+--------+-------------------------------------------------+----------------+
|00000000| 01 02 03 04 05                                  |.....           |
+--------+-------------------------------------------------+----------------+
14:44:38.086 [main] INFO com.george.netty.c3.byteBuf.ByteBufDuplicateTest - 原始ByteBuf 下标为5的字节：6
         */




        log.info("复制后的duplicate .... ");
        log(duplicate);
        log.info("复制后的ByteBuf 下标为5的字节：{}", duplicate.getByte(5));
        /*
read index:0 write index:6 capacity:10
         +-------------------------------------------------+
         |  0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f |
+--------+-------------------------------------------------+----------------+
|00000000| 01 02 03 04 05 06                               |......          |
+--------+-------------------------------------------------+----------------+
14:44:38.086 [main] INFO com.george.netty.c3.byteBuf.ByteBufDuplicateTest - 复制后的ByteBuf 下标为5的字节：6
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
