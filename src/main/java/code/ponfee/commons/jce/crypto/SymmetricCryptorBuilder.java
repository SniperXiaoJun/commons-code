package code.ponfee.commons.jce.crypto;

import java.security.GeneralSecurityException;
import java.security.Provider;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import code.ponfee.commons.util.SecureRandoms;

/**
 * 对称加密构建类
 * @author fupf
 */
public final class SymmetricCryptorBuilder {

    private final Algorithm algorithm; // 加密算法
    private byte[] key; // 密钥
    private Mode mode; // 分组加密模式
    private Padding padding; // 填充
    private IvParameterSpec iv; // 填充向量
    private Provider provider; // 加密服务提供方

    private SymmetricCryptorBuilder(Algorithm algorithm) {
        this.algorithm = algorithm;
    }

    public static SymmetricCryptorBuilder newBuilder(Algorithm algorithm) {
        return new SymmetricCryptorBuilder(algorithm);
    }

    public SymmetricCryptorBuilder key(byte[] key) {
        this.key = key;
        return this;
    }

    public SymmetricCryptorBuilder key(int keySize) {
        this.key = SecureRandoms.nextBytes(keySize);
        return this;
    }

    public SymmetricCryptorBuilder mode(Mode mode) {
        this.mode = mode;
        return this;
    }

    public SymmetricCryptorBuilder padding(Padding padding) {
        this.padding = padding;
        return this;
    }

    public SymmetricCryptorBuilder ivParameter(byte[] ivBytes) {
        this.iv = new IvParameterSpec(ivBytes);
        return this;
    }

    public SymmetricCryptorBuilder provider(Provider provider) {
        this.provider = provider;
        return this;
    }

    public SymmetricCryptor build() {
        if (mode != null && padding == null) {
            throw new IllegalArgumentException("padding cannot be null within mode crypto.");
        } else if (mode == null && padding != null) {
            throw new IllegalArgumentException("padding must be null without mode crypto.");
        }

        SecretKey secretKey;
        if (key == null) {
            try {
                secretKey = KeyGenerator.getInstance(algorithm.name()).generateKey();
            } catch (GeneralSecurityException e) {
                throw new SecurityException(e);
            }
        } else {
            secretKey = new SecretKeySpec(key, algorithm.name());
        }
        return new SymmetricCryptor(secretKey, mode, padding, iv, provider);
    }

}
