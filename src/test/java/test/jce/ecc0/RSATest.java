package test.jce.ecc0;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import code.ponfee.commons.jce.ecc.Cryptor;
import code.ponfee.commons.jce.ecc.Key;
import code.ponfee.commons.jce.ecc.RSACryptor;
import code.ponfee.commons.jce.ecc.RSAKey;
import code.ponfee.commons.jce.security.RSAPrivateKeys;
import code.ponfee.commons.jce.security.RSAPublicKeys;

public class RSATest {

    public static void main(String[] args) {
        RSAKey sk = new RSAKey(1024);
        Key pk = sk.getPublic();
        Cryptor cs = new RSACryptor();
        byte[] encrypted = cs.encrypt("123465".getBytes(), pk);
        //System.out.println(new String(cs.decrypt(encrypted, sk)));

        for (int i = 0; i < 10; i++) {
            RSAPublicKey pub = RSAPublicKeys.toRSAPublicKey(sk.moudles, sk.publicKey);
            RSAPrivateKey pri = RSAPrivateKeys.toRSAPrivateKey(sk.moudles, sk.privateKey);
            encrypted = code.ponfee.commons.jce.security.RSACryptor.encrypt("123465".getBytes(), pub);
            byte[] decrypted = code.ponfee.commons.jce.security.RSACryptor.decrypt(encrypted, pri);
            System.out.println(new String(decrypted));
        }
    }
}
