package code.ponfee.commons.util;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Formatter;

import org.apache.commons.lang3.StringUtils;

/**
 * byte[]
 * @author fupf
 */
public final class Bytes {

    private static final char SINGLE_SPACE = ' ';
    private static final String HEX_CODES = "0123456789abcdef";
    private static final Charset US_ASCII = Charset.forName("US-ASCII");

    /** 随机数 */
    private static final SecureRandom RANDOM = new SecureRandom();
    static {
        RANDOM.setSeed(new SecureRandom(ObjectUtils.uuid32().getBytes()).generateSeed(20));
    }

    /**
     * dump byte array
     * @see org.apache.commons.io.HexDump#dump(byte[], long, java.io.OutputStream, int)
     * @see sun.misc.HexDumpEncoder#encode(byte[], java.io.OutputStream);
     * 
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
            baos.write(HEX_CODES.indexOf(hex.charAt(i)) << 4 | 
                       HEX_CODES.indexOf(hex.charAt(i + 1)));
        }
        return baos.toByteArray();
    }

    /*public static String base64EncodeUrlSafe(byte[] data) {
        String str = base64Encode(data);
        return StringUtils.replaceEach(str, new String[] { "+", "/", "=" }, new String[] { "-", "_", "" });
    }
    public static byte[] base64DecodeUrlSafe(String b64) {
        b64 = StringUtils.replaceEach(b64, new String[] { "-", "_" }, new String[] { "+", "/" });
        b64 += StringUtils.leftPad("", (4 - b64.length() % 4) % 4, '=');
        return base64Decode(b64);
    }*/

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

    /**
     * random byte[] array by SecureRandom
     * @param numOfByte
     * @return
     */
    public static byte[] randomBytes(int numOfByte) {
        byte[] bytes = new byte[numOfByte];
        RANDOM.nextBytes(bytes);
        return bytes;
    }

    public static void main(String[] args) throws Exception {
        System.out.println(hexDump(randomBytes(457)));

        System.out.println(Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes(20)));
        System.out.println(org.apache.commons.codec.binary.Base64.encodeBase64URLSafeString(randomBytes(20)));
    }
}
