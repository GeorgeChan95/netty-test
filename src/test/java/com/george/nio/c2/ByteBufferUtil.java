package com.george.nio.c2;

import io.netty.util.internal.StringUtil;

import java.nio.ByteBuffer;

import static io.netty.util.internal.MathUtil.isOutOfBounds;
import static io.netty.util.internal.StringUtil.NEWLINE;

public class ByteBufferUtil {
    /**
     * 包含256个字符的数组，用于将字节（0-255范围内的值）映射到字符。
     * 具体地，它用于将字节转换为ASCII字符表示，其中不可打印的字符将被替换为'.'字符。
     */
    private static final char[] BYTE2CHAR = new char[256];
    /**
     * 包含256 * 4个字符的数组，用于将字节的值（0-255范围内的值）映射为十六进制字符串。
     * 这是用于将字节值转换为十六进制表示的查找表。
     */
    private static final char[] HEXDUMP_TABLE = new char[256 * 4];
    /**
     * 包含16个字符串的数组，用于生成十六进制转储中每行的填充。
     * 这些字符串包含一些空格字符，以确保输出的格式在每行的末尾保持对齐。
     */
    private static final String[] HEXPADDING = new String[16];
    /**
     * 包含65536 >>> 4个字符串的数组，用于生成十六进制转储中每行的行前缀。
     * 这些行前缀包括每行的偏移量，用于指示数据在转储中的位置。
     * 65536 >>> 4 = 4096 (65536 / 2^4)
     */
    private static final String[] HEXDUMP_ROWPREFIXES = new String[65536 >>> 4];
    /**
     * 包含256个字符串的数组，用于将字节值（0-255范围内的值）转换为两位十六进制表示。
     * 这用于在十六进制转储中格式化字节的十六进制表示。
     */
    private static final String[] BYTE2HEX = new String[256];
    /**
     * 包含16个字符串的数组，用于生成字节转储中每行的填充。
     * 这些字符串包含一些空格字符，以确保输出的格式在每行的末尾保持对齐。
     */
    private static final String[] BYTEPADDING = new String[16];

