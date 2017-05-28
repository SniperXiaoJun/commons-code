package code.ponfee.commons.jce;

import java.nio.charset.Charset;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;

import code.ponfee.commons.jce.crypto.Algorithm;
import code.ponfee.commons.jce.crypto.Encryptor;
import code.ponfee.commons.jce.crypto.EncryptorBuilder;
import code.ponfee.commons.jce.crypto.Mode;
import code.ponfee.commons.jce.crypto.Padding;
import code.ponfee.commons.jce.security.RSACryptor;

/**
 * 加解密服务提供
 * @author: fupf
 */
public abstract class CryptoProvider {
    private static final String DEFAULT_CHARSET = "UTF-8";

    public abstract byte[] encrypt(byte[] original);

    public abstract byte[] decrypt(byte[] encrypted);

    public final String encrypt(String plaintext) {
        return encrypt(plaintext, DEFAULT_CHARSET);
    }

    public final String encrypt(String plaintext, String charset) {
        byte[] original = plaintext.getBytes(Charset.forName(charset));
        return Base64.getUrlEncoder().encodeToString(encrypt(original));
    }

    public final String decrypt(String ciphertext) {
        return decrypt(ciphertext, DEFAULT_CHARSET);
    }

    public final String decrypt(String ciphertext, String charset) {
        byte[] original = decrypt(Base64.getUrlDecoder().decode(ciphertext));
        return new String(original, Charset.forName(charset));
    }

    /**
     * AES加/解密
     */
    public static final CryptoProvider AES_CRYPTO = new CryptoProvider() {
        private final Encryptor key = EncryptorBuilder.newBuilder(Algorithm.AES).key("z]_5Fi!X$ed4OY8j".getBytes()).mode(Mode.CBC)
                                                      .padding(Padding.PKCS5Padding).ivParameter("SVE<r[)qK`n%zQ'o".getBytes()).build();

        @Override
        public byte[] encrypt(byte[] original) {
            return key.encrypt(original);
        }

        @Override
        public byte[] decrypt(byte[] encrypted) {
            return key.decrypt(encrypted);
        }
    };

    /**
     * RSA加/解密
     */
    public static final CryptoProvider RSA_CRYPTO = new CryptoProvider() {
        private final RSAPublicKey pubKey = RSACryptor.parseB64PublicKey("MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAPaVNplmvsicFxdVUG91i+bpNOkXNowEWD5XFdCStMeHzF26Efa6TJaSfXK+AdcGyXQRGvB/pEoGyUThSrJpIRUCAwEAAQ==");
        private final RSAPrivateKey priKey = RSACryptor.parseB64Pkcs8PrivateKey("MIIBVAIBADANBgkqhkiG9w0BAQEFAASCAT4wggE6AgEAAkEA9pU2mWa+yJwXF1VQb3WL5uk06Rc2jARYPlcV0JK0x4fMXboR9rpMlpJ9cr4B1wbJdBEa8H+kSgbJROFKsmkhFQIDAQABAkAcGiNP1krV+BwVl66EFWRtW5ShH/kiefhImoos7BtYReN5WZyYyxFCAf2yjMJigq2GFm8qdkQK+c+E7Q3lY6zdAiEA/wVfy+wGQcFh3gdFKhaQ12fBYMCtywxZ3Edss0EmxBMCIQD3h4vfENmbIMH+PX5dAPbRfrBFcx77/MxFORMESN0bNwIgL5kJMD51TICTi6U/u4NKtWmgJjbQOT2s5/hMyYg3fBECIEqRc+qUKenYuXg80Dd2VeSQlMunPZtN8b+czQTKaomLAiEA02qUv/p1dT/jc2BDtp9bl8jDiWFg5FNFcH6bBDlwgts=");

        @Override
        public byte[] encrypt(byte[] original) {
            return RSACryptor.encrypt(original, pubKey); // 公钥加密
        }

        @Override
        public byte[] decrypt(byte[] encrypted) {
            return RSACryptor.decrypt(encrypted, priKey); // 私钥解密
        }
    };

    public static void main(String[] args) {
        String s = RSA_CRYPTO.encrypt("123");
        System.out.println(s);
        System.out.println(RSA_CRYPTO.decrypt(s));

        s = AES_CRYPTO.encrypt("123");
        System.out.println(s);
        System.out.println(AES_CRYPTO.decrypt(s));
    }
}
