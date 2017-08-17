package test.jce.crypto;

import static code.ponfee.commons.jce.security.RSACryptor.extractPublicKey;
import static code.ponfee.commons.jce.security.RSACryptor.fromEncryptedPkcs8PemPrivateKey;
import static code.ponfee.commons.jce.security.RSACryptor.fromPkcs1PemPrivateKey;
import static code.ponfee.commons.jce.security.RSACryptor.fromPkcs1PrivateKey;
import static code.ponfee.commons.jce.security.RSACryptor.fromPkcs1PublicKey;
import static code.ponfee.commons.jce.security.RSACryptor.fromPkcs8PemPublicKey;
import static code.ponfee.commons.jce.security.RSACryptor.fromPkcs8PrivateKey;
import static code.ponfee.commons.jce.security.RSACryptor.fromPkcs8PublicKey;
import static code.ponfee.commons.jce.security.RSACryptor.genRSAKeyPair;
import static code.ponfee.commons.jce.security.RSACryptor.toEncryptedPkcs8PemPrivateKey;
import static code.ponfee.commons.jce.security.RSACryptor.toPkcs1PemPrivateKey;
import static code.ponfee.commons.jce.security.RSACryptor.toPkcs1PrivateKey;
import static code.ponfee.commons.jce.security.RSACryptor.toPkcs8PemPublicKey;
import static code.ponfee.commons.jce.security.RSACryptor.toPkcs8PrivateKey;

import java.io.IOException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;

import org.apache.commons.lang3.ArrayUtils;

import code.ponfee.commons.jce.security.RSACryptor;
import code.ponfee.commons.jce.security.RSACryptor.RSAKeyPair;
import code.ponfee.commons.util.Bytes;
import code.ponfee.commons.util.MavenProjects;
import code.ponfee.commons.util.Streams;

public class RSACryptoTester {

    public static void main(String[] args) throws Exception {
        RSAKeyPair keyPair = genRSAKeyPair(1024);
        test(keyPair.getPrivateKey(), extractPublicKey(keyPair.getPrivateKey()));
        
        test(fromPkcs1PemPrivateKey(toPkcs1PemPrivateKey(fromPkcs1PrivateKey(keyPair.getPkcs1PrivateKey()))),
             fromPkcs8PemPublicKey(toPkcs8PemPublicKey(fromPkcs1PublicKey(keyPair.getPkcs1PublicKey()))));
        
        test(fromPkcs1PrivateKey(toPkcs1PrivateKey(keyPair.getPrivateKey())),
             fromPkcs1PublicKey(keyPair.getPkcs1PublicKey()));

        test(fromPkcs8PrivateKey(keyPair.getPkcs8PrivateKey()),
             fromPkcs8PublicKey(keyPair.getPkcs8PublicKey()));

        System.out.println(fromEncryptedPkcs8PemPrivateKey(toEncryptedPkcs8PemPrivateKey(keyPair.getPrivateKey(),"123"), "123"));

        System.out.println(toPkcs1PrivateKey(keyPair.getPrivateKey()));
        System.out.println(toPkcs8PrivateKey(keyPair.getPrivateKey()));
        System.out.println(toPkcs1PemPrivateKey(keyPair.getPrivateKey()));
        System.out.println(toEncryptedPkcs8PemPrivateKey(keyPair.getPrivateKey(), "1234"));
    }

    private  static void test(RSAPrivateKey privateKey, RSAPublicKey publicKey) throws IOException {
        long i = System.currentTimeMillis();
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
        data = Base64.getDecoder().decode("");
        byte[] signed = RSACryptor.signSha1(data, privateKey);
        System.out.println("签名数据：len->" + signed.length + " ， b64->" + Base64.getEncoder().encodeToString(signed));
        System.out.println("验签结果：" + RSACryptor.verifySha1(data, publicKey, signed));

        System.out.println("cost time: " + (System.currentTimeMillis() - i));
    }
}
