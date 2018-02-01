package test.jce.ecc0;

import code.ponfee.commons.jce.ECParameters;
import code.ponfee.commons.jce.ecc.Cryptor;
import code.ponfee.commons.jce.ecc.ECCryptor;
import code.ponfee.commons.jce.ecc.ECKey;
import code.ponfee.commons.jce.ecc.EllipticCurve;
import code.ponfee.commons.jce.ecc.Key;

public class ECCryptorTest {

    public static void main(String[] args) throws Exception {
        Cryptor cs = new ECCryptor(new EllipticCurve(ECParameters.secp256r1));
        Key sk = cs.generateKey();
        //Key pk = sk.getPublic();
        Key pk = ((ECKey) sk).getECPublic();
        System.out.println(sk + "\n" + pk);

        byte[] origin = "H".getBytes();
        byte[] encrypted = cs.encrypt(origin, origin.length, pk);
        try {
            new String((cs.encrypt(encrypted, encrypted.length, pk)));
        } catch (Exception e) {
            System.out.println("Exception!");
        }

        byte[] decrypted = cs.decrypt(encrypted, sk);
        System.out.println("Decrypted text is: " + new String(decrypted));
    }
}
