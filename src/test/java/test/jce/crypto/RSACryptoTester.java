package test.jce.crypto;

import static code.ponfee.commons.jce.security.RSACryptor.genRSAKeyPair;

import java.io.IOException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;

import org.apache.commons.lang3.ArrayUtils;

import com.google.common.io.Files;

import code.ponfee.commons.jce.security.RSACryptor;
import code.ponfee.commons.jce.security.RSACryptor.RSAKeyPair;
import code.ponfee.commons.jce.security.RSAPrivateKeys;
import code.ponfee.commons.jce.security.RSAPublicKeys;
import code.ponfee.commons.util.Bytes;
import code.ponfee.commons.util.IdcardResolver;
import code.ponfee.commons.util.MavenProjects;

public class RSACryptoTester {

    public static void main(String[] args) throws Exception {
        RSAKeyPair keyPair = genRSAKeyPair(1024);
        test(keyPair.getPrivateKey(), RSAPrivateKeys.extractPublicKey(keyPair.getPrivateKey()));
        
        test(RSAPrivateKeys.fromPkcs1Pem(RSAPrivateKeys.toPkcs1Pem(RSAPrivateKeys.fromPkcs1(keyPair.getPkcs1PrivateKey()))),
             RSAPublicKeys.fromPkcs8Pem(RSAPublicKeys.toPkcs8Pem(RSAPublicKeys.fromPkcs1(keyPair.getPkcs1PublicKey()))));
        
        test(RSAPrivateKeys.fromPkcs1(RSAPrivateKeys.toPkcs1(keyPair.getPrivateKey())),
             RSAPublicKeys.fromPkcs1(keyPair.getPkcs1PublicKey()));

        test(RSAPrivateKeys.fromPkcs8(keyPair.getPkcs8PrivateKey()),
             RSAPublicKeys.fromPkcs8(keyPair.getPkcs8PublicKey()));

        System.out.println(RSAPrivateKeys.fromEncryptedPkcs8Pem(RSAPrivateKeys.toEncryptedPkcs8Pem(keyPair.getPrivateKey(),"123"), "123"));

        System.out.println(RSAPrivateKeys.toPkcs1(keyPair.getPrivateKey()));
        System.out.println(RSAPrivateKeys.toPkcs8(keyPair.getPrivateKey()));
        System.out.println(RSAPrivateKeys.toPkcs1Pem(keyPair.getPrivateKey()));
        System.out.println(RSAPrivateKeys.toEncryptedPkcs8Pem(keyPair.getPrivateKey(), "1234"));
    }

    private  static void test(RSAPrivateKey privateKey, RSAPublicKey publicKey) throws IOException {
        byte[] data = Files.toByteArray(MavenProjects.getMainJavaFile(IdcardResolver.class));
        System.out.println("=============================加密测试==============================");
        long i = System.currentTimeMillis();
        System.out.println("原文：");
        System.out.println(Bytes.hexDump(ArrayUtils.subarray(data, 0, 100)));
        byte[] encodedData = RSACryptor.encrypt(data, publicKey);
        System.out.println("密文：");
        System.out.println(Bytes.hexDump(ArrayUtils.subarray(encodedData, 0, 100)));
        System.out.println("解密：");
        System.out.println(Bytes.hexDump(ArrayUtils.subarray(RSACryptor.decrypt(encodedData, privateKey), 0, 100)));
        
        System.out.println("=============================加密测试==============================");
        i = System.currentTimeMillis();
        System.out.println("原文：");
        System.out.println(Bytes.hexDump(ArrayUtils.subarray(data, 0, 100)));
        encodedData = RSACryptor.encrypt(data, privateKey);
        System.out.println("密文：");
        System.out.println(Bytes.hexDump(ArrayUtils.subarray(encodedData, 0, 100)));
        System.out.println("解密：");
        System.out.println(Bytes.hexDump(ArrayUtils.subarray(RSACryptor.decrypt(encodedData, publicKey), 0, 100)));

        System.out.println("===========================签名测试=========================");
        data = Base64.getDecoder().decode("");
        byte[] signed = RSACryptor.signSha1(data, privateKey);
        System.out.println("签名数据：len->" + signed.length + " ， b64->" + Base64.getEncoder().encodeToString(signed));
        System.out.println("验签结果：" + RSACryptor.verifySha1(data, publicKey, signed));

        System.out.println("cost time: " + (System.currentTimeMillis() - i));
    }
}
