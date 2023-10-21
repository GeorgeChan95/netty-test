package com.george.netty.c3.byteBuf;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import lombok.extern.slf4j.Slf4j;

import static io.netty.buffer.ByteBufUtil.appendPrettyHexDump;
import static io.netty.util.internal.StringUtil.NEWLINE;

/**
 * <p>
 *     创建ByteBuf
 * </p>
 *
 * @author George
 * @date 2023.10.17 20:37
 */
@Slf4j
public class ByteBufTest {
    public static void main(String[] args) {
        // 创建了一个默认的 ByteBuf（池化基于直接内存的 ByteBuf），初始容量是 10
        ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer(10);
        log.info("{}", buffer); // PooledUnsafeDirectByteBuf(ridx: 0, widx: 0, cap: 10)
        log(buffer);

        // 创建池化基于直接内存的 ByteBuf
        ByteBuf heapBuffer = ByteBufAllocator.DEFAULT.heapBuffer(16);
        log.info("\n{}\n", heapBuffer); // PooledUnsafeHeapByteBuf(ridx: 0, widx: 0, cap: 16)

        // 池化基于直接内存的 ByteBuf，初始容量是 32
        ByteBuf directBuffer = ByteBufAllocator.DEFAULT.directBuffer(32);
        log.info("\n{}\n", directBuffer); // PooledUnsafeDirectByteBuf(ridx: 0, widx: 0, cap: 32)

        log.info("======== 分界线：写入字节 ========");
        // 向ByteBuf中写入内容
        directBuffer.writeBytes(new byte[] {1, 2,3,4});
        log(directBuffer);
        /* 打印结果如下：
        read index:0 write index:4 capacity:32
         +-------------------------------------------------+
         |  0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f |
+--------+-------------------------------------------------+----------------+
|00000000| 01 02 03 04                                     |....            |
+--------+-------------------------------------------------+----------------+
        * */

        log.info("======== 分界线：写入int ========");
        directBuffer.clear();
        directBuffer.writeInt(1);
        log(directBuffer);
        /* int占4字节，默认大端写入。
        打印结果如下：
12:31:26.526 [main] INFO com.george.netty.c3.byteBuf.ByteBufTest - ======== 分界线：写入int ========
read index:0 write index:4 capacity:32
         +-------------------------------------------------+
         |  0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f |
+--------+-------------------------------------------------+----------------+
|00000000| 00 00 00 01                                     |....            |
+--------+-------------------------------------------------+----------------+
        * */


        /**
         * ButeBuf扩容，
         * 扩容规则：
         * 如何写入后数据大小未超过 512，则选择下一个 16 的整数倍，例如写入后大小为 12 ，则扩容后 capacity 是 16
         * 如果写入后数据大小超过 512，则选择下一个 2^n，例如写入后大小为 513，则扩容后 capacity 是 2^10=1024（2^9=512 已经不够了）
         * 扩容不能超过 max capacity 会报错
         */
        log.info("======== 分界线：ButeBuf扩容 ========");
        ByteBuf buffer1 = ByteBufAllocator.DEFAULT.buffer(6);
        log.info("======== 分界线：扩容前 ========");
        log(buffer1);
        buffer1.writeInt(1);
        buffer1.writeInt(2);
        log.info("======== 分界线：扩容后 ========");
        log(buffer1);
        /* 打印结果如下：
        13:06:33.677 [main] INFO com.george.netty.c3.byteBuf.ByteBufTest - ======== 分界线：ButeBuf扩容 ========
13:06:33.677 [main] INFO com.george.netty.c3.byteBuf.ByteBufTest - ======== 分界线：扩容前 ========
read index:0 write index:0 capacity:6

13:06:33.677 [main] INFO com.george.netty.c3.byteBuf.ByteBufTest - ======== 分界线：扩容后 ========
read index:0 write index:8 capacity:16
         +-------------------------------------------------+
         |  0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f |
+--------+-------------------------------------------------+----------------+
|00000000| 00 00 00 01 00 00 00 02                         |........        |
+--------+-------------------------------------------------+----------------+
        * */
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
