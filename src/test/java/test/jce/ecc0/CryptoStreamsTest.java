package test.jce.ecc0;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import code.ponfee.commons.jce.ECParameters;
import code.ponfee.commons.jce.ecc.CryptoInputStream;
import code.ponfee.commons.jce.ecc.CryptoOutputStream;
import code.ponfee.commons.jce.ecc.ECCryptor;
import code.ponfee.commons.jce.ecc.EllipticCurve;
import code.ponfee.commons.jce.ecc.Key;
import code.ponfee.commons.util.MavenProjects;

public class CryptoStreamsTest {

    public static void main(String[] args) throws Exception {
        ECCryptor cs = new ECCryptor(new EllipticCurve(ECParameters.secp160r1));
        Key sk = cs.generateKey();
        Key pk = sk.getPublic();
        //Key pk = ((ECKey)sk).getECPublic();
        
        
        /*Key sk = new RSAKey(1024);
        Key pk = sk.getPublic();
        Cryptor cs = new RSACryptor();*/

        /*CryptoSystem cs = new SillyCryptoSystem();
        Key sk = cs.generateKey();
        Key pk = sk.getPublic();*/

        InputStream in = new ByteArrayInputStream(MavenProjects.getTestJavaFileAsByteArray(CryptoStreamsTest.class));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStream out = new CryptoOutputStream(baos, cs, pk);

        byte[] buff = new byte[1024];
        for (int len; (len = in.read(buff)) != -1;) {
            out.write(buff, 0, len);
        }
        out.flush();
        in.close();
        out.close();

        in = new CryptoInputStream(new ByteArrayInputStream(baos.toByteArray()), cs, sk);
        out = new ByteArrayOutputStream();
        for (int len; (len = in.read(buff)) != -1;) {
            out.write(buff, 0, len);
        }
        out.flush();
        in.close();
        out.close();
        System.out.println(new String(((ByteArrayOutputStream)out).toByteArray()));
    }
}
