package test.jce.crypto;

import static code.ponfee.commons.util.SecureRandoms.nextBytes;

import java.security.Provider;

import code.ponfee.commons.jce.Providers;
import code.ponfee.commons.jce.crypto.Algorithm;
import code.ponfee.commons.jce.crypto.Mode;
import code.ponfee.commons.jce.crypto.Padding;
import code.ponfee.commons.jce.crypto.SymmetricCryptor;
import code.ponfee.commons.jce.crypto.SymmetricCryptorBuilder;

public class EncryptTester {

    public static void main(String[] args) {
        Provider bc = Providers.BC;
        SymmetricCryptor coder = null;

        //coder = SymmetricCryptorBuilder.newBuilder(Algorithm.DESede).build();
        //coder = SymmetricCryptorBuilder.newBuilder(Algorithm.RC2).key(nextBytes(5)).build();
        //coder = SymmetricCryptorBuilder.newBuilder(Algorithm.RC2).key(nextBytes(16)).mode(Mode.ECB).padding(Padding.NoPadding).provider(bc).build();
        //coder = SymmetricCryptorBuilder.newBuilder(Algorithm.AES).padding(Padding.PADDING_ISO10126).mode(Mode.ECB).key(SecureRandoms.nextBytes(16)).provider(bc).build();
        //coder = SymmetricCryptorBuilder.newBuilder(Algorithm.AES).key(nextBytes(16)).build();
        //coder = SymmetricCryptorBuilder.newBuilder(Algorithm.AES).key(nextBytes(16)).mode(Mode.ECB).padding(Padding.PKCS5Padding).provider(bc).build();
        //coder = SymmetricCryptorBuilder.newBuilder(Algorithm.AES).key(nextBytes(16)).mode(Mode.OFB).padding(Padding.NoPadding).ivParameter(nextBytes(16)).provider(bc).build();
        //coder = SymmetricCryptorBuilder.newBuilder(Algorithm.AES).key(nextBytes(16)).mode(Mode.CBC).padding(Padding.PKCS7Padding).ivParameter(nextBytes(16)).provider(bc).build();
        //coder = SymmetricCryptorBuilder.newBuilder(Algorithm.DES).key(nextBytes(8)).mode(Mode.CBC).padding(Padding.NoPadding).ivParameter(nextBytes(8)).provider(bc).build();
        //coder = SymmetricCryptorBuilder.newBuilder(Algorithm.DES).key(nextBytes(8)).provider(bc).build();
        //coder = SymmetricCryptorBuilder.newBuilder(Algorithm.DES).key(nextBytes(8)).mode(Mode.CBC).padding(Padding.PKCS5Padding).ivParameter(nextBytes(8)).provider(bc).build();
        //coder = SymmetricCryptorBuilder.newBuilder(Algorithm.DESede).key(nextBytes(16)).provider(bc).build();
        coder = SymmetricCryptorBuilder.newBuilder(Algorithm.AES).key(nextBytes(32)).mode(Mode.CBC).padding(Padding.X9_23Padding).ivParameter(nextBytes(16)).provider(bc).build();
        //coder = SymmetricCryptorBuilder.newBuilder(Algorithm.DESede).key(nextBytes(16)).mode(Mode.ECB).padding(Padding.PKCS5Padding).provider(bc).build();
        //coder = SymmetricCryptorBuilder.newBuilder(Algorithm.DESede).key(SecureRandoms.nextBytes(16)).mode(Mode.CBC).padding(Padding.PKCS5Padding).ivParameter(SecureRandoms.nextBytes(8)).provider(bc).build();

        byte[] encrypted = coder.encrypt("12345678".getBytes()); // 加密
        byte[] origin = coder.decrypt(encrypted); // 解密
        System.out.println(new String(origin));
    }
}
