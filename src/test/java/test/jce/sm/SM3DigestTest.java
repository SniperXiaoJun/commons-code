package test.jce.sm;

import org.apache.commons.codec.binary.Hex;

import code.ponfee.commons.jce.sm.SM3Digest;

public class SM3DigestTest {

    public static void main(String[] args) {
        String actual = Hex.encodeHexString(SM3Digest.getInstance().doFinal("0123456789".getBytes()));
        if (!"09093b72553f5d9d622d6c62f5ffd916ee959679b1bd4d169c3e12aa8328e743".equals(actual)) {
            System.err.println("sm3 digest error!");
        } else {
            System.out.println("SUCCESS!");
        }

        byte[] data = "0123456789".getBytes();

        byte[] hash = SM3Digest.getInstance().doFinal(data);
        System.out.println(Hex.encodeHexString(hash));

        SM3Digest sm3 = SM3Digest.getInstance();

        hash = sm3.doFinal(data);
        System.out.println(Hex.encodeHexString(hash));

        hash = sm3.doFinal(data);
        System.out.println(Hex.encodeHexString(hash));

        hash = sm3.doFinal(data);
        System.out.println(Hex.encodeHexString(hash));

        System.out.println(SM3Digest.getInstance().getKey("0123456789"));
    }
}
