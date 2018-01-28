package code.ponfee.commons.util;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Formatter;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Preconditions;

/**
 * byte[]
 * 转hex：new BigInteger(1, bytes).toString(16);
 * @author fupf
 */
public final class Bytes {

    private static final char SINGLE_SPACE = ' ';
    private static final Charset US_ASCII = Charset.forName("US-ASCII");

    /**
     * dump byte array like as these 
     * @see org.apache.commons.io.HexDump#dump(byte[], long, java.io.OutputStream, int)
     * @see sun.misc.HexDumpEncoder#encode(byte[], java.io.OutputStream);
     * 
     * @param bytes  字节数组
     * @param block  每块大小
     * @param chunk  每行块数
     * @return
     */
    public static String hexDump(byte[] bytes, int block, int chunk) {
        Formatter fmt = new Formatter(), text;

        for (int i, j = 0, wid = block * chunk; j * wid < bytes.length; j++) {
            fmt.format("%06x: ", j * wid); // 输出行号：“00000: ”

            text = new Formatter(); // 右边文本
            for (i = 0; i < wid && (i + j * wid) < bytes.length; i++) {
                byte b = bytes[i + j * wid];
                fmt.format("%02X ", b); // 输出hex：“B1 ”
                if ((i + 1) % block == 0 || i + 1 == wid) {
                    fmt.format("%s", SINGLE_SPACE); // block与block间加一个空格
                }
                if (b >= 0x21 && b <= 0x7e) {
                    text.format("%c", b);
                } else {
                    text.format("%c", '.'); // 非ascii码则输出“.”
                }
            }

            if (i < wid) { // 最后一行空格补全：i为该行的byte数
                fmt.format("%s", StringUtils.repeat(SINGLE_SPACE, (wid - i) * 3)); // 补全byte位
                for (int k = i + 1; k <= wid; k += block) {
                    fmt.format("%s", SINGLE_SPACE); // 补全block与block间的空格
                }
            }

            fmt.format("%s", SINGLE_SPACE); // block与text间加一个空格
            fmt.format("%s", text.toString()); // 输出text：“..@.s.UwH...b{.U”
            fmt.format("%s", "\n"); // 输出换行
            text.close();
        }

        fmt.flush();
        try {
            return fmt.toString();
        } finally {
            fmt.close();
        }
    }

    public static String hexDump(byte[] buf) {
        return hexDump(buf, 8, 2);
    }

    /**
     * convert byte array to char array
     * @param bytes the byte array
     * @return char array
     */
    public static char[] toCharArray(byte[] bytes) {
        return toCharArray(bytes, US_ASCII.name());
    }

    /**
     * convert byte array to char array
     * @param bytes the byte array
     * @param charset the encoding of char array
     * @return char array
     */
    public static char[] toCharArray(byte[] bytes, String charset) {
        //return new String(bytes, Charset.forName(charset)).toCharArray();
        ByteBuffer buffer = ByteBuffer.allocate(bytes.length);
        buffer.put(bytes);
        buffer.flip();
        return Charset.forName(charset).decode(buffer).array();
    }

    /**
     * convert char array to byte array
     * @param chars the char array
     * @return byte array
     */
    public static byte[] fromCharArray(char[] chars) {
        return fromCharArray(chars, US_ASCII.name());
    }

    /**
     * convert char array to byte array
     * @param chars the char array
     * @param charset the charset
     * @return byte array
     */
    public static byte[] fromCharArray(char[] chars, String charset) {
        //return new String(chars).getBytes(Charset.forName(charset));
        CharBuffer buffer = CharBuffer.allocate(chars.length);
        buffer.put(chars);
        buffer.flip();
        return Charset.forName(charset).encode(buffer).array();
    }

    public static byte[] fromInt(int value) {
        return ByteBuffer.allocate(4).putInt(value).array();
    }

    public static int toInt(byte[] bytes) {
        return toInt(bytes, 0);
    }

    public static int toInt(byte[] bytes, int fromIdx) {
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.put(bytes, fromIdx, 4).flip();
        return buffer.getInt();
    }

    /**
     * convert long number to byte array
     * @param number the long number
     * @return byte array
     */
    public static byte[] fromLong(long number) {
        return ByteBuffer.allocate(8).putLong(number).array();
    }

    /**
     * convert byte array to long number
     * @param bytes  the byte array
     * @param fromIdx the byte array offset
     * @return long number
     */
    public static long toLong(byte[] bytes, int fromIdx) {
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.put(bytes, fromIdx, 8).flip();
        return buffer.getLong();
    }

    /**
     * convert byte array to long number
     * @param bytes the byte array
     * @return
     */
    public static long toLong(byte[] bytes) {
        return toLong(bytes, 0);
    }

    /**
     * 转BigInteger
     * @param bytes
     * @return
     */
    public static BigInteger toBigInteger(byte[] bytes) {
        return new BigInteger(1, bytes);
    }

    /**
     * merge byte arrays
     * @param first  first byte array of args
     * @param rest   others byte array
     * @return a new byte array of them
     */
    public static byte[] concat(byte[] first, byte[]... rest) {
        Preconditions.checkArgument(first != null, "the first can not be null");
        if (rest == null || rest.length == 0) {
            return first;
        }

        int totalLength = first.length;
        for (byte[] array : rest) {
            if (array != null) {
                totalLength += array.length;
            }
        }

        byte[] result = Arrays.copyOf(first, totalLength);
        int offset = first.length;
        for (byte[] array : rest) {
            if (array != null) {
                System.arraycopy(array, 0, result, offset, array.length);
                offset += array.length;
            }
        }
        return result;
    }

}
