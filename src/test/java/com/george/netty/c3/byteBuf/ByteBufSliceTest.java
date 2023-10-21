package com.george.netty.c3.byteBuf;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufUtil;
import lombok.extern.slf4j.Slf4j;

import static io.netty.buffer.ByteBufUtil.appendPrettyHexDump;
import static io.netty.util.internal.StringUtil.NEWLINE;

/**
 * <p>
 *     测试Slice的使用
 *     slice 对原始 ByteBuf 进行切片成多个 ByteBuf，
 *     切片后的 ByteBuf 并没有发生内存复制，
 *     还是使用原始 ByteBuf 的内存，
 *     切片后的 ByteBuf 维护独立的 read，write 指针
 * </p>
 *
 * @author George
 * @date 2023.10.21 13:44
 */
@Slf4j
public class ByteBufSliceTest {
    public static void main(String[] args) {
        // 初始化一个ByteBuf，并写入数据
        ByteBuf origin = ByteBufAllocator.DEFAULT.buffer(10);
        origin.writeBytes(new byte[]{1,2,3,4});
        log(origin);
        /* 打印如下：
read index:0 write index:4 capacity:10
         +-------------------------------------------------+
         |  0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f |
+--------+-------------------------------------------------+----------------+
|00000000| 01 02 03 04                                     |....            |
+--------+-------------------------------------------------+----------------+
        * */




        // 读取一个字节，查看buffer
        origin.readByte();
        log(origin);
        /* 打印如下：
read index:1 write index:4 capacity:10
         +-------------------------------------------------+
         |  0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f |
+--------+-------------------------------------------------+----------------+
|00000000| 02 03 04                                        |...             |
+--------+-------------------------------------------------+----------------+
        * */




        log.info("======== 分割线 ========");
        // 调用slice进行切片,
        // 无参 slice 是从原始 ByteBuf 的 read index 到 write index 之间的内容进行切片，
        // 切片后的 max capacity 被固定为这个区间的大小，因此不能追加 write
        ByteBuf slice = origin.slice();
        log(slice);
        /* 打印如下：
read index:0 write index:3 capacity:3
         +-------------------------------------------------+
         |  0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f |
+--------+-------------------------------------------------+----------------+
|00000000| 02 03 04                                        |...             |
+--------+-------------------------------------------------+----------------+
         */



        // 此时对原始的ByteBuf又读了一个字节，打印原始ByteBuf和切片后的ByteBuf，
        // 切片后的ByteBuf读写指针都没有变化，原因是切片后的ByteBuf有着独立的读写指针
        origin.readByte();
        log.info("原始ByteBuf打印如下：");
        log(origin);
        log.info("切片ByteBuf打印如下：");
        log(slice);
        /* 打印如下：
14:12:48.986 [main] INFO com.george.netty.c3.byteBuf.ByteBufSliceTest - 原始ByteBuf打印如下：
read index:2 write index:4 capacity:10
         +-------------------------------------------------+
         |  0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f |
+--------+-------------------------------------------------+----------------+
|00000000| 03 04                                           |..              |
+--------+-------------------------------------------------+----------------+
14:12:48.986 [main] INFO com.george.netty.c3.byteBuf.ByteBufSliceTest - 切片ByteBuf打印如下：
read index:0 write index:3 capacity:3
         +-------------------------------------------------+
         |  0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f |
+--------+-------------------------------------------------+----------------+
|00000000| 02 03 04                                        |...             |
+--------+-------------------------------------------------+----------------+
         */



        // 修改Slice，原始的ByteBuf也会受到影响,因为底层都是同一块内存
        slice.setByte(2, 5);
        log.info("切片ByteBuf已修改，打印如下：");
        log(slice);
        log.info("查看原始ByteBuf，打印如下：");
        log(origin);
        /* 打印如下：
14:20:29.912 [main] INFO com.george.netty.c3.byteBuf.ByteBufSliceTest - 切片ByteBuf已修改，打印如下：
read index:0 write index:3 capacity:3
         +-------------------------------------------------+
         |  0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f |
+--------+-------------------------------------------------+----------------+
|00000000| 02 03 05                                        |...             |
+--------+-------------------------------------------------+----------------+
14:20:29.913 [main] INFO com.george.netty.c3.byteBuf.ByteBufSliceTest - 查看原始ByteBuf，打印如下：
read index:2 write index:4 capacity:10
         +-------------------------------------------------+
         |  0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f |
+--------+-------------------------------------------------+----------------+
|00000000| 03 05                                           |..              |
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
