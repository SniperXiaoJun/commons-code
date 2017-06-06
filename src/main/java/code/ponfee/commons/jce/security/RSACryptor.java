package code.ponfee.commons.jce.security;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Signature;
import java.security.interfaces.RSAKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

import org.apache.commons.lang3.ArrayUtils;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERNull;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;

import code.ponfee.commons.exception.UnimplementedException;
import code.ponfee.commons.jce.RSASignAlgorithm;
import code.ponfee.commons.util.Bytes;
import code.ponfee.commons.util.MavenProjects;
import code.ponfee.commons.util.Streams;

/**
 * RSA加/解密，签名/验签
 * @author fupf
 */
public final class RSACryptor {

    private static final String ALG_RSA = "RSA";

    /**
     * 密钥生成
     * @param keySize 密钥长度（位）
     * @return
     */
    public static RSAKeyPair genRSAKeyPair(int keySize) {
        KeyPairGenerator keyPairGen;
        try {
            keyPairGen = KeyPairGenerator.getInstance(ALG_RSA);
        } catch (Exception e) {
            throw new SecurityException(e);
        }
        keyPairGen.initialize(keySize);
        KeyPair keyPair = keyPairGen.generateKeyPair();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        return new RSAKeyPair(privateKey, publicKey);
    }

    /**
     * 1024位密钥生成
     * @return
     */
    public static RSAKeyPair genRsaKeyPair() {
        return genRSAKeyPair(1024);
    }

