package code.ponfee.commons.jce.security;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.SecretKey;
import javax.crypto.interfaces.DHKey;
import javax.crypto.interfaces.DHPrivateKey;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHParameterSpec;

/**
 * DH安全编码组件
 */
public abstract class DHCryptor {
    private static final String ALGORITHM = "DH";
    /** DH加密下需要一种对称加密算法对数据加密，这里我们使用DESede，也可以使用其他对称加密算法。 */
    private static final String SECRET_ALGORITHM = "DESede";
    private static final String PUBLIC_KEY = "DHPublicKey";
    private static final String PRIVATE_KEY = "DHPrivateKey";

    public static Map<String, DHKey> initPartAKey() throws Exception {
        return initPartAKey(1024);
    }

    /**
     * 初始化甲方密钥
     * @param Keysize must be a multiple of 64, ranging from 512 to 1024 (inclusive).
     * @return
     * @throws Exception
     */
    public static Map<String, DHKey> initPartAKey(int keySize) throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(ALGORITHM);
        keyPairGenerator.initialize(keySize);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        Map<String, DHKey> keyMap = new HashMap<>(2);
        keyMap.put(PUBLIC_KEY, (DHKey) keyPair.getPublic()); // 甲方公钥
        keyMap.put(PRIVATE_KEY, (DHKey) keyPair.getPrivate()); // 甲方私钥
        return keyMap;
    }

    /**
     * 初始化乙方密钥
     * @param key 甲方公钥
     * @return
     * @throws Exception
     */
    public static Map<String, DHKey> initPartBKey(byte[] partAPubKeyBytes) throws Exception {
        // 解析甲方公钥
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(partAPubKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
        return initPartBKey((DHPublicKey) keyFactory.generatePublic(x509KeySpec));
    }

    /**
     * 初始化乙方密钥
     * @param partAPublicKey 甲方公钥
     * @return
     * @throws Exception
     */
    public static Map<String, DHKey> initPartBKey(DHPublicKey partAPublicKey) throws Exception {
        // 由甲方公钥构建乙方密钥
        DHParameterSpec dhParamSpec = partAPublicKey.getParams();
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance(partAPublicKey.getAlgorithm());
        keyPairGen.initialize(dhParamSpec);
        KeyPair keyPair = keyPairGen.generateKeyPair();

        Map<String, DHKey> keyMap = new HashMap<>(2);
        keyMap.put(PUBLIC_KEY, (DHKey) keyPair.getPublic()); // 乙方公钥
        keyMap.put(PRIVATE_KEY, (DHKey) keyPair.getPrivate()); // 乙方私钥
        return keyMap;
    }

    /**
     * 加密<br>
     * @param data 待加密数据
     * @param secretKey 双方公私钥协商的对称密钥
     * @return
     * @throws Exception
     */
    public static byte[] encrypt(byte[] data, SecretKey secretKey) throws Exception {
        Cipher cipher = Cipher.getInstance(secretKey.getAlgorithm());
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        return cipher.doFinal(data);
    }

    /**
     * 解密<br>
     * @param data 待解密数据
     * @param secretKey 双方公私钥协商的对称密钥
     * @return
     * @throws Exception
     */
    public static byte[] decrypt(byte[] data, SecretKey secretKey) throws Exception {
        Cipher cipher = Cipher.getInstance(secretKey.getAlgorithm());
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        return cipher.doFinal(data);
    }

    /**
     * 双方公私钥生成（协商）对称密钥
     * @param aPubKey
     * @param bPriKey
     * @return
     * @throws Exception
     */
    public static SecretKey getSecretKey(byte[] aPubKey, byte[] bPriKey) throws Exception {
        KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(aPubKey);
        DHPublicKey pubKey = (DHPublicKey) keyFactory.generatePublic(x509KeySpec);

        // 初始化私钥  
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(bPriKey);
        DHPrivateKey priKey = (DHPrivateKey) keyFactory.generatePrivate(pkcs8KeySpec);

        return getSecretKey(pubKey, priKey);
    }

    /**
     * 双方公私钥生成（协商）对称密钥
     * @param aPubKey
     * @param bPriKey
     * @return
     * @throws Exception
     */
    public static SecretKey getSecretKey(DHPublicKey aPubKey, DHPrivateKey bPriKey) throws Exception {
        KeyAgreement keyAgree = KeyAgreement.getInstance(aPubKey.getAlgorithm());
        keyAgree.init(bPriKey);
        keyAgree.doPhase(aPubKey, true);
        return keyAgree.generateSecret(SECRET_ALGORITHM); // 生成本地密钥
    }

    /**
     * 取得私钥
     * @param keyMap
     * @return
     * @throws Exception
     */
    public static DHPrivateKey getPrivateKey(Map<String, DHKey> keyMap) throws Exception {
        return (DHPrivateKey) keyMap.get(PRIVATE_KEY);
    }

    /**
     * 取得公钥
     * @param keyMap
     * @return
     * @throws Exception
     */
    public static DHPublicKey getPublicKey(Map<String, DHKey> keyMap) throws Exception {
        return (DHPublicKey) keyMap.get(PUBLIC_KEY);
    }

    public static void main(String[] args) throws Exception {
        Map<String, DHKey> partA = initPartAKey(1024);
        Map<String, DHKey> partB = initPartBKey(getPublicKey(partA));
        byte[] data = "123456".getBytes();

        // 乙方加密甲方解密
        byte[] encrypted = encrypt(data, getSecretKey(getPublicKey(partA), getPrivateKey(partB)));
        byte[] decrypted = decrypt(encrypted, getSecretKey(getPublicKey(partB), getPrivateKey(partA)));
        System.out.println(new String(decrypted));

        // 甲方加密乙方解密
        encrypted = encrypt(data, getSecretKey(getPublicKey(partB), getPrivateKey(partA)));
        decrypted = decrypt(encrypted, getSecretKey(getPublicKey(partA), getPrivateKey(partB)));
        System.out.println(new String(decrypted));
    }
}
