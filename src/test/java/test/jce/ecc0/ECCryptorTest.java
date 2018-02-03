package test.jce.ecc0;

import java.io.ByteArrayInputStream;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

import javax.crypto.Cipher;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import com.google.common.base.Stopwatch;

import code.ponfee.commons.jce.ECParameters;
import code.ponfee.commons.jce.ecc.Cryptor;
import code.ponfee.commons.jce.ecc.ECCryptor;
import code.ponfee.commons.jce.ecc.EllipticCurve;
import code.ponfee.commons.jce.ecc.Key;
import code.ponfee.commons.jce.ecc.NullCryptor;
import code.ponfee.commons.jce.ecc.RSACryptor;
import code.ponfee.commons.jce.ecc.RSAHashCryptor;
import code.ponfee.commons.jce.ecc.RSAKey;
import code.ponfee.commons.jce.ecc.RSAPKCS1PaddingCryptor;
import code.ponfee.commons.jce.security.RSAPrivateKeys;
import code.ponfee.commons.jce.security.RSAPublicKeys;
import code.ponfee.commons.util.IdcardResolver;
import code.ponfee.commons.util.MavenProjects;
import code.ponfee.commons.util.SecureRandoms;

public class ECCryptorTest {

    private static byte[] origin = MavenProjects.getMainJavaFileAsByteArray(IdcardResolver.class);

    @Test
    public void testECC() throws Exception {
        Cryptor cs = new ECCryptor(new EllipticCurve(ECParameters.secp256r1));
        Key dk = cs.generateKey();
        Key ek = dk.getPublic();
        //Key pk = ((ECKey) sk).getECPublic();
        System.out.println(dk + "\n" + ek);

        byte[] encrypted = cs.encrypt(origin, ek);
        byte[] decrypted = cs.decrypt(encrypted, dk);
        if (!Arrays.equals(origin, decrypted)) {
            System.err.println("FAIL!");
        } else {
            System.out.println("=====ECCryptor Decrypted text is: \n" + new String(decrypted));
        }
    }

    @Test
    public void testRSA() throws Exception {
        RSAKey dk = new RSAKey(1024);
        Key ek = dk.getPublic();
        //RSACryptor cs = new RSACryptor();
        //RSACryptor cs = new RSAHashCryptor();
        RSACryptor cs = new RSAPKCS1PaddingCryptor();

        Stopwatch watch = Stopwatch.createStarted();
        byte[] encrypted = cs.encrypt(origin, ek);
        byte[] decrypted = cs.decrypt(encrypted, dk);
        if (!Arrays.equals(origin, decrypted)) {
            System.err.println("FAIL!");
        } else {
            System.out.println("\n\n=====RSA1 Decrypted text is: \n" + new String(decrypted));
        }
        System.out.println(watch.stop());

        watch.reset().start();
        /*RSAPublicKey pub = RSAPublicKeys.toRSAPublicKey(dk.n, dk.e);
        encrypted = code.ponfee.commons.jce.security.RSACryptor.encrypt(origin, pub);*/
        RSAPrivateKey pri = RSAPrivateKeys.toRSAPrivateKey(dk.n, dk.d);
        decrypted = code.ponfee.commons.jce.security.RSACryptor.decryptWithoutPadding(encrypted, pri);
        if (!Arrays.equals(origin, decrypted)) {
            System.err.println("FAIL!");
        } else {
            System.out.println("\n\n=====RSA2 Decrypted text is: \n" + new String(decrypted));
            System.out.println(new String(decrypted));
        }
        System.out.println(watch.stop());
    }

