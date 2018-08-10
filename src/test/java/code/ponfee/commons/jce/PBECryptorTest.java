package code.ponfee.commons.jce;

import code.ponfee.commons.jce.symmetric.PBECryptor;

public class PBECryptorTest {

    public static void main(String[] args) {
        String ag = PBECryptor.ALG_PBE_SHA1_3DES;

        // 加密
        PBECryptor p = new PBECryptor(ag, "fdsafasd".toCharArray(), "12343215678".getBytes(), 1000, Providers.BC);
        byte[] encrypted = p.encrypt("abc".getBytes());

        // 解密
        p = new PBECryptor(p.getAlgorithm(), p.getPass(), p.getParameter(), p.getIterations(), p.getProvider());
        byte[] decrypted = p.decrypt(encrypted);
        System.out.println(new String(decrypted));
    }
}
