package code.ponfee.commons.jce.pwd;

import static code.ponfee.commons.jce.HmacAlgorithm.ALGORITHM_MAPPING;

import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import com.google.common.base.Preconditions;

import code.ponfee.commons.jce.HmacAlgorithm;
import code.ponfee.commons.util.SecureRandoms;

/**
 * PBKDF2 salted password hashing.
 * Author: havoc AT defuse.ca
 * www: http://crackstation.net/hashing-security.htm
 * 
 * The OpenJDK implementation does only provide a PBKDF2HmacSHA1Factory.java which has the "HmacSHA1" 
 * digest harcoded. As far as I tested, the Oracle JDK is not different in that sense.
 * 
 * @author havoc AT defuse.ca
 * @author Ponfee
 * Reference from internet and with optimization
 */
public final class PBKDF2 {
    private PBKDF2() {}

    private static final String SEPARATOR = "$";

    public static String create(String password) {
        return create(HmacAlgorithm.HmacSHA256, password.toCharArray());
    }

    public static String create(HmacAlgorithm alg, String password) {
        return create(alg, password.toCharArray());
    }

    public static String create(HmacAlgorithm alg, char[] password) {
        return create(alg, password, 16, 64, 32);
    }

    /**
     * Returns a salted PBKDF2 hash of the password.
     * @param alg                HmacAlgorithm, HmacAlgorithm.HmacMD5 is invalid
     * @param password           the password to hash
     * @param saltByteSize       the byte length of random slat
     * @param iterationCount     the iteration count (slowness factor)
     * @param dkLen              Intended length, in octets, of the derived key.
     * @return a salted PBKDF2 hash of the password
     */
    public static String create(HmacAlgorithm alg, char[] password, int saltByteSize,
                                int iterationCount, int dkLen) {
        Preconditions.checkArgument(iterationCount >= 1 && iterationCount <= 0xffff, 
                                    "iterations must between 1 and 65535");
        // Generate a random salt
        byte[] salt = SecureRandoms.nextBytes(saltByteSize);

        // Hash the password
        byte[] hash = pbkdf2(alg, password, salt, iterationCount, dkLen);

        long algIdx = ALGORITHM_MAPPING.inverse().get(alg) & 0xf; // maximum is 0xf
        String params = Long.toString(algIdx << 16L | iterationCount, 16);

        // format iterations:salt:hash
        return new StringBuilder(8 + (salt.length + hash.length) * 4 / 3 + 4)
                .append(SEPARATOR).append(params)
                .append(SEPARATOR).append(toBase64(salt))
                .append(SEPARATOR).append(toBase64(hash))
                .toString();
    }

    private static String toBase64(byte[] data) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(data);
    }

    /**
     * Validates a password using a hash.
     * @param password the password to check
     * @param correctHash the hash of the valid password
     * @return true if the password is correct, false if not
     */
    public static boolean check(String password, String correctHash) {
        return check(password.toCharArray(), correctHash);
    }

    /**
     * Validates a password using a hash.
     * @param password the password to check
     * @param correctHash the hash of the valid password
     * @return true if the password is correct, false if not
     */
    public static boolean check(char[] password, String correctHash) {
        // Decode the hash into its parameters
        String[] parts = correctHash.split("\\" + SEPARATOR);
        if (parts.length != 4) {
            throw new IllegalArgumentException("Invalid hashed value");
        }

        long params = Long.parseLong(parts[1], 16);
        HmacAlgorithm alg = ALGORITHM_MAPPING.get((int) (params >> 16 & 0xf));
        int iterations = (int) params & 0xffff;
        byte[] salt = Base64.getUrlDecoder().decode(parts[2]);
        byte[] hash = Base64.getUrlDecoder().decode(parts[3]);
        // Compute the hash of the provided password, using the same salt, 
        // iteration count, and hash length
        byte[] testHash = pbkdf2(alg, password, salt, iterations, hash.length);
        // Compare the hashes in constant time. The password is correct if
        // both hashes match.

        return Arrays.equals(hash, testHash);
    }

    /**
     * Computes the PBKDF2 hash of a password.
     * @param alg             the HmacAlgorithm
     * @param password        the password to hash
     * @param salt            the salt
     * @param iterationCount  the iteration count (slowness factor)
     * @param dkLen           the length of the hash to compute in bytes
     * @return the PBDKF2 hash of the password
     */
    private static byte[] pbkdf2(HmacAlgorithm alg, char[] password, byte[] salt,
                                 int iterationCount, int dkLen) {
        PBEKeySpec spec = new PBEKeySpec(password, salt, iterationCount, dkLen * 8);
        try {
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2With" + alg.name());
            return skf.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new SecurityException(e);
        }
    }

    /**
     * Tests the basic functionality of the PasswordHash class
     * @param args ignored
     * @throws GeneralSecurityException
     */
    public static void main(String[] args) {
        // Print out 10 hashes
        for (int i = 0; i < 10; i++) {
            System.out.println(create(HmacAlgorithm.HmacSHA256, "p\r\nassw0Rd!".toCharArray(), 16, 65535, 32));
        }
        System.out.println("============================================\n");

        // Test password validation
        HmacAlgorithm alg = HmacAlgorithm.HmacSHA384;
        boolean failure = false;
        System.out.println("Running tests...");
        String passwd = "password";
        String hashed = create(alg, passwd);
        System.out.println(hashed);
        long start = System.currentTimeMillis();
        for (int i = 0; i < 100000; i++) { // 20 seconds
            if (!check(passwd, hashed)) {
                System.out.println("FAILURE: GOOD PASSWORD NOT ACCEPTED!");
                failure = true;
                break;
            }
        }
        System.out.println("cost: "+(System.currentTimeMillis()-start)/1000);
        if (failure) {
            System.out.println("TESTS FAILED!");
        } else {
            System.out.println("TESTS PASSED!");
        }
    }

}
