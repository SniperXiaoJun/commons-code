package test.jce.ecc0;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

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
import code.ponfee.commons.jce.security.RSAPrivateKeys;
import code.ponfee.commons.jce.security.RSAPublicKeys;
import code.ponfee.commons.util.IdcardResolver;
import code.ponfee.commons.util.MavenProjects;

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
        System.out.println("=====Decrypted text is: \n" + new String(decrypted));
    }

    @Test
    public void testRSA() throws Exception {
        RSAKey dk = new RSAKey(1024);
        Key ek = dk.getPublic();
        Cryptor cs = new RSACryptor();
        System.out.println(dk + "\n" + ek);

        Stopwatch watch = Stopwatch.createStarted();
        byte[] encrypted = cs.encrypt(origin, ek);
        byte[] decrypted = cs.decrypt(encrypted, dk);
        System.out.println("\n\n=====Decrypted text is: \n" + new String(decrypted));
        System.out.println(watch.stop());

        watch.reset().start();
        RSAPublicKey pub = RSAPublicKeys.toRSAPublicKey(dk.n, dk.e);
        RSAPrivateKey pri = RSAPrivateKeys.toRSAPrivateKey(dk.n, dk.d);
        encrypted = code.ponfee.commons.jce.security.RSACryptor.encrypt(origin, pub);
        decrypted = code.ponfee.commons.jce.security.RSACryptor.decrypt(encrypted, pri);
        System.out.println(new String(decrypted));
        System.out.println(watch.stop());
    }

    @Test
    public void testRSAHash() throws Exception {
        RSAKey dk = new RSAKey(1024);
        Key ek = dk.getPublic();
        RSACryptor cs = new RSAHashCryptor();
        System.out.println(dk + "\n" + ek);

        byte[] encrypted = cs.encrypt(origin, origin.length, ek);
        byte[] decrypted = cs.decrypt(encrypted, dk);
        System.out.println("\n\n=====Decrypted text is: \n" + new String(decrypted));
    }

    @Test
    public void testNull() throws Exception {
        Cryptor cs = new NullCryptor();
        Key dk = cs.generateKey();
        Key ek = dk.getPublic();

        byte[] encrypted = cs.encrypt(origin, ek);
        byte[] decrypted = cs.decrypt(encrypted, dk);
        System.out.println("\n\n=====Decrypted text is: " + new String(decrypted));
    }
}
