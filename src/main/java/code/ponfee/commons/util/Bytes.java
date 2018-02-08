package code.ponfee.commons.util;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Formatter;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Preconditions;

/**
 * byte[]
 * 转hex：new BigInteger(1, bytes).toString(16);
 * 求与4的余数：(4 - b64.length() % 4) % 4
 * @author fupf
 */
public final class Bytes {

    private static final char SPACE_CHAR = ' ';
    private static final char[] HEX_LOWER_CODES = "0123456789abcdef".toCharArray();
    private static final char[] HEX_UPPER_CODES = "0123456789ABCDEF".toCharArray();

    /**
     * dump byte array like as these 
     * @see org.apache.commons.io.HexDump#dump(byte[], long, java.io.OutputStream, int)
     * @see sun.misc.HexDumpEncoder#encode(byte[], java.io.OutputStream);
     * 
     * @param data   字节数组
     * @param block  每块大小
     * @param chunk  每行块数
     * @return
     */
    public static String hexDump(byte[] data, int block, int chunk) {
        Formatter fmt = new Formatter(), text;

        for (int i, j = 0, wid = block * chunk; j * wid < data.length; j++) {
            fmt.format("%06x: ", j * wid); // 输出行号：“00000: ”

            text = new Formatter(); // 右边文本
            for (i = 0; i < wid && (i + j * wid) < data.length; i++) {
                byte b = data[i + j * wid];
                fmt.format("%02X ", b); // 输出hex：“B1 ”
                if ((i + 1) % block == 0 || i + 1 == wid) {
                    fmt.format("%s", SPACE_CHAR); // block与block间加一个空格
                }
                if (b >= 0x21 && b <= 0x7e) {
                    text.format("%c", b);
                } else {
                    text.format("%c", '.'); // 非ascii码则输出“.”
                }
            }

            if (i < wid) { // 最后一行空格补全：i为该行的byte数
                fmt.format("%s", StringUtils.repeat(SPACE_CHAR, (wid - i) * 3)); // 补全byte位
                for (int k = i + 1; k <= wid; k += block) {
                    fmt.format("%s", SPACE_CHAR); // 补全block与block间的空格
                }
            }

            fmt.format("%s", SPACE_CHAR); // block与text间加一个空格
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

    public static String hexDump(byte[] data) {
        return hexDump(data, 8, 2);
    }

    public static String toBinary(byte... array) {
        if (array == null || array.length == 0) {
            return null;
        }

        StringBuilder builder = new StringBuilder(array.length * 8);
        for (byte b : array) {
            // b & 0xFF：byte转int保留bit位
            // | 0x100：100000000，对于正数保留八位
            // 也可以 + 0x100或leftPad(str, 8, '0')
            builder.append(Integer.toBinaryString((b & 0xFF) | 0x100).substring(1));
        }
        return builder.toString();
    }

    public static String hexEncode(byte[] bytes) {
        return hexEncode(bytes, true);
    }

    /**
     * byte[]转hex
     * @param bytes
     * @param lowercase
     * @return
     */
    public static String hexEncode(byte[] bytes, boolean lowercase) {
        //new BigInteger(1, bytes).toString(16);
        int len = bytes.length;
        char[] out = new char[len << 1];

        char[] codes = lowercase ? HEX_LOWER_CODES : HEX_UPPER_CODES;

        // one byte -> two char
        for (int i = 0, j = 0; i < len; i++) {
            out[j++] = codes[(0xF0 & bytes[i]) >>> 4];
            out[j++] = codes[0x0F & bytes[i]];
        }
        return new String(out);
    }

    /**
     * hex转byte[]
     * @param hex
     * @return
     */
    public static byte[] hexDecode(String hex) {
        char[] data = hex.toCharArray();
        int len = data.length;
        if ((len & 0x01) != 0) {
            throw new IllegalArgumentException("Invalid hex string.");
        }

        byte[] out = new byte[len >> 1];

        // two char -> one byte
        for (int i = 0, j = 0; j < len; i++, j += 2) {
            int f  =  Character.digit(data[j], 16) << 4;
                f |= Character.digit(data[j + 1], 16);
            out[i] = (byte) (f & 0xFF);
        }

        return out;
    }

    // ----------------------------------------------base64 encode/decode-------------------------------------- //
    public static final char[] BASE64_ENCODES = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".toCharArray();
    private static final int[] BASE64_DECODES = new int[256];
    static {
        Arrays.fill(BASE64_DECODES, -1);
        for (int i = 0, len = BASE64_ENCODES.length; i < len; i++) {
            BASE64_DECODES[BASE64_ENCODES[i]] = i;
        }
        BASE64_DECODES['='] = 0;
    }

    /**
     * base64 encode
     * @param data
     * @return 
     * @deprecated {@code Base64.getEncoder().encodeToString(data)}
     */
    @Deprecated
    public static String base64Encode(byte[] data) {
        StringBuilder builder = new StringBuilder(data.length * 4 / 3 + 2);
        for (int i = 0, len = data.length, b1, b2, b3; i < len;) {
            b1 = data[i++] & 0xFF;
            if (i == len) {
                builder.append(BASE64_ENCODES[b1 >>> 2]);
                builder.append(BASE64_ENCODES[(b1 & 0x3) << 4]);
                builder.append("==");
                break;
            }
            b2 = data[i++] & 0xFF;
            if (i == len) {
                builder.append(BASE64_ENCODES[b1 >>> 2]);
                builder.append(BASE64_ENCODES[((b1 & 0x03) << 4) | ((b2 & 0xf0) >>> 4)]);
                builder.append(BASE64_ENCODES[(b2 & 0x0f) << 2]);
                builder.append("=");
                break;
            }
            b3 = data[i++] & 0xFF;
            builder.append(BASE64_ENCODES[b1 >>> 2]);
            builder.append(BASE64_ENCODES[((b1 & 0x03) << 4) | ((b2 & 0xf0) >>> 4)]);
            builder.append(BASE64_ENCODES[((b2 & 0x0f) << 2) | ((b3 & 0xc0) >>> 6)]);
            builder.append(BASE64_ENCODES[b3 & 0x3f]);
        }
        return builder.toString();
    }

    /**
     * base64转byte[]
     * @param b64
     * @return
     * @deprecated {@code Base64.getDecoder().decode(data)}
     */
    @Deprecated
    public static byte[] base64Decode(String b64) {
        byte[] data = b64.getBytes(StandardCharsets.US_ASCII);
        StringBuilder builder = new StringBuilder(data.length * 3 / 4 + 1);
        for (int i = 0, len = data.length, b1, b2, b3, b4; i < len;) {
            /* b1 */
            do {
                b1 = BASE64_DECODES[data[i++]];
            } while (i < len && b1 == -1);
            if (b1 == -1) {
                break;
            }
            /* b2 */
            do {
                b2 = BASE64_DECODES[data[i++]];
            } while (i < len && b2 == -1);
            if (b2 == -1) {
                break;
            }
            builder.append((char) ((b1 << 2) | ((b2 & 0x30) >>> 4)));
            /* b3 */
            do {
                b3 = data[i++];
                if (b3 == 61) {
                    return builder.toString().getBytes(StandardCharsets.ISO_8859_1);
                }
                b3 = BASE64_DECODES[b3];
            } while (i < len && b3 == -1);

            if (b3 == -1) {
                break;
            }
            builder.append((char) (((b2 & 0x0f) << 4) | ((b3 & 0x3c) >>> 2)));
            /* b4 */
            do {
                b4 = data[i++];
                if (b4 == 61) {
                    return builder.toString().getBytes(StandardCharsets.ISO_8859_1);
                }
                b4 = BASE64_DECODES[b4];
            } while (i < len && b4 == -1);

            if (b4 == -1) {
                break;
            }
            builder.append((char) (((b3 & 0x03) << 6) | b4));
        }
        return builder.toString().getBytes(StandardCharsets.ISO_8859_1);
    }

    /**
     * @param data
     * @return
     * @Deprecated {@code Base64.getUrlEncoder()[.withoutPadding()].encodeToString(data)}
     */
    @Deprecated
    public static String base64EncodeUrlSafe(byte[] data) {
        String str = base64Encode(data);
        return StringUtils.replaceEach(str, new String[] { "+", "/", "=" }, new String[] { "-", "_", "" });
    }

    /**
     * @param b64
     * @return
     * @Deprecated {@code Base64.getUrlDecoder().decode(data)}
     */
    @Deprecated
    public static byte[] base64DecodeUrlSafe(String b64) {
        b64 = StringUtils.replaceEach(b64, new String[] { "-", "_" }, new String[] { "+", "/" });
        b64 += StringUtils.leftPad("", (4 - b64.length() % 4) % 4, '=');
        return base64Decode(b64);
    }

    /**
     * convert byte array to char array
     * @param bytes the byte array
     * @return char array
     */
    public static char[] toCharArray(byte[] bytes) {
        return toCharArray(bytes, StandardCharsets.US_ASCII.name());
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
        return fromCharArray(chars, StandardCharsets.US_ASCII.name());
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

    public static byte[] fromShort(short value) {
        return ByteBuffer.allocate(Short.BYTES).putShort(value).array();
    }

    public static short toShort(byte[] bytes) {
        return toShort(bytes, 0);
    }

    public static short toShort(byte[] bytes, int fromIdx) {
        ByteBuffer buffer = ByteBuffer.allocate(Short.BYTES);
        buffer.put(bytes, fromIdx, Short.BYTES).flip();
        return buffer.getShort();
    }

    public static byte[] fromInt(int value) {
        return ByteBuffer.allocate(Integer.BYTES).putInt(value).array();
    }

    public static int toInt(byte[] bytes) {
        return toInt(bytes, 0);
    }

    public static int toInt(byte[] bytes, int fromIdx) {
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
        buffer.put(bytes, fromIdx, Integer.BYTES).flip();
        return buffer.getInt();
    }

    /**
     * convert long number to byte array
     * @param number the long number
     * @return byte array
     */
    public static byte[] fromLong(long number) {
        return ByteBuffer.allocate(Long.BYTES).putLong(number).array();
    }

    /**
     * convert byte array to long number
     * @param bytes  the byte array
     * @param fromIdx the byte array offset
     * @return long number
     */
    public static long toLong(byte[] bytes, int fromIdx) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.put(bytes, fromIdx, Long.BYTES).flip();
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
        if (bytes == null || bytes.length == 0) {
            return BigInteger.ZERO;
        }
        return new BigInteger(1, bytes);
    }

    /**
     * merge byte arrays
     * @param first  first byte array of args
     * @param rest   others byte array
     * @return a new byte array of them
     */
    public static byte[] concat(byte[] first, byte[]... rest) {
        Preconditions.checkArgument(first != null, "the first cannot be null");
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

        /*ByteArrayOutputStream baos = new ByteArrayOutputStream(totalLength);
        try {
            baos.write(first);
            for (byte[] array : rest) {
                if (array != null) {
                    baos.write(array);
                }
            }
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e); // cannot happened
        }*/
    }

}