    @Test
    public void testRSARandom() throws Exception {
        RSAKey dk = new RSAKey(1024);
        Key ek = dk.getPublic();
        RSACryptor cs = new RSACryptor();
        RSACryptor cs2 = new RSAPKCS1PaddingCryptor();
        RSAPublicKey pub = RSAPublicKeys.toRSAPublicKey(dk.n, dk.e);
        RSAPrivateKey pri = RSAPrivateKeys.toRSAPrivateKey(dk.n, dk.d);
        for (int i = 0; i < 100; i++) {

            /*int length = ThreadLocalRandom.current().nextInt(65537) + 1;
            int offset = ThreadLocalRandom.current().nextInt(origin.length - length);
            System.out.println(length + " -> " + offset);
            byte[] data = Arrays.copyOfRange(origin, offset, offset + length);*/

            //byte[] data = new byte[] { 0, 1, 1, 0, 0 }; // occur wrong

            byte[] data = SecureRandoms.nextBytes(ThreadLocalRandom.current().nextInt(255) + 1);

            // 1
            byte[] encrypted1 = cs.encrypt(data, ek);
            byte[] decrypted1 = cs.decrypt(encrypted1, dk);

            // 2
            byte[] encrypted2 = encrypted1;
            byte[] decrypted2 = code.ponfee.commons.jce.security.RSACryptor.decryptWithoutPadding(encrypted2, pri);

            // 3
            byte[] encrypted3 = code.ponfee.commons.jce.security.RSACryptor.encryptWithoutPadding(data, pub);
            byte[] decrypted3 = code.ponfee.commons.jce.security.RSACryptor.decryptWithoutPadding(encrypted3, pri);

            // 4
            byte[] encrypted4 = code.ponfee.commons.jce.security.RSACryptor.encrypt(data, pub);
            byte[] decrypted4 = code.ponfee.commons.jce.security.RSACryptor.decrypt(encrypted4, pri);

            // 5
            byte[] encrypted5 = cs2.encrypt(data, ek);
            byte[] decrypted5 = cs2.decrypt(encrypted5, dk);
            // -------------------------------------------------------
            if (!Arrays.equals(data, decrypted1)) {
                System.err.println("[" + StringUtils.leftPad(i + "", 4, "0") + "]decrypt1 FAIL!: " + Hex.encodeHexString(data) + " -> "
                    + Hex.encodeHexString(decrypted1));
            }

            if (!Arrays.equals(data, decrypted2)) {
                System.err.println("[" + StringUtils.leftPad(i + "", 4, "0") + "]decrypt2 FAIL!: " + Hex.encodeHexString(data) + " -> "
                    + Hex.encodeHexString(decrypted2));
            }

            if (!Arrays.equals(data, decrypted3)) {
                System.err.println("[" + StringUtils.leftPad(i + "", 4, "0") + "]decrypt3 FAIL!: " + Hex.encodeHexString(data) + " -> "
                    + Hex.encodeHexString(decrypted3));
            }

            if (!Arrays.equals(data, decrypted4)) {
                System.err.println("[" + StringUtils.leftPad(i + "", 4, "0") + "]decrypt4 FAIL!: " + Hex.encodeHexString(data) + " -> "
                    + Hex.encodeHexString(decrypted4));
            }

            if (!Arrays.equals(data, decrypted5)) {
                System.err.println("[" + StringUtils.leftPad(i + "", 4, "0") + "]decrypt5 FAIL!: " + Hex.encodeHexString(data) + " -> "
                    + Hex.encodeHexString(decrypted5));
            }
        }
    }

    @Test
    public void testRSAInverse() throws Exception {
        RSAKey dk = new RSAKey(1024);
        Key ek = dk.getPublic();
        //RSACryptor cs = new RSACryptor();
        //RSACryptor cs = new RSAPkcs1PaddingCryptor();
        RSAHashCryptor cs = new RSAHashCryptor();
        for (int i = 0; i < 100; i++) {
            byte[] data = SecureRandoms.nextBytes(ThreadLocalRandom.current().nextInt(255) + 1);
            byte[] encrypted1 = cs.encrypt(data, dk); // 私钥加密
            byte[] decrypted1 = cs.decrypt(encrypted1, ek); // 公钥解密
            if (!Arrays.equals(data, decrypted1)) {
                System.err.println("[" + StringUtils.leftPad(i + "", 4, "0") + "]decrypt1 FAIL!: " + Hex.encodeHexString(data) + " -> "
                    + Hex.encodeHexString(decrypted1));
            }
        }
    }

