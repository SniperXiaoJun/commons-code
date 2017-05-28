package code.ponfee.commons.jce.crypto;

import java.security.GeneralSecurityException;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

/**
 * 对称加密
 * @author fupf
 */
public class Encryptor {

    /** 随机数 */
    private static final SecureRandom RANDOM = new SecureRandom();
    static {
        RANDOM.setSeed(new SecureRandom().generateSeed(20));
    }

    /** 
     * 分组对称加密模式时padding不能为null 
     */
    private final Mode mode;

    /** 
     * 1、RC2、RC4分组对称加密模式时padding必须为NoPadding
     * 2、无分组模式时padding必须为null
     * 3、其它算法无限制 
     */
    private final Padding padding;

    /** 
     * 1、ECB模式时iv必须为null
     * 2、无分组对称加密模式时iv必须为null
     * 3、有分组对称加密模式时必须要有iv
     */
    protected final AlgorithmParameterSpec parameter;

    /** 加密提供方 */
    private final Provider provider;

    /** 密钥 */
    private final SecretKey secretKey;

    protected Encryptor(SecretKey secretKey, Mode mode, Padding padding, AlgorithmParameterSpec parameter, Provider provider) {
        this.secretKey = secretKey;
        this.mode = mode;
        this.padding = padding;
        this.parameter = parameter;
        this.provider = provider;
    }

    public final byte[] encrypt(byte[] data) {
        return this.docrypt(data, Cipher.ENCRYPT_MODE);
    }

    public final byte[] decrypt(byte[] encrypted) {
        return this.docrypt(encrypted, Cipher.DECRYPT_MODE);
    }

    private byte[] docrypt(byte[] bytes, int cryptMode) {
        StringBuilder transformation = new StringBuilder(getAlgorithm());
        if (mode != null) {
            transformation.append("/").append(mode.name());
            transformation.append("/").append(padding.name());
        }
        try {
            Cipher cipher = null;
            if (provider == null) {
                cipher = Cipher.getInstance(transformation.toString());
            } else {
                cipher = Cipher.getInstance(transformation.toString(), provider);
            }
            cipher.init(cryptMode, secretKey, parameter);
            return cipher.doFinal(bytes);
        } catch (GeneralSecurityException e) {
            throw new SecurityException(e);
        }
    }

    // -----------------getter-----------------
    /**
     * get encrypt algorithm string
     * @return
     */
    public final String getAlgorithm() {
        return secretKey.getAlgorithm();
    }

    /**
     * get key byte[] data
     * @return
     */
    public final byte[] getKey() {
        return secretKey.getEncoded();
    }

    /**
     * get iv parameter byte[] data
     * @return
     */
    public byte[] getParameter() {
        return ((IvParameterSpec) parameter).getIV();
    }

    /**
     * random byte[] array by SecureRandom
     * @param numBytes
     * @return
     */
    public static byte[] randomBytes(int numBytes) {
        byte[] bytes = new byte[numBytes];
        RANDOM.nextBytes(bytes);
        return bytes;
    }

}
