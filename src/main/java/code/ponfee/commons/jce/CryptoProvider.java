package code.ponfee.commons.jce;

import java.nio.charset.Charset;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;

import com.google.common.base.Preconditions;

import code.ponfee.commons.io.Files;
import code.ponfee.commons.jce.crypto.Algorithm;
import code.ponfee.commons.jce.crypto.Mode;
import code.ponfee.commons.jce.crypto.Padding;
import code.ponfee.commons.jce.crypto.SymmetricCryptor;
import code.ponfee.commons.jce.crypto.SymmetricCryptorBuilder;
import code.ponfee.commons.jce.security.RSACryptor;
import code.ponfee.commons.jce.security.RSAPrivateKeys;
import code.ponfee.commons.util.MavenProjects;

/**
 * 加解密服务提供
 * @author fupf
 */
public abstract class CryptoProvider {

    /**
     * 数据加密
     * @param original  原文
     * @return
     */
    public abstract byte[] encrypt(byte[] original);

    /**
     * 数据解密
     * @param encrypted  密文
     * @return
     */
    public abstract byte[] decrypt(byte[] encrypted);

    /**
     * 字符串数据加密
     * @param plaintext  明文
     * @return
     */
    public final String encrypt(String plaintext) {
        return encrypt(plaintext, Files.SYSTEM_CHARSET);
    }

    /**
     * 字符串数据加密
     * @param plaintext 明文
     * @param charset   字符串编码
     * @return
     */
    public final String encrypt(String plaintext, String charset) {
        if (plaintext == null) {
            return null;
        }

        byte[] original = plaintext.getBytes(Charset.forName(charset));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(encrypt(original));
    }

    /**
     * 数据解密
     * @param ciphertext  密文数据的base64编码
     * @return
     */
    public final String decrypt(String ciphertext) {
        return decrypt(ciphertext, Files.SYSTEM_CHARSET);
    }

    /**
     * 数据解密
     * @param ciphertext  密文数据的base64编码
     * @param charset     明文字符串编码
     * @return
     */
    public final String decrypt(String ciphertext, String charset) {
        if (ciphertext == null) {
            return null;
        }

        byte[] original = decrypt(Base64.getUrlDecoder().decode(ciphertext));
        return new String(original, Charset.forName(charset));
    }

    /**
     * AES加/解密
     */
    public static final CryptoProvider AES_CRYPTO = new CryptoProvider() {
        private final SymmetricCryptor key = SymmetricCryptorBuilder.newBuilder(Algorithm.AES)
                                                                    .key("z]_5Fi!X$ed4OY8j".getBytes())
                                                                    .mode(Mode.CBC).ivParameter("SVE<r[)qK`n%zQ'o".getBytes())
                                                                    .padding(Padding.PKCS7Padding).provider(Providers.BC)
                                                                    .build();

        @Override
        public byte[] encrypt(byte[] original) {
            Preconditions.checkArgument(original != null);
            return key.encrypt(original);
        }

        @Override
        public byte[] decrypt(byte[] encrypted) {
            Preconditions.checkArgument(encrypted != null);
            return key.decrypt(encrypted);
        }
    };

    /**
     * RSA加/解密
     */
    public static final CryptoProvider RSA_CRYPTO = new CryptoProvider() {
        private final RSAPrivateKey priKey = RSAPrivateKeys.fromPkcs8("MIIBVAIBADANBgkqhkiG9w0BAQEFAASCAT4wggE6AgEAAkEA9pU2mWa+yJwXF1VQb3WL5uk06Rc2jARYPlcV0JK0x4fMXboR9rpMlpJ9cr4B1wbJdBEa8H+kSgbJROFKsmkhFQIDAQABAkAcGiNP1krV+BwVl66EFWRtW5ShH/kiefhImoos7BtYReN5WZyYyxFCAf2yjMJigq2GFm8qdkQK+c+E7Q3lY6zdAiEA/wVfy+wGQcFh3gdFKhaQ12fBYMCtywxZ3Edss0EmxBMCIQD3h4vfENmbIMH+PX5dAPbRfrBFcx77/MxFORMESN0bNwIgL5kJMD51TICTi6U/u4NKtWmgJjbQOT2s5/hMyYg3fBECIEqRc+qUKenYuXg80Dd2VeSQlMunPZtN8b+czQTKaomLAiEA02qUv/p1dT/jc2BDtp9bl8jDiWFg5FNFcH6bBDlwgts=");
        private final RSAPublicKey pubKey = RSAPrivateKeys.extractPublicKey(priKey);

        @Override
        public byte[] encrypt(byte[] original) {
            Preconditions.checkArgument(original != null);
            return RSACryptor.encrypt(original, pubKey); // 公钥加密
        }

        @Override
        public byte[] decrypt(byte[] encrypted) {
            Preconditions.checkArgument(encrypted != null);
            return RSACryptor.decrypt(encrypted, priKey); // 私钥解密
        }
    };

    public static void main(String[] args) {
        String str = Files.toString(MavenProjects.getMainJavaFile(CryptoProvider.class));
        String data = RSA_CRYPTO.encrypt(str);
        System.out.println(data);
        System.out.println(RSA_CRYPTO.decrypt(data));

        System.out.println("======================================================");
        data = AES_CRYPTO.encrypt(str);
        System.out.println(data);
        System.out.println(AES_CRYPTO.decrypt(data));
    }
}
