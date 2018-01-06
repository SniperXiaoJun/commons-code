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
import code.ponfee.commons.jce.HmacAlgorithm;
import code.ponfee.commons.util.Bytes;
import code.ponfee.commons.util.MavenProjects;
import code.ponfee.commons.util.SecureRandoms;

/**
 * Hmac算法封装
 * @author fupf
 */
public final class HmacUtils {
    private static final int BUF_SIZE = 4096;

    public static byte[] sha1(byte[] key, byte[] data) {
        return crypt(key, data, HmacAlgorithm.HmacSHA1.name());
    }

    public static byte[] sha1(byte[] key, InputStream data) {
        return crypt(key, data, HmacAlgorithm.HmacSHA1.name());
    }

    public static String sha1Hex(byte[] key, byte[] data) {
        return Bytes.hexEncode(sha1(key, data));
    }

    public static String sha1Hex(byte[] key, InputStream data) {
        return Bytes.hexEncode(sha1(key, data));
    }

    public static byte[] md5(byte[] key, byte[] data) {
        return crypt(key, data, HmacAlgorithm.HmacMD5.name());
    }

    public static String md5Hex(byte[] key, byte[] data) {
        return Bytes.hexEncode(md5(key, data));
    }

    public static byte[] sha224(byte[] key, byte[] data) {
        return crypt(key, data, HmacAlgorithm.HmacSHA224.name());
    }

    public static String sha224Hex(byte[] key, byte[] data) {
        return Bytes.hexEncode(sha224(key, data));
    }

    public static byte[] sha256(byte[] key, byte[] data) {
        return crypt(key, data, HmacAlgorithm.HmacSHA256.name());
    }

    public static String sha256Hex(byte[] key, byte[] data) {
        return Bytes.hexEncode(sha256(key, data));
    }

    public static byte[] sha384(byte[] key, byte[] data) {
        return crypt(key, data, HmacAlgorithm.HmacSHA384.name());
    }

    public static String sha384Hex(byte[] key, byte[] data) {
        return Bytes.hexEncode(sha384(key, data));
    }

    public static byte[] sha512(byte[] key, byte[] data) {
        return crypt(key, data, HmacAlgorithm.HmacSHA512.name());
    }

    public static String sha512Hex(byte[] key, byte[] data) {
        return Bytes.hexEncode(sha512(key, data));
    }

    public static Mac getInitializedMac(String algorithm, byte[] key) {
        if (key == null) {
            throw new IllegalArgumentException("Null key");
        }

        try {
            SecretKeySpec keySpec = new SecretKeySpec(key, algorithm);
            Mac mac = Mac.getInstance(algorithm);
            mac.init(keySpec);
            return mac;
        } catch (final NoSuchAlgorithmException e) {
            throw new IllegalArgumentException("unknown slgorithm:" + algorithm);
        } catch (final InvalidKeyException e) {
            throw new IllegalArgumentException("invalid key:" + Bytes.hexEncode(key));
        }
    }

    // ------------------------private methods-------------------------
    private static byte[] crypt(byte[] key, byte[] data, String algName) {
        return getInitializedMac(algName, key).doFinal(data);
    }

    private static byte[] crypt(byte[] key, InputStream input, String algName) {
        try {
            Mac mac = getInitializedMac(algName, key);
            byte[] buffer = new byte[BUF_SIZE];
            int len;
            while ((len = input.read(buffer)) != Files.EOF) {
                mac.update(buffer, 0, len);
            }
            return mac.doFinal();
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
        byte[] key = SecureRandoms.nextBytes(16);
        System.out.println(sha1Hex(key, new FileInputStream(MavenProjects.getMainJavaFile(HmacUtils.class))));
        System.out.println(sha1Hex(key, new FileInputStream(MavenProjects.getMainJavaFile(HmacUtils.class))));
    }
}
