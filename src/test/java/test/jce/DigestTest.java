package test.jce;

import org.apache.commons.codec.binary.Hex;

import code.ponfee.commons.jce.DigestAlgorithms;
import code.ponfee.commons.jce.HmacAlgorithms;
import code.ponfee.commons.jce.Providers;
import code.ponfee.commons.jce.digest.DigestUtils;
import code.ponfee.commons.jce.digest.HmacUtils;
import code.ponfee.commons.util.SecureRandoms;

public class DigestTest {

    public static void main(String[] args) {
        byte[] data = SecureRandoms.nextBytes(1204);
        byte[] key = SecureRandoms.nextBytes(1204);
        //System.out.println(Hex.encodeHexString(DigestUtils.digest(DigestAlgorithms.SHAKE128, Providers.BC, data)));
        //System.out.println(Hex.encodeHexString(HmacUtils.crypt(key, data, HmacAlgorithms.HmacSHAKE256)));

        System.out.println(Hex.encodeHexString(DigestUtils.digest(DigestAlgorithms.SM3, Providers.BC, data)));
        //System.out.println(Hex.encodeHexString(HmacUtils.crypt(key, data, HmacAlgorithms.HmacSM3)));

        System.out.println(Hex.encodeHexString(DigestUtils.digest(DigestAlgorithms.SKEIN_512_256, Providers.BC, data)));
        System.out.println(Hex.encodeHexString(HmacUtils.crypt(key, data, HmacAlgorithms.HmacSKEIN_512_256)));

        System.out.println(Hex.encodeHexString(DigestUtils.digest(DigestAlgorithms.SKEIN_1024_1024, Providers.BC, data)));
        System.out.println(Hex.encodeHexString(HmacUtils.crypt(key, data, HmacAlgorithms.HmacSKEIN_1024_1024)));
    }
}
