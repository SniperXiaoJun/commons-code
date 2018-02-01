package test.jce.ecc0;

import code.ponfee.commons.jce.ECParameters;
import code.ponfee.commons.jce.ecc.ECCryptor;
import code.ponfee.commons.jce.ecc.EllipticCurve;

public class Main {

    public static void main(String[] args) {
        try {
            EllipticCurve ec = new EllipticCurve(ECParameters.secp256r1);
            ECCryptor cs = new ECCryptor(ec);
            new Login(null);
            //View v = new View(600,600, new RSACryptor());
        } catch (Exception e) {
            System.out.println("Error initializing system... exit(0)");
            System.exit(0);
        }
    }

}