    @Test
    public void testRSAPadding() throws Exception {
        RSAKey dk = new RSAKey(1024);
        Key ek = dk.getPublic();
        RSACryptor cs = new RSAPKCS1PaddingCryptor();
        RSAPublicKey pub = RSAPublicKeys.toRSAPublicKey(dk.n, dk.e);
        RSAPrivateKey pri = RSAPrivateKeys.toRSAPrivateKey(dk.n, dk.d);

        // ＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝公钥加密，私钥解密
        Stopwatch watch = Stopwatch.createStarted();
        byte[] encrypted = cs.encrypt(origin, ek);
        byte[] decrypted = cs.decrypt(encrypted, dk);
        if (!Arrays.equals(origin, decrypted)) {
            System.err.println("FAIL!");
        } else {
            System.out.println("\n\n=====RSA1 Decrypted text is: \n" + new String(decrypted));
        }
        System.out.println(watch.stop());

        watch.reset().start();
        decrypted = code.ponfee.commons.jce.security.RSACryptor.decrypt(encrypted, pri);
        if (!Arrays.equals(origin, decrypted)) {
            System.err.println("FAIL!");
        } else {
            System.out.println("\n\n=====RSA2 Decrypted text is: \n" + new String(decrypted));
            System.out.println(new String(decrypted));
        }
        System.out.println(watch.stop());

        // ＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝私钥加密，公钥解密
        watch = Stopwatch.createStarted();
        encrypted = cs.encrypt(origin, dk);
        decrypted = cs.decrypt(encrypted, ek);
        if (!Arrays.equals(origin, decrypted)) {
            System.err.println("FAIL!");
        } else {
            System.out.println("\n\n=====RSA1 Decrypted text is: \n" + new String(decrypted));
        }
        System.out.println(watch.stop());

        watch.reset().start();
        decrypted = code.ponfee.commons.jce.security.RSACryptor.decrypt(encrypted, pub);
        if (!Arrays.equals(origin, decrypted)) {
            System.err.println("FAIL!");
        } else {
            System.out.println("\n\n=====RSA2 Decrypted text is: \n" + new String(decrypted));
            System.out.println(new String(decrypted));
        }
        System.out.println(watch.stop());

        // =======================================加密－解密
        encrypted = code.ponfee.commons.jce.security.RSACryptor.encrypt(origin, pub);
        decrypted = cs.decrypt(encrypted, dk);
        if (!Arrays.equals(origin, decrypted)) {
            System.err.println("FAIL!");
        } else {
            System.out.println("\n\n=====RSA1 Decrypted text is: \n" + new String(decrypted));
        }

        // =======================================加密－解密
        encrypted = code.ponfee.commons.jce.security.RSACryptor.encrypt(origin, pri);
        decrypted = cs.decrypt(encrypted, ek);
        if (!Arrays.equals(origin, decrypted)) {
            System.err.println("FAIL!");
        } else {
            System.out.println("\n\n=====RSA1 Decrypted text is: \n" + new String(decrypted));
        }
    }

    @Test
    public void testRSAStream() throws Exception {
        RSAKey dk = new RSAKey(1024);
        Key ek = dk.getPublic();
        RSACryptor cs = new RSACryptor();

        ByteArrayInputStream input = new ByteArrayInputStream(origin);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Stopwatch watch = Stopwatch.createStarted();
        cs.encrypt(input, ek, output);
        byte[] encrypted = output.toByteArray();

        input = new ByteArrayInputStream(encrypted);
        output = new ByteArrayOutputStream();
        cs.decrypt(input, dk, output);
        byte[] decrypted = output.toByteArray();
        if (!Arrays.equals(origin, decrypted)) {
            System.err.println("FAIL!");
        } else {
            System.out.println("\n\n=====RSAStream Decrypted text is: \n" + new String(decrypted));
        }
        System.out.println(watch.stop());

        watch.reset().start();
        input = new ByteArrayInputStream(encrypted);
        output = new ByteArrayOutputStream();
        RSAPrivateKey pri = RSAPrivateKeys.toRSAPrivateKey(dk.n, dk.d);
        code.ponfee.commons.jce.security.RSACryptor.decryptWithoutPadding(input, pri, output);
        decrypted = output.toByteArray();
        if (!Arrays.equals(origin, decrypted)) {
            System.err.println("FAIL!");
        } else {
            System.out.println("\n\n=====RSAStream Decrypted text is: \n" + new String(decrypted));
        }
        System.out.println(watch.stop());
    }

    @Test
    public void testRSAPaddingStream() throws Exception {
        RSAKey dk = new RSAKey(1024);
        Key ek = dk.getPublic();
        RSACryptor cs = new RSAPKCS1PaddingCryptor();

        ByteArrayInputStream input = new ByteArrayInputStream(origin);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Stopwatch watch = Stopwatch.createStarted();
        cs.encrypt(input, ek, output);
        byte[] encrypted = output.toByteArray();

        input = new ByteArrayInputStream(encrypted);
        output = new ByteArrayOutputStream();
        cs.decrypt(input, dk, output);
        byte[] decrypted = output.toByteArray();
        if (!Arrays.equals(origin, decrypted)) {
            System.err.println("FAIL!");
        } else {
            System.out.println("\n\n=====RSAStream Decrypted text is: \n" + new String(decrypted));
        }
        System.out.println(watch.stop());

        watch.reset().start();
        input = new ByteArrayInputStream(encrypted);
        output = new ByteArrayOutputStream();
        RSAPrivateKey pri = RSAPrivateKeys.toRSAPrivateKey(dk.n, dk.d);
        code.ponfee.commons.jce.security.RSACryptor.decrypt(input, pri, output);
        decrypted = output.toByteArray();
        if (!Arrays.equals(origin, decrypted)) {
            System.err.println("FAIL!");
        } else {
            System.out.println("\n\n=====RSAStream Decrypted text is: \n" + new String(decrypted));
        }
        System.out.println(watch.stop());
    }
    
