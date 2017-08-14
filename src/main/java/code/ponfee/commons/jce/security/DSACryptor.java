package code.ponfee.commons.jce.security;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.interfaces.DSAKey;
import java.security.interfaces.DSAPrivateKey;
import java.security.interfaces.DSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Map;

import com.google.common.collect.ImmutableMap;

import code.ponfee.commons.util.ObjectUtils;

/**
 * DSA签名/验签
 * 只用于数字签名
 */
public abstract class DSACryptor {

    private static final String ALGORITHM = "DSA";
    private static final String PUBLIC_KEY = "DSAPublicKey";
    private static final String PRIVATE_KEY = "DSAPrivateKey";

    /**
     * 默认生成密钥
     * @return 密钥对象
     * @throws Exception
     */
    public static Map<String, DSAKey> initKey() throws Exception {
        return initKey(ObjectUtils.uuid32(), 1024);
    }

    /**
     * 生成密钥
     * @param seed 种子
     * @param keySize 默认密钥字节数must be a multiple of 64, ranging from 512 to 1024 (inclusive).
     * @return 密钥对象
     * @throws Exception
     */
    public static Map<String, DSAKey> initKey(String seed, int keySize) throws Exception {
        KeyPairGenerator keygen = KeyPairGenerator.getInstance(ALGORITHM);
        // 初始化随机产生器
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.setSeed(seed.getBytes());
        keygen.initialize(keySize, secureRandom);
        KeyPair pair = keygen.genKeyPair();
        return ImmutableMap.of(PUBLIC_KEY, (DSAKey) pair.getPublic(), PRIVATE_KEY, (DSAKey) pair.getPrivate());
    }

    /**
     * 取得私钥
     * @param keyMap
     * @return
     * @throws Exception
     */
    public static DSAPrivateKey getPrivateKey(Map<String, DSAKey> keyMap) throws Exception {
        return (DSAPrivateKey) keyMap.get(PRIVATE_KEY);
    }

    public static DSAPrivateKey getPrivateKey(byte[] priKeyBytes) throws Exception {
        // 构造PKCS8EncodedKeySpec对象
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(priKeyBytes);
        // KEY_ALGORITHM 指定的加密算法
        KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
        return (DSAPrivateKey) keyFactory.generatePrivate(pkcs8KeySpec);
    }

    /**
     * 取得公钥
     * @param keyMap
     * @return
     * @throws Exception
     */
    public static DSAPublicKey getPublicKey(Map<String, DSAKey> keyMap) throws Exception {
        return (DSAPublicKey) keyMap.get(PUBLIC_KEY);
    }

    public static DSAPublicKey getPublicKey(byte[] pubKeyBytes) throws Exception {
        // 构造X509EncodedKeySpec对象
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(pubKeyBytes);
        // ALGORITHM 指定的加密算法
        KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
        return (DSAPublicKey) keyFactory.generatePublic(keySpec);
    }

    /**
     * 用私钥对信息生成数字签名
     * @param data 原文数据
     * @param privateKey 私钥
     * @return  签名结果
     * @throws Exception
     */
    public static byte[] sign(byte[] data, byte[] priKeyBytes) throws Exception {
        return sign(data, getPrivateKey(priKeyBytes));
    }

    public static byte[] sign(byte[] data, DSAPrivateKey priKey) throws Exception {
        // 用私钥对信息生成数字签名
        Signature signature = Signature.getInstance(priKey.getAlgorithm());
        signature.initSign(priKey);
        signature.update(data);
        return signature.sign();
    }

    /**
     * 校验数字签名
     * @param data 原文数据
     * @param publicKey 公钥
     * @param sign 签名数据
     * @return 校验成功返回true 失败返回false
     * @throws Exception
     * 
     */
    public static boolean verify(byte[] origin, byte[] pubKeyBytes, byte[] signed) throws Exception {
        return verify(origin, getPublicKey(pubKeyBytes), signed);
    }

    public static boolean verify(byte[] origin, DSAPublicKey pubKey, byte[] signed) throws Exception {
        Signature signature = Signature.getInstance(pubKey.getAlgorithm());
        signature.initVerify(pubKey);
        signature.update(origin);

        // 验证签名是否正常
        return signature.verify(signed);
    }

    public static void main(String[] args) throws Exception {
        Map<String, DSAKey> key = initKey();
        byte[] data = "123456".getBytes();
        byte[] signed = sign(data, getPrivateKey(key));
        boolean flag = verify(data, getPublicKey(key), signed);
        System.out.println(flag);
    }
}
