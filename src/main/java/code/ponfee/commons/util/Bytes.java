package code.ponfee.commons.util;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.security.SecureRandom;
import java.util.Formatter;

import org.apache.commons.lang3.StringUtils;

/**
 * byte[]
 * @author fupf
 */
public final class Bytes {
    private static final char SINGLE_SPACE = ' ';
    private static final String HEX = "0123456789abcdef";
    private static final Charset US_ASCII = Charset.forName("US-ASCII");
    private static final Charset ISO_8859_1 = Charset.forName("ISO-8859-1");

    private static final char[] BASE64_ENCODE_CHARS = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I',
        'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b',
        'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't',
        'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/' };

    private static final byte[] BASE64_DECODE_CHARS = { -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, 62, -1, -1, -1, 63, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, -1, -1,
        -1, -1, -1, -1, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21,
        22, 23, 24, 25, -1, -1, -1, -1, -1, -1, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39,
        40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, -1, -1, -1, -1, -1 };

    /**
     * 与HexDump、HexDumpEncoder功能相似（PS：早知有现成的就不用重复造轮子了）
     * @see new org.apache.commons.io.HexDump()#dump(byte[],0,System.out,bytes[].length);
     * @see new HexDumpEncoder()#encode((byte[], System.out);
     * dump byte array
     * @param bytes  字节数组
     * @param block  每块大小
     * @param chunk  每行块数
     * @return
     */
    public static String hexDump(byte[] bytes, int block, int chunk) {
        Formatter fmt = new Formatter();
        for (int i, j = 0, wid = block * chunk; j * wid < bytes.length; j++) {
            fmt.format("%05X: ", j * wid); // 输出行号：“00000: ”

            Formatter text = new Formatter(); // 右边文本
            for (i = 0; i < wid && (i + j * wid) < bytes.length; i++) {
                byte b = bytes[i + j * wid];

                fmt.format("%02X ", b); // 输出hex：“B1 ”
                if ((i + 1) % block == 0 || i + 1 == wid) {
                    fmt.format("%s", SINGLE_SPACE); // block与block间加一个空格
                }

                if (b >= 0x21 && b <= 0x7e) text.format("%c", b);
                else text.format("%c", '.'); // 非ascii码则输出“.”
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
     * byte[]转HEX
     * @param str
     * @return
     */
    public static String hexEncode(byte[] bytes) {
        //new BigInteger(1, bytes).toString(16);
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        // 将字节数组中每个字节拆解成2位16进制整数
        for (int i = 0; i < bytes.length; i++) {
            sb.append(HEX.charAt((bytes[i] & 0xf0) >> 4));
            sb.append(HEX.charAt((bytes[i] & 0x0f) >> 0));
        }
        return sb.toString();
    }

    /**
     * HEX转byte[]
     * @param hex
     * @return
     */
    public static byte[] hexDecode(String hex) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(hex.length() / 2);
        // 将每2位16进制整数组装成一个字节
        for (int i = 0, n = hex.length(); i < n; i += 2) {
            baos.write((HEX.indexOf(hex.charAt(i)) << 4 | HEX.indexOf(hex.charAt(i + 1))));
        }
        return baos.toByteArray();
    }

    /**
     * byte[]转base64
     * @param data
     * @return
     */
    public static String base64Encode(byte[] data) {
        StringBuilder builder = new StringBuilder(data.length * 4 / 3);
        for (int i = 0, len = data.length, b1, b2, b3; i < len;) {
            b1 = data[i++] & 0xff;
            if (i == len) {
                builder.append(BASE64_ENCODE_CHARS[b1 >>> 2]);
                builder.append(BASE64_ENCODE_CHARS[(b1 & 0x3) << 4]);
                builder.append("==");
                break;
            }
            b2 = data[i++] & 0xff;
            if (i == len) {
                builder.append(BASE64_ENCODE_CHARS[b1 >>> 2]);
                builder.append(BASE64_ENCODE_CHARS[((b1 & 0x03) << 4) | ((b2 & 0xf0) >>> 4)]);
                builder.append(BASE64_ENCODE_CHARS[(b2 & 0x0f) << 2]);
                builder.append("=");
                break;
            }
            b3 = data[i++] & 0xff;
            builder.append(BASE64_ENCODE_CHARS[b1 >>> 2]);
            builder.append(BASE64_ENCODE_CHARS[((b1 & 0x03) << 4) | ((b2 & 0xf0) >>> 4)]);
            builder.append(BASE64_ENCODE_CHARS[((b2 & 0x0f) << 2) | ((b3 & 0xc0) >>> 6)]);
            builder.append(BASE64_ENCODE_CHARS[b3 & 0x3f]);
        }
        return builder.toString();
    }

    /**
     * base64转byte[]
     * @param b64
     * @return
     */
    public static byte[] base64Decode(String b64) {
        byte[] data = b64.getBytes(US_ASCII);
        StringBuilder builder = new StringBuilder(data.length * 3 / 4);
        for (int i = 0, len = data.length, b1, b2, b3, b4; i < len;) {
            /* b1 */
            do {
                b1 = BASE64_DECODE_CHARS[data[i++]];
            } while (i < len && b1 == -1);
            if (b1 == -1) break;
            /* b2 */
            do {
                b2 = BASE64_DECODE_CHARS[data[i++]];
            } while (i < len && b2 == -1);
            if (b2 == -1) break;
            builder.append((char) ((b1 << 2) | ((b2 & 0x30) >>> 4)));
            /* b3 */
            do {
                b3 = data[i++];
                if (b3 == 61) return builder.toString().getBytes(ISO_8859_1);
                b3 = BASE64_DECODE_CHARS[b3];
            } while (i < len && b3 == -1);
            if (b3 == -1) break;
            builder.append((char) (((b2 & 0x0f) << 4) | ((b3 & 0x3c) >>> 2)));
            /* b4 */
            do {
                b4 = data[i++];
                if (b4 == 61) return builder.toString().getBytes(ISO_8859_1);
                b4 = BASE64_DECODE_CHARS[b4];
            } while (i < len && b4 == -1);
            if (b4 == -1) break;
            builder.append((char) (((b3 & 0x03) << 6) | b4));
        }
        return builder.toString().getBytes(ISO_8859_1);
    }

    public static String base64EncodeUrlSafe(byte[] data) {
        String str = base64Encode(data);
        return StringUtils.replaceEach(str, new String[] { "+", "/", "=" }, new String[] { "-", "_", "" });
    }

    public static byte[] base64DecodeUrlSafe(String b64) {
        b64 = StringUtils.replaceEach(b64, new String[] { "-", "_" }, new String[] { "+", "/" });
        b64 += StringUtils.leftPad("", (4 - b64.length() % 4) % 4, '=');
        return base64Decode(b64);
    }

    public static char[] bytes2chars(byte[] bytes) {
        return bytes2chars(bytes, US_ASCII.name());
    }

    public static char[] bytes2chars(byte[] bytes, String charset) {
        //return new String(bytes, Charset.forName(charset)).toCharArray();
        ByteBuffer buff = ByteBuffer.allocate(bytes.length);
        buff.put(bytes);
        buff.flip();
        return Charset.forName(charset).decode(buff).array();
    }

    public static byte[] chars2bytes(char[] chars) {
        return chars2bytes(chars, US_ASCII.name());
    }

    public static byte[] chars2bytes(char[] chars, String charset) {
        //return new String(chars).getBytes(Charset.forName(charset));
        CharBuffer cb = CharBuffer.allocate(chars.length);
        cb.put(chars);
        cb.flip();
        return Charset.forName(charset).encode(cb).array();
    }

    public static void main(String[] args) throws Exception {
        byte[] bytes = new byte[51];
        new SecureRandom().nextBytes(bytes);
        System.out.println(hexDump(bytes));
    }
}
