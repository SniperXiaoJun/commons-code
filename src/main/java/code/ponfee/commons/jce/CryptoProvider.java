/* __________              _____                                          *\
** \______   \____   _____/ ____\____   ____        Ponfee's code         **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \       (c) 2017-2018, MIT    **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/       http://www.ponfee.cn  **
**  |____|   \____/|___|  /__|  \___  >\___  >                            **
**                      \/          \/     \/                             **
\*                                                                        */

package code.ponfee.commons.jce;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Preconditions;

import code.ponfee.commons.io.Files;
import code.ponfee.commons.jce.security.RSACryptor;
import code.ponfee.commons.jce.security.RSAPrivateKeys;
import code.ponfee.commons.jce.security.RSAPublicKeys;
import code.ponfee.commons.jce.sm.SM2;
import code.ponfee.commons.jce.symmetric.SymmetricCryptor;

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
     * sign the data
     * @param data
     * @return signature
     */
    public byte[] sign(byte[] data) {
        throw new UnsupportedOperationException("cannot support sign.");
    }

    /**
     * verify the data signature
     * @param data
     * @param signed
     * @return true|false
     */
    public boolean verify(byte[] data, byte[] signed) {
        throw new UnsupportedOperationException("cannot support verify signature.");
    }

    /**
     * 字符串数据加密
     * @param plaintext  明文
     * @return
     */
    public final String encrypt(String plaintext) {
        return encrypt(plaintext, StandardCharsets.UTF_8);
    }

    /**
     * 字符串数据加密
     * @param plaintext 明文
     * @param charset   字符串编码
     * @return
     */
    public final String encrypt(String plaintext, Charset charset) {
        if (plaintext == null) {
            return null;
        }

        return Base64.getUrlEncoder().withoutPadding().encodeToString(
            this.encrypt(plaintext.getBytes(charset))
        );
    }

    /**
     * 数据解密
     * @param ciphertext  密文数据的base64编码
     * @return
     */
    public final String decrypt(String ciphertext) {
        return decrypt(ciphertext, StandardCharsets.UTF_8);
    }

    /**
     * 数据解密
     * @param ciphertext  密文数据的base64编码
     * @param charset     明文字符串编码
     * @return
     */
    public final String decrypt(String ciphertext, Charset charset) {
        if (ciphertext == null) {
            return null;
        }

        return new String(
            decrypt(Base64.getUrlDecoder().decode(ciphertext)), 
            charset
        );
    }

    /**
     * sign of data
     * @param data
     * @return
     */
    public final String sign(String data) {
        return sign(data, Files.UTF_8);
    }

    /**
     * data
     * @param data
     * @param charset
     * @return
     */
    public final String sign(String data, String charset) {
        if (StringUtils.isEmpty(data)) {
            return null;
        }
        return Base64.getUrlEncoder().withoutPadding().encodeToString(
            sign(data.getBytes(Charset.forName(charset)))
        );
    }

    /**
     * verify the data
     * @param data
     * @param signed
     * @return
     */
    public final boolean verify(String data, String signed) {
        return verify(data, Files.UTF_8, signed);
    }

    /**
     * verify the data
     * @param data
     * @param charset
     * @param signed
     * @return
     */
    public final boolean verify(String data, String charset, String signed) {
        return verify(data.getBytes(Charset.forName(charset)), 
            Base64.getUrlDecoder().decode(signed)
        );
    }

    /**
     * 对称密钥组件
     * @param symmetricKey {@link SymmetricCryptor}
     * @return
     */
    public static CryptoProvider symmetricKeyProvider(final SymmetricCryptor symmetricKey) {
        // the symmetricKey is thread-safe
        return new CryptoProvider() {
            @Override
            public byte[] encrypt(byte[] original) {
                Preconditions.checkArgument(original != null);
                return symmetricKey.encrypt(original);
            }

            @Override
            public byte[] decrypt(byte[] encrypted) {
                Preconditions.checkArgument(encrypted != null);
                return symmetricKey.decrypt(encrypted);
            }
        };
    }

    /**
     * RSA private key密钥组件
     * forbid use private key encrypt and use public key decrypt
     * 
     * @param pkcs8PrivateKey  the string of pkcs8 private key format
     * @return
     */
    public static CryptoProvider rsaPrivateKeyProvider(final String pkcs8PrivateKey) {
        return new CryptoProvider() {
            private final RSAPrivateKey priKey = RSAPrivateKeys.fromPkcs8(pkcs8PrivateKey); // thread-safe
            private final RSAPublicKey  pubKey = RSAPrivateKeys.extractPublicKey(priKey); // thread-safe

            @Override
            public byte[] encrypt(byte[] original) {
                Preconditions.checkArgument(original != null);
                // only support public key encrypt
                // forbid encrypt with private key
                return RSACryptor.encrypt(original, pubKey); // 公钥加密
            }

            @Override
            public byte[] decrypt(byte[] encrypted) {
                Preconditions.checkArgument(encrypted != null);
                // only support private key decrypt
                return RSACryptor.decrypt(encrypted, priKey); // 私钥解密
            }

            @Override
            public byte[] sign(byte[] data) {
                return RSACryptor.signSha1(data, priKey);
            }

            @Override
            public boolean verify(byte[] data, byte[] signed) {
                return RSACryptor.verifySha1(data, pubKey, signed);
            }
        };
    }

    /**
     * rsa public key密钥组件
     * @param pkcs8PublicKey  the string of pkcs8 public key format
     * @return
     */
    public static CryptoProvider rsaPublicKeyProvider(final String pkcs8PublicKey) {
        return new CryptoProvider() {
            private final RSAPublicKey pubKey = RSAPublicKeys.fromPkcs8(pkcs8PublicKey); // thread-safe

            @Override
            public byte[] encrypt(byte[] original) {
                Preconditions.checkArgument(original != null);
                return RSACryptor.encrypt(original, pubKey); // 公钥加密
            }

            @Override
            public byte[] decrypt(byte[] encrypted) {
                // cannot support public key decrypt
                throw new UnsupportedOperationException("cannot support decrypt.");
            }

            @Override
            public boolean verify(byte[] data, byte[] signed) {
                return RSACryptor.verifySha1(data, pubKey, signed);
            }
        };
    }

    public static CryptoProvider sm2PublicKeyProvider(final byte[] publicKey) {
        return sm2PublicKeyProvider(ECParameters.SM2_BEST, publicKey);
    }

    public static CryptoProvider sm2PublicKeyProvider(final ECParameters ecParameter, 
                                                      final byte[] publicKey) {
        return new CryptoProvider() {
            @Override
            public byte[] encrypt(byte[] original) {
                return SM2.encrypt(ecParameter, publicKey, original); // 公钥加密
            }

            @Override
            public byte[] decrypt(byte[] encrypted) {
                throw new UnsupportedOperationException("cannot support decrypt.");
            }

            @Override
            public boolean verify(byte[] data, byte[] signed) {
                return SM2.verify(ecParameter, data, signed, publicKey);
            }
        };
    }

    public static CryptoProvider sm2PrivateKeyProvider(final byte[] publicKey, 
                                                       final byte[] privateKey) {
        return sm2PrivateKeyProvider(ECParameters.SM2_BEST, publicKey, privateKey);
    }

    public static CryptoProvider sm2PrivateKeyProvider(final ECParameters ecParameter,
                                                       final byte[] publicKey, 
                                                       final byte[] privateKey) {
        return new CryptoProvider() {
            @Override
            public byte[] encrypt(byte[] original) {
                return SM2.encrypt(ecParameter, publicKey, original); // 公钥加密
            }

            @Override
            public byte[] decrypt(byte[] encrypted) { // 私钥解密
                return SM2.decrypt(ecParameter, privateKey, encrypted);
            }

            @Override
            public byte[] sign(byte[] data) { // sign data by SM3WithSM2
                return SM2.sign(ecParameter, data, publicKey, privateKey);
            }

            @Override
            public boolean verify(byte[] data, byte[] signed) { // verify the SM3WithSM2 signature
                return SM2.verify(ecParameter, data, signed, publicKey);
            }
        };
    }

}
