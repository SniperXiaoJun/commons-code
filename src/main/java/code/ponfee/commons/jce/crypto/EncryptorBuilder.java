package code.ponfee.commons.jce.crypto;

import java.security.GeneralSecurityException;
import java.security.Provider;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * 加密构建类
 * @author fupf
 */
public final class EncryptorBuilder {

    private Algorithm algorithm; // 加密算法
    private byte[] key; // 密钥
    private Mode mode; // 分组加密模式
    private Padding padding; // 填充
    private IvParameterSpec iv; // 填充向量
    private Provider provider; // 加密服务提供方

    private EncryptorBuilder() {};

    public static EncryptorBuilder newBuilder(Algorithm algorithm) {
        EncryptorBuilder builder = new EncryptorBuilder();
        builder.algorithm = algorithm;
        return builder;
    }

    public EncryptorBuilder key(byte[] key) {
        this.key = key;
        return this;
    }

    public EncryptorBuilder key(int keySize) {
        this.key = Encryptor.randomBytes(keySize);
        return this;
    }

    public EncryptorBuilder mode(Mode mode) {
        this.mode = mode;
        return this;
    }

    public EncryptorBuilder padding(Padding padding) {
        this.padding = padding;
        return this;
    }

    public EncryptorBuilder ivParameter(byte[] ivBytes) {
        this.iv = new IvParameterSpec(ivBytes);
        return this;
    }

    public EncryptorBuilder provider(Provider provider) {
        this.provider = provider;
        return this;
    }

    public Encryptor build() {
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
        return new Encryptor(secretKey, mode, padding, iv, provider);
    }

}
