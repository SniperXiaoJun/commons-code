package code.ponfee.commons.jce.hash;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import code.ponfee.commons.util.Bytes;

/**
 * Hmac算法封装
 * @author fupf
 */
public final class HmacUtils {

    public static byte[] md5(byte[] key, byte[] data) {
        return encrypt(key, data, "HmacMD5");
    }

    public static String md5Hex(byte[] key, byte[] data) {
        return Bytes.hexEncode(md5(key, data));
    }

    public static byte[] sha1(byte[] key, byte[] data) {
        return encrypt(key, data, "HmacSHA1");
    }

    public static String sha1Hex(byte[] key, byte[] data) {
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

}
