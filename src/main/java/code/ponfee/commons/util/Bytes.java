package code.ponfee.commons.util;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Formatter;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Preconditions;

/**
 * byte[]
 * @author fupf
 */
public final class Bytes {

    private static final char SINGLE_SPACE = ' ';
    private static final String HEX_CODES = "0123456789abcdef";
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
     * byte[]转hex
     * @param str
     * @return
     */
    public static String hexEncode(byte[] bytes) {
        //new BigInteger(1, bytes).toString(16);
        StringBuilder builder = new StringBuilder(bytes.length * 2);
        // one byte -> two hex
        for (int n = bytes.length, i = 0; i < n; i++) {
            builder.append(HEX_CODES.charAt((bytes[i] & 0xf0) >> 4))
                   .append(HEX_CODES.charAt((bytes[i] & 0x0f) >> 0));
        }
        return builder.toString();
    }

    /**
     * hex转byte[]
     * @param hex
     * @return
     */
    public static byte[] hexDecode(String hex) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(hex.length() / 2);
        // two hex -> one byte
        for (int i = 0, n = hex.length(); i < n; i += 2) {
            baos.write(HEX_CODES.indexOf(hex.charAt(i)) << 4 
                       | HEX_CODES.indexOf(hex.charAt(i + 1)));
        }
        return baos.toByteArray();
    }

    public static char[] toCharArray(byte[] bytes) {
        return toCharArray(bytes, US_ASCII.name());
    }

    public static char[] toCharArray(byte[] bytes, String charset) {
        //return new String(bytes, Charset.forName(charset)).toCharArray();
        ByteBuffer buffer = ByteBuffer.allocate(bytes.length);
        buffer.put(bytes);
        buffer.flip();
        return Charset.forName(charset).decode(buffer).array();
    }

    public static byte[] fromCharArray(char[] chars) {
        return fromCharArray(chars, US_ASCII.name());
    }

    public static byte[] fromCharArray(char[] chars, String charset) {
        //return new String(chars).getBytes(Charset.forName(charset));
        CharBuffer buffer = CharBuffer.allocate(chars.length);
        buffer.put(chars);
        buffer.flip();
        return Charset.forName(charset).encode(buffer).array();
    }

    public static byte[] fromLong(long number) {
        return ByteBuffer.allocate(8).putLong(number).array();
    }

    public static long toLong(byte[] bytes, int fromIdx) {
        return ((ByteBuffer) ByteBuffer.allocate(8).put(bytes, fromIdx, 8).flip()).getLong();
    }

    /**
     * 比较两个byte数组是否相同
     * @param b1
     * @param b2
     * @return
     */
    public static boolean equals(byte[] b1, byte[] b2) {
        if (b1 == b2) {
            return true;
        }
        if (b1 == null || b2 == null) {
            return false;
        }
        if (b1.length != b1.length) {
            return false;
        }

        int result = 0;
        for (int n = b1.length, i = 0; i < n; i++) {
            result |= b1[i] ^ b2[i];
        }
        return result == 0;
    }

    /**
     * merge byte array
     * @param first
     * @param rest
     * @return
     */
    public static byte[] concat(byte[] first, byte[]... rest) {
        Preconditions.checkArgument(first != null, "the first can not be null");
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