    static {
        final char[] DIGITS = "0123456789abcdef".toCharArray();
        for (int i = 0; i < 256; i++) {
            /**
             * 这行代码将 HEXDUMP_TABLE 数组的偶数索引（0、2、4、...）设置为字节的高四位（高位四个比特）的十六进制表示字符。
             * i >>> 4 是将 i 向右逻辑移动 4 位，以获取高四位的值，
             * 然后通过 & 0x0F 操作保证它的范围在 0-15 之间，以将其用作索引来获取 DIGITS 数组中的相应字符。
             */
            HEXDUMP_TABLE[i << 1] = DIGITS[i >>> 4 & 0x0F];
            /**
             * 这行代码将 HEXDUMP_TABLE 数组的奇数索引（1、3、5、...）设置为字节的低四位（低位四个比特）的十六进制表示字符。
             * i & 0x0F 通过 & 操作获取字节的低四位值，然后将其用作索引来获取 DIGITS 数组中的相应字符。
             */
            HEXDUMP_TABLE[(i << 1) + 1] = DIGITS[i & 0x0F];
        }

        int i;

        // Generate the lookup table for hex dump paddings
        // 生成用于十六进制转储（hex dump）中的每行的填充字符串，并将这些填充字符串存储在名为 HEXPADDING 的字符串数组中。
        for (i = 0; i < HEXPADDING.length; i++) {
            // 计算 padding 变量，该变量表示当前位置 i 到数组末尾的距离（数组长度减去 i）。这个距离用于确定每行的填充字符数量。
            int padding = HEXPADDING.length - i;
            // 创建一个 StringBuilder 对象 buf，其容量是 padding * 3。
            // 这里的 padding * 3 表示每个填充字符都包含三个空格，这将用于在十六进制转储中在数据之间创建间距。
            StringBuilder buf = new StringBuilder(padding * 3);
            // 在嵌套的 for 循环中，使用 j 遍历从0到 padding - 1 的索引，这样就可以将三个空格字符追加到 buf 中，以形成填充字符串。
            for (int j = 0; j < padding; j++) {
                buf.append("   ");
            }
            // 将生成的填充字符串（由 buf.toString() 返回）存储在 HEXPADDING[i] 中，以便在十六进制转储时使用。这将确保每行数据在打印时都具有一致的填充，以保持格式的对齐。
            HEXPADDING[i] = buf.toString();
        }

        // Generate the lookup table for the start-offset header in each row (up to 64KiB).
        // 生成用于十六进制转储（hex dump）中的每行的起始偏移量头部字符串，
        // 并将这些字符串存储在名为 HEXDUMP_ROWPREFIXES 的字符串数组中。
        for (i = 0; i < HEXDUMP_ROWPREFIXES.length; i++) {
            StringBuilder buf = new StringBuilder(12);
            buf.append(NEWLINE);
            /**
             * 将一个整数 i 左移 4 位，然后将结果转换为一个十六进制字符串，并将该字符串追加到名为 buf 的 StringBuilder 对象中
             * 1、i << 4：这部分将整数 i 左移 4 位。左移操作将 i 的二进制表示向左移动 4 位，相当于将 i 的值乘以 16。
             *  这是因为每个十六进制位对应于四个二进制位，所以左移 4 位相当于将 i 的值转化为十六进制中的一位。
             * 2、& 0xFFFFFFFFL：这一部分是一个按位与运算，它的目的是确保结果是一个无符号的 32 位整数。
             *  0xFFFFFFFFL 是一个 32 位全 1 的二进制常数，通过与操作可以清除高于 32 位的位数，使结果保持 32 位。
             *  这有助于确保生成的十六进制字符串具有 8 位（32 位二进制）。
             * 3、| 0x100000000L：这一部分是将结果的最高位（第 32 位）设置为 1。
             *  它确保生成的字符串始终具有 8 位，即使最高位是 0 时也将其设置为 1。
             *  这有助于确保生成的十六进制字符串始终包含 8 位，而不会被截断。
             * 4、最终将整数结果转换为一个十六进制字符串。
             */
            buf.append(Long.toHexString(i << 4 & 0xFFFFFFFFL | 0x100000000L));
            // 在字符串的第 9 个字符位置（从 0 开始数）设置一个 | 字符，用于分隔偏移量和数据。
            buf.setCharAt(buf.length() - 9, '|');
            buf.append('|');
            HEXDUMP_ROWPREFIXES[i] = buf.toString();
        }

        // Generate the lookup table for byte-to-hex-dump conversion
        // 初始化名为 BYTE2HEX 的字符串数组，将每个字节值（0-255 范围内的整数）
        // 映射为对应的十六进制字符串表示，并且在字符串前面添加一个空格字符。
        for (i = 0; i < BYTE2HEX.length; i++) {
            BYTE2HEX[i] = ' ' + StringUtil.byteToHexStringPadded(i);
        }

        // Generate the lookup table for byte dump paddings
        // 生成用于字节（byte）转储的每行数据的填充字符串，并将这些字符串存储在名为 BYTEPADDING 的字符串数组中。
        for (i = 0; i < BYTEPADDING.length; i++) {
            int padding = BYTEPADDING.length - i;
            StringBuilder buf = new StringBuilder(padding);
            for (int j = 0; j < padding; j++) {
                buf.append(' ');
            }
            BYTEPADDING[i] = buf.toString();
        }

        // Generate the lookup table for byte-to-char conversion
        // 将字节转换为ASCII字符表示，其中不可打印的字符将被替换为'.'字符
        for (i = 0; i < BYTE2CHAR.length; i++) {
            // 0x1f 表示十六进制值，等于十进制的 31。它通常用于表示ASCII字符集中的控制字符（不可打印字符），如回车、换行、制表符等。
            // 0x7f 表示十六进制值，等于十进制的 127。它代表ASCII字符集中的删除字符（DEL）。
            if (i <= 0x1f || i >= 0x7f) {
                BYTE2CHAR[i] = '.';
            } else {
                BYTE2CHAR[i] = (char) i;
            }
        }
    }

    /**
     * 打印所有内容
     * @param buffer
     */
    public static void debugAll(ByteBuffer buffer) {
        int oldlimit = buffer.limit();
        buffer.limit(buffer.capacity());
        StringBuilder origin = new StringBuilder(256);
        appendPrettyHexDump(origin, buffer, 0, buffer.capacity());
        System.out.println("+--------+-------------------- all ------------------------+----------------+");
        System.out.printf("position: [%d], limit: [%d]\n", buffer.position(), oldlimit);
        System.out.println(origin);
        buffer.limit(oldlimit);
    }

