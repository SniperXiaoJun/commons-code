package code.ponfee.commons.jce.hash;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import code.ponfee.commons.io.Files;
import code.ponfee.commons.util.Bytes;

/**
 * hash算法封装
 * @author fupf
 */
public final class HashUtils {

    private static final int BUF_SIZE = 4096;

    public static byte[] md5(InputStream input) {
        return digest(input, "MD5");
    }

    public static byte[] md5(byte[] data) {
        return digest(data, "MD5");
    }

    public static String md5Hex(InputStream input) {
        return Bytes.hexEncode(md5(input));
    }

    public static String md5Hex(byte[] data) {
        return Bytes.hexEncode(md5(data));
    }

    public static String md5Hex(String data) {
        return md5Hex(data.getBytes());
    }

    public static String md5Hex(String data, String charset) {
        return md5Hex(data.getBytes(Charset.forName(charset)));
    }

    public static byte[] sha1(InputStream input) {
        return digest(input, "SHA-1");
    }

    public static byte[] sha1(byte[] data) {
        return digest(data, "SHA-1");
    }

    public static String sha1Hex(InputStream input) {
        return Bytes.hexEncode(sha1(input));
    }

    public static String sha1Hex(byte[] data) {
        return Bytes.hexEncode(sha1(data));
    }

    public static String sha1Hex(String data) {
        return sha1Hex(data.getBytes());
    }

    public static String sha1Hex(String data, String charset) {
        return sha1Hex(data.getBytes(Charset.forName(charset)));
    }

    public static byte[] sha256(byte[] data) {
        return digest(data, "SHA-256");
    }

    public static String sha256Hex(byte[] data) {
        return Bytes.hexEncode(sha256(data));
    }

    public static byte[] sha384(byte[] data) {
        return digest(data, "SHA-384");
    }

    public static String sha384Hex(byte[] data) {
        return Bytes.hexEncode(sha384(data));
    }

    public static byte[] sha512(byte[] data) {
        return digest(data, "SHA-512");
    }

    public static String sha512Hex(byte[] data) {
        return Bytes.hexEncode(sha512(data));
    }

    // ---------------------------------------private methods---------------------------------------
    /**
     * 数据摘要
     * @param data      hash data of byte array
     * @param algorithm hash算法
     * @return
     */
    private static byte[] digest(byte[] data, String algorithm) {
        try {
            return MessageDigest.getInstance(algorithm).digest(data);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * 数据摘要
     * @param data      hash data of input stream
     * @param algorithm hash 算法
     * @return
     */
    private static byte[] digest(InputStream input, String algorithm) {
        /*try {
            byte[] buffer = new byte[BUF_SIZE];
            MessageDigest md = MessageDigest.getInstance(algorithm);
            int len;
            while ((len = input.read(buffer)) != Files.EOF) {
                md.update(buffer, 0, len);
            }
            return md.digest();
        } catch (NoSuchAlgorithmException | IOException e) {
            throw new IllegalArgumentException(e);
        } finally {
            if (input != null) try {
                input.close();
            } catch (IOException ignored) {
                ignored.printStackTrace();
            }
        }*/

        DigestInputStream digestInput = null;
        try {
            MessageDigest md = MessageDigest.getInstance(algorithm);
            digestInput = new DigestInputStream(input, md);
            byte[] buffer = new byte[BUF_SIZE];
            while (digestInput.read(buffer) != Files.EOF) {
                //  nothing to do
            }
            return md.digest();
        } catch (NoSuchAlgorithmException | IOException e) {
            throw new IllegalArgumentException(e);
        } finally {
            if (digestInput != null) try {
                digestInput.close();
            } catch (IOException ignored) {
                ignored.printStackTrace();
            }
            if (input != null) try {
                input.close();
            } catch (IOException ignored) {
                ignored.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws Exception {
        //System.out.println(ObjectUtils.toString(shortText("http://www.manong5.com/102542001/")));
        long start = System.currentTimeMillis();
        System.out.println(sha1Hex(new FileInputStream("E:\\tools\\develop\\linux\\CentOS-6.6-x86_64-bin-DVD1.iso")));
        System.out.println((System.currentTimeMillis() - start) / 1000);
    }
}