    @Test
    public void testRSAHash() throws Exception {
        RSAKey dk = new RSAKey(2048);
        Key ek = dk.getPublic();
        RSACryptor cs = new RSAHashCryptor();
        System.out.println(dk + "\n" + ek);

        byte[] encrypted = cs.encrypt(origin, origin.length, ek);
        byte[] decrypted = cs.decrypt(encrypted, dk);
        if (!Arrays.equals(origin, decrypted)) {
            System.err.println("FAIL!");
        } else {
            System.out.println("\n\n=====RSAHashCryptor Decrypted text is: \n" + new String(decrypted));
        }
    }

    @Test
    public void testRSAHashStream() throws Exception {
        RSAKey dk = new RSAKey(2048);
        Key ek = dk.getPublic();
        RSAHashCryptor cs = new RSAHashCryptor();

        ByteArrayInputStream input = new ByteArrayInputStream(origin);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Stopwatch watch = Stopwatch.createStarted();
        cs.encrypt(input, ek, output);
        byte[] encrypted = output.toByteArray();
        System.out.println("encrypted len: " + encrypted.length + ",  origin len: " + origin.length);

        input = new ByteArrayInputStream(encrypted);
        output = new ByteArrayOutputStream();
        cs.decrypt(input, dk, output);
        byte[] decrypted = output.toByteArray();
        if (!Arrays.equals(origin, decrypted)) {
            System.err.println("FAIL!");
        } else {
            System.out.println("\n\n=====RSAStream Decrypted text is: \n" + new String(decrypted));
        }
        System.out.println(watch.stop());
    }

    @Test
    public void testNull() throws Exception {
        Cryptor cs = new NullCryptor();
        Key dk = cs.generateKey();
        Key ek = dk.getPublic();

        byte[] encrypted = cs.encrypt(origin, ek);
        byte[] decrypted = cs.decrypt(encrypted, dk);
        if (!Arrays.equals(origin, decrypted)) {
            System.err.println("FAIL!");
        } else {
            System.out.println("\n\n=====NullCryptor Decrypted text is: \n" + new String(decrypted));
        }
    }

    public static void main(String[] args) throws Exception {
        //Cipher cipher = Cipher.getInstance("RSA/None/PKCS1Padding");
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        
        //Cipher cipher = Cipher.getInstance("RSA/None/NoPadding", Providers.BC);
        //Cipher cipher = Cipher.getInstance("RSA/ECB/NOPADDING", Providers.BC);
        
        //Cipher cipher = Cipher.getInstance("RSA/None/ISO9796-1PADDING", Providers.BC);
        //Cipher cipher = Cipher.getInstance("RSA/ECB/ISO9796-1PADDING", Providers.BC);
        
        //Cipher cipher = Cipher.getInstance("RSA/None/OAEPWITHMD5ANDMGF1PADDING", Providers.BC);
        //Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWITHMD5ANDMGF1PADDING", Providers.BC);
        
        //Cipher cipher = Cipher.getInstance("RSA/None/OAEPPADDING", Providers.BC);
        //Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPPADDING", Providers.BC);
        
        //Cipher cipher = Cipher.getInstance("RSA/None/OAEPWITHSHA1ANDMGF1PADDING", Providers.BC);
        //Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWITHSHA1ANDMGF1PADDING", Providers.BC);

        //Cipher cipher = Cipher.getInstance("RSA/None/OAEPWITHSHA1ANDMGF1PADDING", Providers.BC);
        //Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWITHSHA1ANDMGF1PADDING", Providers.BC);
        RSAKey dk = new RSAKey(1024);
        cipher.init(Cipher.ENCRYPT_MODE, RSAPublicKeys.toRSAPublicKey(dk.n, dk.e));
        byte[] encrypted = cipher.doFinal("123".getBytes());

        cipher.init(Cipher.DECRYPT_MODE, RSAPrivateKeys.toRSAPrivateKey(dk.n, dk.d));
        byte[] decrypted = cipher.doFinal(encrypted);
        System.out.println(new String(decrypted));
    }
}