    /**
     * 打印可读取内容
     * @param buffer
     */
    public static void debugRead(ByteBuffer buffer) {
        StringBuilder builder = new StringBuilder(256);
        appendPrettyHexDump(builder, buffer, buffer.position(), buffer.limit() - buffer.position());
        System.out.println("+--------+-------------------- read -----------------------+----------------+");
        System.out.printf("position: [%d], limit: [%d]\n", buffer.position(), buffer.limit());
        System.out.println(builder);
    }

    public static void main(String[] args) {
        ByteBuffer buffer = ByteBuffer.allocate(10);
        buffer.put(new byte[]{97, 98, 99, 100});
        debugAll(buffer);
    }

    private static void appendPrettyHexDump(StringBuilder dump, ByteBuffer buf, int offset, int length) {
        if (isOutOfBounds(offset, length, buf.capacity())) {
            throw new IndexOutOfBoundsException(
                    "expected: " + "0 <= offset(" + offset + ") <= offset + length(" + length
                            + ") <= " + "buf.capacity(" + buf.capacity() + ')');
        }
        if (length == 0) {
            return;
        }
        dump.append(
                "         +-------------------------------------------------+" +
                        NEWLINE + "         |  0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f |" +
                        NEWLINE + "+--------+-------------------------------------------------+----------------+");

        final int startIndex = offset;
        final int fullRows = length >>> 4;
        final int remainder = length & 0xF;

        // Dump the rows which have 16 bytes.
        for (int row = 0; row < fullRows; row++) {
            int rowStartIndex = (row << 4) + startIndex;

            // Per-row prefix.
            appendHexDumpRowPrefix(dump, row, rowStartIndex);

            // Hex dump
            int rowEndIndex = rowStartIndex + 16;
            for (int j = rowStartIndex; j < rowEndIndex; j++) {
                dump.append(BYTE2HEX[getUnsignedByte(buf, j)]);
            }
            dump.append(" |");

            // ASCII dump
            for (int j = rowStartIndex; j < rowEndIndex; j++) {
                dump.append(BYTE2CHAR[getUnsignedByte(buf, j)]);
            }
            dump.append('|');
        }

        // Dump the last row which has less than 16 bytes.
        if (remainder != 0) {
            int rowStartIndex = (fullRows << 4) + startIndex;
            appendHexDumpRowPrefix(dump, fullRows, rowStartIndex);

            // Hex dump
            int rowEndIndex = rowStartIndex + remainder;
            for (int j = rowStartIndex; j < rowEndIndex; j++) {
                dump.append(BYTE2HEX[getUnsignedByte(buf, j)]);
            }
            dump.append(HEXPADDING[remainder]);
            dump.append(" |");

            // Ascii dump
            for (int j = rowStartIndex; j < rowEndIndex; j++) {
                dump.append(BYTE2CHAR[getUnsignedByte(buf, j)]);
            }
            dump.append(BYTEPADDING[remainder]);
            dump.append('|');
        }

        dump.append(NEWLINE +
                "+--------+-------------------------------------------------+----------------+");
    }

    /**
     * 定义了一个名为 appendHexDumpRowPrefix 的方法，用于在十六进制转储（hex dump）中的每一行数据前添加一个行前缀字符串。它接受三个参数：
     * @param dump 一个 StringBuilder 对象，表示正在构建的十六进制转储字符串。
     * @param row 一个整数，表示当前行的索引。
     * @param rowStartIndex 一个整数，表示当前行数据的起始偏移量。
     */
    private static void appendHexDumpRowPrefix(StringBuilder dump, int row, int rowStartIndex) {
        if (row < HEXDUMP_ROWPREFIXES.length) {
            dump.append(HEXDUMP_ROWPREFIXES[row]);
        } else {
            dump.append(NEWLINE);
            dump.append(Long.toHexString(rowStartIndex & 0xFFFFFFFFL | 0x100000000L));
            dump.setCharAt(dump.length() - 9, '|');
            dump.append('|');
        }
    }

    /**
     * 它用于从给定的 ByteBuffer 对象中获取一个无符号的字节值。
     * @param buffer
     * @param index
     * @return
     */
    public static short getUnsignedByte(ByteBuffer buffer, int index) {
        // (short) (buffer.get(index) & 0xFF)：这部分将获取的字节值进行处理，将其转换为无符号的 short 类型
        return (short) (buffer.get(index) & 0xFF);
    }
}