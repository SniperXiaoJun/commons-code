package code.ponfee.commons.jce.security;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.SecretKey;
import javax.crypto.interfaces.DHKey;
import javax.crypto.interfaces.DHPrivateKey;
import javax.crypto.interfaces.DHPublicKey;

import com.google.common.collect.ImmutableMap;

/**
 * Diffie-Hellman加解密组件（一般用于密钥交换）
 * Key-Agreement
 * @author fupf
 */
public final class DHCryptor {

    private static final String ALGORITHM = "DH"; // DH算法名称
    private static final String SECRET_ALGORITHM = "DESede"; // 使用3DES对称加密
    private static final String PUBLIC_KEY = "DHPublicKey";
    private static final String PRIVATE_KEY = "DHPrivateKey";

    public static Map<String, DHKey> initPartAKey() {
        return initPartAKey(1024);
    }

    /**
     * 初始化甲方密钥
     * @param Keysize must be a multiple of 64, ranging from 512 to 1024 (inclusive).
     * @return
     */
    public static Map<String, DHKey> initPartAKey(int keySize) {
        KeyPairGenerator keyPairGenerator;
        try {
            keyPairGenerator = KeyPairGenerator.getInstance(ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            throw new SecurityException(e);
        }
        keyPairGenerator.initialize(keySize);
        KeyPair pair = keyPairGenerator.generateKeyPair();
        return ImmutableMap.of(PUBLIC_KEY, (DHKey) pair.getPublic(), // 甲方公钥
                               PRIVATE_KEY, (DHKey) pair.getPrivate()); // 甲方私钥
    }

    /**
     * 初始化乙方密钥
     * @param key 甲方公钥
     * @return
     */
    public static Map<String, DHKey> initPartBKey(byte[] partAPubKey) {
        return initPartBKey(getPublicKey(partAPubKey));
    }

    /**
     * 初始化乙方密钥
     * @param partAPublicKey 甲方公钥
     * @return
     */
    public static Map<String, DHKey> initPartBKey(DHPublicKey partAPublicKey) {
        // 由甲方公钥构建乙方密钥
        KeyPairGenerator keyPairGen;
        try {
            keyPairGen = KeyPairGenerator.getInstance(partAPublicKey.getAlgorithm());
            keyPairGen.initialize(partAPublicKey.getParams());
        } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException e) {
            throw new SecurityException(e);
        }

        KeyPair keyPair = keyPairGen.generateKeyPair();
        return ImmutableMap.of(PUBLIC_KEY, (DHKey) keyPair.getPublic(),  // 乙方公钥
                               PRIVATE_KEY, (DHKey) keyPair.getPrivate());  // 乙方私钥
    }

    /**
     * DHPublicKey convert to byte array
     * @param key the DHPublicKey
     * @return byte array encoded of DHPublicKey
     */
    public static byte[] encode(DHPublicKey key) {
        return key.getEncoded();
    }

    /**
     * DHPrivateKey convert to byte array
     * @param key the DHPrivateKey
     * @return byte array encoded of DHPrivateKey
     */
    public static byte[] encode(DHPrivateKey key) {
        return key.getEncoded();
    }

    /**
     * 取得私钥
     * @param keyMap
     * @return
     */
    public static DHPrivateKey getPrivateKey(Map<String, DHKey> keyMap) {
        return (DHPrivateKey) keyMap.get(PRIVATE_KEY);
    }

    /**
     * 取得公钥
     * @param keyMap
     * @return
     */
    public static DHPublicKey getPublicKey(Map<String, DHKey> keyMap) {
        return (DHPublicKey) keyMap.get(PUBLIC_KEY);
    }

    /**
     * 取得私钥
     * @param keyMap
     * @return
     */
    public static DHPrivateKey getPrivateKey(byte[] privateKey) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
            PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(privateKey);
            return (DHPrivateKey) keyFactory.generatePrivate(pkcs8KeySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new SecurityException(e);
        }
    }

    /**
     * 取得公钥
     * @param keyMap
     * @return
     */
    public static DHPublicKey getPublicKey(byte[] publicKey) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
            X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(publicKey);
            return (DHPublicKey) keyFactory.generatePublic(x509KeySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new SecurityException(e);
        }
    }

    /**
     * 加密<br>
     * @param data 待加密数据
     * @param secretKey 双方公私钥协商的对称密钥
     * @return
     */
    public static byte[] encrypt(byte[] data, SecretKey secretKey) {
        try {
            Cipher cipher = Cipher.getInstance(secretKey.getAlgorithm());
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return cipher.doFinal(data);
        } catch (Exception e) {
            throw new SecurityException(e);
        }
    }

    /**
     * 解密<br>
     * @param data 待解密数据
     * @param secretKey 双方公私钥协商的对称密钥
     * @return
     */
    public static byte[] decrypt(byte[] data, SecretKey secretKey) {
        try {
            Cipher cipher = Cipher.getInstance(secretKey.getAlgorithm());
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            return cipher.doFinal(data);
        } catch (Exception e) {
            throw new SecurityException(e);
        }
    }

    /**
     * 双方公私钥生成（协商）对称密钥
     * @param bPriKey
     * @param aPubKey
     * @return
     */
    public static SecretKey genSecretKey(byte[] bPriKey, byte[] aPubKey) {
        return genSecretKey(getPrivateKey(bPriKey), getPublicKey(aPubKey));
    }

    /**
     * 双方公私钥生成（协商）对称密钥
     * @param bPriKey
     * @param aPubKey
     * @return
     */
    public static SecretKey genSecretKey(DHPrivateKey bPriKey, DHPublicKey aPubKey) {
        try {
            KeyAgreement keyAgree = KeyAgreement.getInstance(aPubKey.getAlgorithm());
            keyAgree.init(bPriKey);
            keyAgree.doPhase(aPubKey, true);
            return keyAgree.generateSecret(SECRET_ALGORITHM); // 生成本地密钥
        } catch (InvalidKeyException | NoSuchAlgorithmException | IllegalStateException e) {
            throw new SecurityException(e);
        }
    }

    public static void main(String[] args) {
        Map<String, DHKey> partA = initPartAKey(1024);
        Map<String, DHKey> partB = initPartBKey(getPublicKey(partA));
        byte[] data = "123456".getBytes();

        // 乙方加密甲方解密
        byte[] encrypted = encrypt(data, genSecretKey(getPrivateKey(partB), getPublicKey(partA)));
        byte[] decrypted = decrypt(encrypted, genSecretKey(getPrivateKey(partA), getPublicKey(partB)));
        System.out.println(new String(decrypted));

        // 甲方加密乙方解密
        encrypted = encrypt(data, genSecretKey(getPrivateKey(partA), getPublicKey(partB)));
        decrypted = decrypt(encrypted, genSecretKey(getPrivateKey(partB), getPublicKey(partA)));
        System.out.println(new String(decrypted));
    }
}
