package test.jce.crypto;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import code.ponfee.commons.jce.crypto.Algorithm;
import code.ponfee.commons.jce.crypto.Encryptor;
import code.ponfee.commons.jce.crypto.EncryptorBuilder;
import code.ponfee.commons.jce.crypto.Mode;
import code.ponfee.commons.jce.crypto.Padding;

public class EncryptTester {

    public static void main(String[] args) {
        BouncyCastleProvider bc = new BouncyCastleProvider();
        Encryptor coder = null;

        //coder = EncryptorBuilder.newBuilder(Algorithm.DESede).build();
        //coder = EncryptorBuilder.newBuilder(Algorithm.RC2).key(randomBytes(5)).build();
        //coder = EncryptorBuilder.newBuilder(Algorithm.RC2).key(randomBytes(16)).mode(Mode.ECB).padding(Padding.NoPadding).provider(bc).build();
        //coder = EncryptorBuilder.newBuilder(Algorithm.RC4).key(randomBytes(16)).mode(Mode.ECB).padding(Padding.NoPadding).provider(bc).build();
        //coder = EncryptorBuilder.newBuilder(Algorithm.AES).key(randomBytes(16)).build();
        //coder = EncryptorBuilder.newBuilder(Algorithm.AES).key(randomBytes(16)).mode(Mode.ECB).padding(Padding.PKCS5Padding).provider(bc).build();
        //coder = EncryptorBuilder.newBuilder(Algorithm.AES).key(randomBytes(16)).mode(Mode.OFB).padding(Padding.NoPadding).ivParameter(randomBytes(16)).provider(bc).build();
        //coder = EncryptorBuilder.newBuilder(Algorithm.AES).key(randomBytes(16)).mode(Mode.CBC).padding(Padding.PKCS7Padding).ivParameter(randomBytes(16)).provider(bc).build();
        //coder = EncryptorBuilder.newBuilder(Algorithm.DES).key(randomBytes(8)).mode(Mode.CBC).padding(Padding.NoPadding).ivParameter(randomBytes(8)).provider(bc).build();
        //coder = EncryptorBuilder.newBuilder(Algorithm.DES).key(randomBytes(8)).provider(bc).build();
        //coder = EncryptorBuilder.newBuilder(Algorithm.DES).key(randomBytes(8)).mode(Mode.CBC).padding(Padding.PKCS5Padding).ivParameter(randomBytes(8)).provider(bc).build();
        //coder = EncryptorBuilder.newBuilder(Algorithm.DESede).key(randomBytes(16)).provider(bc).build();
        //coder = EncryptorBuilder.newBuilder(Algorithm.DESede).key(randomBytes(24)).mode(Mode.CBC).padding(Padding.PKCS5Padding).ivParameter(randomBytes(8)).build();
        //coder = EncryptorBuilder.newBuilder(Algorithm.DESede).key(Encryptor.randomBytes(16)).mode(Mode.ECB).padding(Padding.PKCS5Padding).provider(bc).build();
        coder = EncryptorBuilder.newBuilder(Algorithm.DESede).key(Encryptor.randomBytes(16)).mode(Mode.CBC).padding(Padding.PKCS5Padding).ivParameter(Encryptor.randomBytes(8)).provider(bc).build();

        byte[] encrypted = coder.encrypt("12345678".getBytes()); // 加密
        byte[] origin = coder.decrypt(encrypted); // 解密
        System.out.println(new String(origin));
    }
}
