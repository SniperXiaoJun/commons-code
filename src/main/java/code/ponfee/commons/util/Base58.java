package code.ponfee.commons.util;

import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * Base58 code：except number 0, uppercase letter I and O, lowercase latter l
 * Reference from internet
 * @author Ponfee
 */
public class Base58 {

    public static final char[] ALPHABET = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz".toCharArray();

    private static final Charset US_ASCII = Charset.forName("US-ASCII");

    private static final int[] INDEXES = new int[128];
    static {
        Arrays.fill(INDEXES, -1);
        for (int i = 0; i < ALPHABET.length; i++) {
            INDEXES[ALPHABET[i]] = i;
        }
    }

    /** 
     * Encodes the given bytes in base58. No checksum is appended.
     */
    public static String encode(byte[] data) {
        if (data.length == 0) {
            return "";
        }

        // Duplicate input 
        data = copyOfRange(data, 0, data.length);

        // Count leading zeroes.
        int zeroCount = 0;
        while (zeroCount < data.length && data[zeroCount] == 0) {
            ++zeroCount;
        }

        // The actual encoding.
        byte[] temp = new byte[data.length * 2];
        int j = temp.length;
        for (int startAt = zeroCount; startAt < data.length;) {
            byte mod = divmod58(data, startAt);
            if (data[startAt] == 0) {
                ++startAt;
            }
            temp[--j] = (byte) ALPHABET[mod];
        }

        // Strip extra '1' if there are some after decoding.
        while (j < temp.length && temp[j] == ALPHABET[0]) {
            ++j;
        }

        // Add as many leading '1' as there were leading zeros.
        while (--zeroCount >= 0) {
            temp[--j] = (byte) ALPHABET[0];
        }

        return new String(temp, j, temp.length - j, US_ASCII);
    }

    /**
     * decode base58 string
     */
    public static byte[] decode(String data) {
        if (data.length() == 0) {
            return new byte[0];
        }
        byte[] input58 = new byte[data.length()];

        // Transform the String to a base58 byte sequence  
        for (int i = 0; i < data.length(); ++i) {
            char c = data.charAt(i);
            int digit58 = -1;
            if (c >= 0 && c < 128) {
                digit58 = INDEXES[c];
            }
            if (digit58 < 0) {
                throw new IllegalArgumentException("Illegal character '" 
                                                 + c + "' at [" + i + "]");
            }
            input58[i] = (byte) digit58;
        }

        // Count leading zeroes  
        int zeroCount = 0;
        while (zeroCount < input58.length && input58[zeroCount] == 0) {
            ++zeroCount;
        }

        // The encoding  
        byte[] temp = new byte[data.length()];
        int j = temp.length;

        int startAt = zeroCount;
        while (startAt < input58.length) {
            byte mod = divmod256(input58, startAt);
            if (input58[startAt] == 0) {
                ++startAt;
            }

            temp[--j] = mod;
        }

        // Do no add extra leading zeroes, move j to first non null byte.
        while (j < temp.length && temp[j] == 0) {
            ++j;
        }

        return copyOfRange(temp, j - zeroCount, temp.length);
    }

    // number -> number / 58, returns number % 58
    private static byte divmod58(byte[] number, int startAt) {
        int remainder = 0;
        for (int i = startAt; i < number.length; i++) {
            int digit256 = (int) number[i] & 0xFF;
            int temp = remainder * 256 + digit256;
            number[i] = (byte) (temp / 58);
            remainder = temp % 58;
        }

        return (byte) remainder;
    }

    // number -> number / 256, returns number % 256
    private static byte divmod256(byte[] number58, int startAt) {
        int remainder = 0;
        for (int i = startAt; i < number58.length; i++) {
            int digit58 = (int) number58[i] & 0xFF;
            int temp = remainder * 58 + digit58;
            number58[i] = (byte) (temp / 256);
            remainder = temp % 256;
        }

        return (byte) remainder;
    }

    private static byte[] copyOfRange(byte[] source, int from, int to) {
        byte[] range = new byte[to - from];
        System.arraycopy(source, from, range, 0, range.length);
        return range;
    }

    public static void main(String[] args) {
        //System.out.println(encode(Files.toByteArray(MavenProjects.getMainJavaFile(Bytes.class))));

        byte[] b128 = new byte[16], b0 = new byte[16], b127 = new byte[16];
        Arrays.fill(b128, (byte) -128);
        Arrays.fill(b0, (byte) 0);
        Arrays.fill(b127, (byte) 127);

        System.out.println("==================base58==================");
        System.out.println(encode(b128));
        //System.out.println(encode(b0));
        //System.out.println(encode(b127));

        /*System.out.println("\n==================base64==================");
        System.out.println(Base64.getUrlEncoder().withoutPadding().encodeToString(b128));
        System.out.println(Base64.getUrlEncoder().withoutPadding().encodeToString(b0));
        System.out.println(Base64.getUrlEncoder().withoutPadding().encodeToString(b127));*/
    }
}