package code.ponfee.commons.jce.hash;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import code.ponfee.commons.io.Files;
import code.ponfee.commons.util.Bytes;
import code.ponfee.commons.util.MavenProjects;
import code.ponfee.commons.util.SecureRandoms;

/**
 * Hmac算法封装
 * @author fupf
 */
public final class HmacUtils {
    private static final int BUF_SIZE = 4096;

    public static byte[] md5(byte[] key, byte[] data) {
        return encrypt(key, data, "HmacMD5");
    }

    public static String md5Hex(byte[] key, byte[] data) {
        return Bytes.hexEncode(md5(key, data));
    }

    public static byte[] sha1(byte[] key, byte[] data) {
        return encrypt(key, data, "HmacSHA1");
    }

    public static byte[] sha1(byte[] key, InputStream data) {
        return encrypt(key, data, "HmacSHA1");
    }

    public static String sha1Hex(byte[] key, byte[] data) {
        return Bytes.hexEncode(sha1(key, data));
    }

    public static String sha1Hex(byte[] key, InputStream data) {
        return Bytes.hexEncode(sha1(key, data));
    }

    public static byte[] sha384(byte[] key, byte[] data) {
        return encrypt(key, data, "HmacSHA384");
    }

    public static String sha384Hex(byte[] key, byte[] data) {
        return Bytes.hexEncode(sha384(key, data));
    }

    public static byte[] sha256(byte[] key, byte[] data) {
        return encrypt(key, data, "HmacSHA256");
    }

    public static String sha256Hex(byte[] key, byte[] data) {
        return Bytes.hexEncode(sha256(key, data));
    }

    public static byte[] sha512(byte[] key, byte[] data) {
        return encrypt(key, data, "HmacSHA512");
    }

    public static String sha512Hex(byte[] key, byte[] data) {
        return Bytes.hexEncode(sha512(key, data));
    }

    private static byte[] encrypt(byte[] key, byte[] data, String algName) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(key, algName);
            Mac mac = Mac.getInstance(secretKey.getAlgorithm());
            mac.init(secretKey);
            return mac.doFinal(data);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException("unknown slgorithm:" + algName);
        } catch (InvalidKeyException e) {
            throw new IllegalArgumentException("invalid key:" + Bytes.hexEncode(key));
        }
    }

    private static byte[] encrypt(byte[] key, InputStream input, String algName) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(key, algName);
            Mac mac = Mac.getInstance(secretKey.getAlgorithm());
            mac.init(secretKey);
            byte[] buffer = new byte[BUF_SIZE];
            int len;
            while ((len = input.read(buffer)) != Files.EOF) {
                mac.update(buffer, 0, len);
            }
            return mac.doFinal();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException("unknown slgorithm:" + algName);
        } catch (InvalidKeyException e) {
            throw new IllegalArgumentException("invalid key:" + Bytes.hexEncode(key));
        } catch (IOException e) {
            throw new IllegalArgumentException("read data error:" + e);
        } finally {
            if (input != null) try {
                input.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws FileNotFoundException {
        byte[] key = SecureRandoms.nextBytes(10);
        System.out.println(sha1Hex(key, new FileInputStream(MavenProjects.getMainJavaFile(HmacUtils.class))));
        System.out.println(sha1Hex(key, new FileInputStream(MavenProjects.getMainJavaFile(HmacUtils.class))));
        System.out.println(sha1Hex(key, new FileInputStream(MavenProjects.getMainJavaFile(HmacUtils.class))));
        System.out.println(sha1Hex(key, new FileInputStream(MavenProjects.getMainJavaFile(HmacUtils.class))));
        System.out.println(sha1Hex(key, new FileInputStream(MavenProjects.getMainJavaFile(HmacUtils.class))));
    }
}
