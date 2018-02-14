package code.ponfee.commons.jce.passwd;

import static code.ponfee.commons.jce.HmacAlgorithms.ALGORITHM_MAPPING;

import java.security.Provider;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.Mac;

import org.apache.commons.codec.binary.Hex;

import com.google.common.base.Preconditions;

import code.ponfee.commons.jce.HmacAlgorithms;
import code.ponfee.commons.jce.Providers;
import code.ponfee.commons.jce.hash.HmacUtils;
import code.ponfee.commons.util.SecureRandoms;

/**
 * the passwd crypt based hmac
 * @author Ponfee
 */
public class Crypt {

    private static final String SEPARATOR = "$";

    public static String create(String passwd) {
        return create(HmacAlgorithms.HmacSHA256, passwd, 32, Providers.BC);
    }

    /**
     * create crypt
     * @param alg
     * @param passwd
     * @param rounds
     * @param provider
     * @return
     */
    public static String create(HmacAlgorithms alg, String passwd, 
                                int rounds, Provider provider) {
        Preconditions.checkArgument(rounds >= 1 && rounds <= 0xff, 
                                    "iterations must between 1 and 255");

        byte[] salt = SecureRandoms.nextBytes(32);
        long algIdx = ALGORITHM_MAPPING.inverse().get(alg) & 0xf; // maximum is 0xf
        byte[] hashed = crypt(alg, passwd.getBytes(), salt, rounds, provider);

        return new StringBuilder(6 + (salt.length + hashed.length) * 4 / 3 + 4)
                    .append(SEPARATOR).append(Long.toString(algIdx << 8L | rounds, 16))
                    .append(SEPARATOR).append(toBase64(salt))
                    .append(SEPARATOR).append(toBase64(hashed))
                    .toString();
    }

    public static boolean check(String passwd, String hashed) {
        return check(passwd, hashed, null);
    }

    /**
     * check the passwd crypt
     * @param passwd
     * @param hashed
     * @param provider
     * @return {@code true} is success
     */
    public static boolean check(String passwd, String hashed, Provider provider) {
        String[] parts = hashed.split("\\" + SEPARATOR);
        if (parts.length != 4) {
            throw new IllegalArgumentException("Invalid hashed value");
        }

        long params = Long.parseLong(parts[1], 16);
        HmacAlgorithms alg = ALGORITHM_MAPPING.get((int) (params >> 8 & 0xf));
        byte[] salt = Base64.getUrlDecoder().decode(parts[2]);
        byte[] testHash = crypt(alg, passwd.getBytes(), salt, (int) params & 0xff, provider);

        // compare
        return Arrays.equals(Base64.getUrlDecoder().decode(parts[3]), testHash);
    }

    /**
     * crypt with hmac
     * @param alg
     * @param password
     * @param salt
     * @param rounds
     * @param provider
     * @return
     */
    private static byte[] crypt(HmacAlgorithms alg, byte[] password, 
                                byte[] salt, int rounds, Provider provider) {
        Mac mac = HmacUtils.getInitializedMac(alg, provider, salt);
        for (int i = 0; i < rounds; i++) {
            password = mac.doFinal(password);
        }
        return password;
    }

    private static String toBase64(byte[] data) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(data);
    }

    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        String passwd = "passwd";
        String hashed = create(passwd);
        System.out.println(hashed);
        for (int i = 0; i < 100000; i++) {
            if (!check(passwd, hashed)) {
                System.err.println("fail");
            }
        }
        System.out.println(System.currentTimeMillis()-start);

        System.out.println(Hex.encodeHexString(crypt(HmacAlgorithms.HmacSHA256, "password".getBytes(), "salt".getBytes(), 64, Providers.BC)));
    }
}