    /**
     * 解析PKCS#8私钥
     * @param pkcs8PrivateKey
     * @return RSAPrivateKey
     */
    public static RSAPrivateKey parsePkcs8PrivateKey(byte[] pkcs8PrivateKey) {
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(pkcs8PrivateKey);
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(ALG_RSA);
            return (RSAPrivateKey) keyFactory.generatePrivate(pkcs8KeySpec);
        } catch (Exception e) {
            throw new SecurityException(e);
        }
    }

    /**
     * 解析PKCS#8私钥
     * @param b64Pkcs8PrivateKey
     * @return RSAPrivateKey
     */
    public static RSAPrivateKey parseB64Pkcs8PrivateKey(String b64Pkcs8PrivateKey) {
        return parsePkcs8PrivateKey(Bytes.base64Decode(b64Pkcs8PrivateKey));
    }

    /**
     * 解析PKCS#1私钥
     * @param pkcs1PrivateKey
     * @return RSAPrivateKey
     */
    public static RSAPrivateKey parsePkcs1PrivateKey(byte[] pkcs1PrivateKey) {
        // add PKCS#8 formatting
        ASN1EncodableVector v1 = new ASN1EncodableVector();
        v1.add(new ASN1ObjectIdentifier(PKCSObjectIdentifiers.rsaEncryption.getId()));
        v1.add(DERNull.INSTANCE);

        ASN1EncodableVector v2 = new ASN1EncodableVector();
        v2.add(new ASN1Integer(0));
        v2.add(new DERSequence(v1));
        v2.add(new DEROctetString(pkcs1PrivateKey));
        ASN1Sequence seq = new DERSequence(v2);
        try {
            return parsePkcs8PrivateKey(seq.getEncoded(ASN1Sequence.DER));
        } catch (IOException e) {
            throw new SecurityException(e);
        }
    }

    /**
     * 解析PKCS#1私钥
     * @param pkcs1PrivateKey（base64编码）
     * @return RSAPrivateKey
     */
    public static RSAPrivateKey parseB64Pkcs1PrivateKey(String b64Pkcs1PrivateKey) {
        return parsePkcs1PrivateKey(Bytes.base64Decode(b64Pkcs1PrivateKey));
    }

    /**
     * 解析公钥
     * @param publicKey（base64编码）
     * @return RSAPublicKey
     */
    public static RSAPublicKey parseB64PublicKey(String publicKey) {
        byte[] keyBytes = Bytes.base64Decode(publicKey);
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(keyBytes);
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(ALG_RSA);
            return (RSAPublicKey) keyFactory.generatePublic(x509KeySpec);
        } catch (Exception e) {
            throw new SecurityException(e);
        }
    }

    /**
     * 私钥base64编码（pkcs8格式）
     * @param privateKey
     * @return pkcs8 base64
     */
    public static String toB64Pkcs8Encode(RSAPrivateKey privateKey) {
        return Bytes.base64Encode(privateKey.getEncoded());
    }

    /**
     * 私钥base64编码（pkcs1格式），未实现
     * @param privateKey
     * @return pkcs1 base64
     */
    public static String toB64Pkcs1Encode(RSAPrivateKey privateKey) {
        throw new UnimplementedException();
    }

    /**
     * 转base64编码
     * @param publicKey
     * @return
     */
    public static String toB64Encode(RSAPublicKey publicKey) {
        return Bytes.base64Encode(publicKey.getEncoded());
    }

    /**
     * SHA1签名
     * @param data
     * @param privateKey
     * @return
     */
    public static byte[] signSha1(byte[] data, RSAPrivateKey privateKey) {
        return sign(data, privateKey, RSASignAlgorithm.SHA1withRSA);
    }

    /**
     * MD5签名
     * @param data
     * @param privateKey
     * @return
     */
    public static byte[] signMd5(byte[] data, RSAPrivateKey privateKey) {
        return sign(data, privateKey, RSASignAlgorithm.MD5withRSA);
    }

    /**
     * SHA1
     * @param data
     * @param publicKey
     * @param signed
     * @return
     */
    public static boolean verifySha1(byte[] data, RSAPublicKey publicKey, byte[] signed) {
        return verify(data, publicKey, signed, RSASignAlgorithm.SHA1withRSA);
    }

    /**
     * MD5验签
     * @param data
     * @param publicKey
     * @param signed
     * @return
     */
    public static boolean verifyMd5(byte[] data, RSAPublicKey publicKey, byte[] signed) {
        return verify(data, publicKey, signed, RSASignAlgorithm.MD5withRSA);
    }

    /**
     * <pre>
     *   1、可以通过修改生成密钥的长度来调整密文长度
     *   2、不管明文长度是多少，RSA生成的密文长度总是固定的
     *   3、明文长度不能超过密钥长度：
     *     1）sun jdk默认的RSA加密实现不允许明文长度超过密钥长度减去11字节（byte）：比如1024位（bit）的密钥，则待加密的明文最长为1024/8-11=117（byte）
     *     2）BouncyCastle提供的加密算法能够支持到的RSA明文长度最长为密钥长度
     *   4、每次生成的密文都不一致证明加密算法安全：这是因为在加密前使用RSA/None/PKCS1Padding对明文信息进行了随机数填充
     *   5、javax.crypto.Cipher是线程不安全的
     * </pre>
     * 
     * 大数据分块加密
     * @param data 源数据
     * @param Key
     * @return
     */
    public static <T extends Key & RSAKey> byte[] encrypt(byte[] data, T key) {
        try {
            Cipher cipher = Cipher.getInstance(key.getAlgorithm());
            cipher.init(Cipher.ENCRYPT_MODE, key);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int inputLen = data.length, modLen = key.getModulus().bitLength() / 8 - 11;
            byte[] block;
            for (int len, i = 0, offSet = 0; inputLen - offSet > 0; i++, offSet = i * modLen) {
                // 对数据分段加密
                len = (inputLen - offSet > modLen) ? modLen : inputLen - offSet;
                block = cipher.doFinal(data, offSet, len);
                out.write(block, 0, block.length);
            }
            out.flush();
            return out.toByteArray();
        } catch (Exception e) {
            throw new SecurityException(e);
        }
    }

    /**
     * 大数据分块解密
     * @param encrypted
     * @param key
     * @return
     */
    public static <T extends Key & RSAKey> byte[] decrypt(byte[] encrypted, T key) {
        try {
            Cipher cipher = Cipher.getInstance(key.getAlgorithm());
            cipher.init(Cipher.DECRYPT_MODE, key);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int inputLen = encrypted.length, modLen = key.getModulus().bitLength() / 8;
            byte[] block;
            for (int len, i = 0, offSet = 0; inputLen - offSet > 0; i++, offSet = i * modLen) {
                // 对数据分段解密
                len = (inputLen - offSet > modLen) ? modLen : inputLen - offSet;
                block = cipher.doFinal(encrypted, offSet, len);
                out.write(block, 0, block.length);
            }
            out.flush();
            return out.toByteArray();
        } catch (Exception e) {
            throw new SecurityException(e);
        }
    }

    // -------------------------------private methods-------------------------------
    /**
     * 数据签名
     * @param data
     * @param privateKey
     * @param algId
     * @return
     */
    private static byte[] sign(byte[] data, RSAPrivateKey privateKey, RSASignAlgorithm alg) {
        try {
            Signature signature = Signature.getInstance(alg.name());
            signature.initSign(privateKey);
            signature.update(data);
            return signature.sign();
        } catch (Exception e) {
            throw new SecurityException(e);
        }
    }

    /**
     * 验签
     * @param data
     * @param publicKey
     * @param signed
     * @param algId
     * @return
     */
    private static boolean verify(byte[] data, RSAPublicKey publicKey, byte[] signed, RSASignAlgorithm alg) {
        try {
            Signature signature = Signature.getInstance(alg.name());
            signature.initVerify(publicKey);
            signature.update(data);
            return signature.verify(signed);
        } catch (Exception e) {
            throw new SecurityException(e);
        }
    }

    /**
     * RSA密钥对
     */
    public static final class RSAKeyPair implements Serializable {
        private static final long serialVersionUID = -1592700389671199076L;
        private final RSAPrivateKey privateKey;
        private final RSAPublicKey publicKey;

        private RSAKeyPair(RSAPrivateKey privateKey, RSAPublicKey publicKey) {
            this.privateKey = privateKey;
            this.publicKey = publicKey;
        }

        public RSAPrivateKey getPrivateKey() {
            return privateKey;
        }

        public RSAPublicKey getPublicKey() {
            return publicKey;
        }

        public String getB64Pkcs8PrivateKey() {
            return toB64Pkcs8Encode(privateKey);
        }

        public String getB64Pkcs1PrivateKey() {
            throw new UnimplementedException();
        }

        public String getB64PublicKey() {
            return toB64Encode(publicKey);
        }
    }

    public static void main(String[] args) throws Exception {
        long i = System.currentTimeMillis();
        RSAKeyPair keyPair = genRSAKeyPair(512);

        RSAPrivateKey privateKey = keyPair.getPrivateKey();
        RSAPublicKey publicKey = keyPair.getPublicKey();
        System.out.println(RSACryptor.toB64Pkcs8Encode(privateKey));
        System.out.println(RSACryptor.toB64Encode(publicKey));

        System.out.println("=============================加密测试==============================");
        //byte[] data = "加解密测试".getBytes();
        byte[] data = Streams.file2bytes(MavenProjects.getMainJavaFile(RSACryptor.class));
        System.out.println("原文：");
        System.out.println(Bytes.hexDump(ArrayUtils.subarray(data, 0, 100)));
        byte[] encodedData = RSACryptor.encrypt(data, publicKey);
        System.out.println("密文：");
        System.out.println(Bytes.hexDump(ArrayUtils.subarray(encodedData, 0, 100)));
        System.out.println("解密：");
        System.out.println(Bytes.hexDump(ArrayUtils.subarray(RSACryptor.decrypt(encodedData, privateKey), 0, 100)));

        System.out.println("===========================签名测试=========================");
        data = Bytes.base64Decode("");
        byte[] signed = RSACryptor.signSha1(data, privateKey);
        System.out.println("签名数据：len->" + signed.length + " ， b64->" + Bytes.base64Encode(signed));
        System.out.println("验签结果：" + RSACryptor.verifySha1(data, publicKey, signed));

        System.out.println("cost time: " + (System.currentTimeMillis() - i));

        //parseB64Pkcs1PrivateKey("MIIEoQIBAAKCAQB87L/ttdhGrffwOW7TzgDHDIbQCm2qU+PqCwP5z6QOqZWywnqeudF19FhlWY1nh16e7j03eMPeCCa/ZUOsoelsxdXZ1zyxtxoylWeg1RxtvPIw1YpvvyNbeTiInAMI01qxG4OOusgKjCmga+xIqZiH87kr5NWkEcQIOW4e+H0oROVzGSUr8cpHj5xwX3HRORgF/Papm5LcSYVNuU3iTRy6MhuPouQclI7CwwcukC7YVCLra+Z3+GHWauKZMrVNHPjM/2zyu3KvOR2IZDWg6cBIV5c6aHdiB6MwKiRj6fIzPbqh/szumgrVrrz3VeSnPlNWVaUy2MWfkKT0KJa7vfjXAgMBAAECggEAJIkdLMl1Il6417IEXr+t7IkWWHvkTN9SFd344LPAmGUymeBU+l0ADI5U1/dT6sZlfvfQQYv5RNN/eZSFMVT9LsnBXH+diaAycj7N2vTY5qNO9cdOQJZXIeaXCSUPoiImMQwJ8tFfte3+MqO9rBalvIUkT9kSPnTPr1QUh8xG1mK7Em/rx+8fRcl2IKHEyFoch31e3xuKxuxtQhkWyqXNinWJ/AQRojtysDcjZNy/Ics/9/YO803do3DUvIhzVouxgZcw0hriKt6QEyeJ/n1HLXI+JXogxDNUdjJsfzUMHgagdOdrBSYCAyx8DXd/O9NSJRAluwjvRDOETYhvl9kRGQKBgQDt0OcntHaWyDMpVSZvu5TUhk39XYWbStWA+z2/VCtehoZWF1+W7mG5H5mjZ6GKgiQ0fEXoR/gkVF3GOGOfMpG0+grTAygG37ugllr/tKZKx+I4MSftXh9shqOzQKtFDiUHIU6vzXjPE62fWCGiGYCUmgZk2p4hHxEVtXPN9hUEiwKBgQCGehK07j8mBbBa4HTWunkZBIoy+gTtDPFWJjBE2GlbXJ7L5mzDm04VGPrVvwzTxV+MuuepCUv/dsWxQX/RiFUQffXI1tSi4b0NKDbY2xxDQlUwIfG4bIn53uX/NqTtggUFWzY9GMghqD8t6s6HdFfuMt6Ul/utULZd9aaZI8lKZQKBgGz6XfMD75QJCejW7FYnT3xUT0jbom4XTN9eQl165KTcYJLzAwrXElES+gS3aH9gQ9cJW7+lu0BqqM486On68mpMaslnmOANhp2ASRMEZW+/SRsW64UKrLu+tyVdbR6n7K/nw3csYUADdHyglkkCBroSGvv8cpoa8mlQTVEEg30hAoGAK43aBTOszDnHdoeAEBPxKMMpp30Gn2gzuf1AYOveo7KJ0+xbibcBQSAIDbaFBwnD+qaGZV8XeDQVr2VRaqHHO0Iwms3JrL+EJYDC0tWUf8w6Hw6/ZUXyIjWpNFGUdUBJNATouj0OhKgjXlHQdlqeKA3dvS7EWsvrZN8tCChpB2kCgYBxODTwc5l/0zz85TIapeERIj+fEP+KxupV2H7aHsUNdxVn6VcfqqL1ihlpSWFDrQvvs5pLiLUuQvxM1kpFOnYJ0cj7jK7YUq3GGg2FVtDSyOLoepDH45jK0NTRzUvbfiXNHSgn+gT6NGikAIXpseDabwWX9+dAc0U7TH2nQGqYew==");
        //parseB64Pkcs8PriKey("MIIEoQIBAAKCAQB87L/ttdhGrffwOW7TzgDHDIbQCm2qU+PqCwP5z6QOqZWywnqeudF19FhlWY1nh16e7j03eMPeCCa/ZUOsoelsxdXZ1zyxtxoylWeg1RxtvPIw1YpvvyNbeTiInAMI01qxG4OOusgKjCmga+xIqZiH87kr5NWkEcQIOW4e+H0oROVzGSUr8cpHj5xwX3HRORgF/Papm5LcSYVNuU3iTRy6MhuPouQclI7CwwcukC7YVCLra+Z3+GHWauKZMrVNHPjM/2zyu3KvOR2IZDWg6cBIV5c6aHdiB6MwKiRj6fIzPbqh/szumgrVrrz3VeSnPlNWVaUy2MWfkKT0KJa7vfjXAgMBAAECggEAJIkdLMl1Il6417IEXr+t7IkWWHvkTN9SFd344LPAmGUymeBU+l0ADI5U1/dT6sZlfvfQQYv5RNN/eZSFMVT9LsnBXH+diaAycj7N2vTY5qNO9cdOQJZXIeaXCSUPoiImMQwJ8tFfte3+MqO9rBalvIUkT9kSPnTPr1QUh8xG1mK7Em/rx+8fRcl2IKHEyFoch31e3xuKxuxtQhkWyqXNinWJ/AQRojtysDcjZNy/Ics/9/YO803do3DUvIhzVouxgZcw0hriKt6QEyeJ/n1HLXI+JXogxDNUdjJsfzUMHgagdOdrBSYCAyx8DXd/O9NSJRAluwjvRDOETYhvl9kRGQKBgQDt0OcntHaWyDMpVSZvu5TUhk39XYWbStWA+z2/VCtehoZWF1+W7mG5H5mjZ6GKgiQ0fEXoR/gkVF3GOGOfMpG0+grTAygG37ugllr/tKZKx+I4MSftXh9shqOzQKtFDiUHIU6vzXjPE62fWCGiGYCUmgZk2p4hHxEVtXPN9hUEiwKBgQCGehK07j8mBbBa4HTWunkZBIoy+gTtDPFWJjBE2GlbXJ7L5mzDm04VGPrVvwzTxV+MuuepCUv/dsWxQX/RiFUQffXI1tSi4b0NKDbY2xxDQlUwIfG4bIn53uX/NqTtggUFWzY9GMghqD8t6s6HdFfuMt6Ul/utULZd9aaZI8lKZQKBgGz6XfMD75QJCejW7FYnT3xUT0jbom4XTN9eQl165KTcYJLzAwrXElES+gS3aH9gQ9cJW7+lu0BqqM486On68mpMaslnmOANhp2ASRMEZW+/SRsW64UKrLu+tyVdbR6n7K/nw3csYUADdHyglkkCBroSGvv8cpoa8mlQTVEEg30hAoGAK43aBTOszDnHdoeAEBPxKMMpp30Gn2gzuf1AYOveo7KJ0+xbibcBQSAIDbaFBwnD+qaGZV8XeDQVr2VRaqHHO0Iwms3JrL+EJYDC0tWUf8w6Hw6/ZUXyIjWpNFGUdUBJNATouj0OhKgjXlHQdlqeKA3dvS7EWsvrZN8tCChpB2kCgYBxODTwc5l/0zz85TIapeERIj+fEP+KxupV2H7aHsUNdxVn6VcfqqL1ihlpSWFDrQvvs5pLiLUuQvxM1kpFOnYJ0cj7jK7YUq3GGg2FVtDSyOLoepDH45jK0NTRzUvbfiXNHSgn+gT6NGikAIXpseDabwWX9+dAc0U7TH2nQGqYew==");
    }

}
