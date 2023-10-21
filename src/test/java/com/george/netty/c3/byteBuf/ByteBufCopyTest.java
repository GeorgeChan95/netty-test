package com.george.netty.c3.byteBuf;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import lombok.extern.slf4j.Slf4j;

import static io.netty.buffer.ByteBufUtil.appendPrettyHexDump;
import static io.netty.util.internal.StringUtil.NEWLINE;

/**
 * <p>
 *     copy()
 *     会将底层内存数据进行深拷贝，因此无论读写，都与原始 ByteBuf 无关
 * </p>
 *
 * @author George
 * @date 2023.10.21 14:46
 */
@Slf4j
public class ByteBufCopyTest {
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



        ByteBuf copy = origin.copy();
        copy.writeByte(6);

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
14:51:07.730 [main] INFO com.george.netty.c3.byteBuf.ByteBufCopyTest - 原始ByteBuf 下标为5的字节：0
         */




        log.info("复制后的copy .... ");
        log(copy);
        log.info("复制后的ByteBuf 下标为5的字节：{}", copy.getByte(5));
        /*
read index:0 write index:6 capacity:16
         +-------------------------------------------------+
         |  0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f |
+--------+-------------------------------------------------+----------------+
|00000000| 01 02 03 04 05 06                               |......          |
+--------+-------------------------------------------------+----------------+
14:51:07.730 [main] INFO com.george.netty.c3.byteBuf.ByteBufCopyTest - 复制后的ByteBuf 下标为5的字节：6
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
