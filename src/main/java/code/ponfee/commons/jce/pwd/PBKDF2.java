package code.ponfee.commons.jce.pwd;

import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

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
public class PBKDF2 {

    private static final String SEPARATOR = "$";

    public static String create(String password) {
        return create(HmacAlgorithm.HmacSHA256, password.toCharArray());
    }

    public static String create(HmacAlgorithm alg, String password) {
        return create(alg, password.toCharArray());
    }

    public static String create(HmacAlgorithm alg, char[] password) {
        return create(alg, password, 24, 24, 64);
    }

    /**
     * Returns a salted PBKDF2 hash of the password.
     * @param alg Hmac Algorithm
     * @param password the password to hash
     * @param saltByteSize
     * @param hashByteSize
     * @param pbkdf2Iterations
     * @return a salted PBKDF2 hash of the password
     */
    public static String create(HmacAlgorithm alg, char[] password, int saltByteSize,
                                int hashByteSize, int pbkdf2Iterations) {
        // Generate a random salt
        byte[] salt = SecureRandoms.nextBytes(saltByteSize);

        // Hash the password
        byte[] hash = pbkdf2(alg.name(), password, salt, pbkdf2Iterations, hashByteSize);

        // format iterations:salt:hash
        return pbkdf2Iterations + SEPARATOR + toBase64(salt) + SEPARATOR + toBase64(hash);
    }

    private static String toBase64(byte[] data) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(data);
    }

    public static boolean check(String password, String correctHash) {
        return check(HmacAlgorithm.HmacSHA256, password.toCharArray(), correctHash);
    }

    /**
     * Validates a password using a hash.
     * @param password the password to check
     * @param correctHash the hash of the valid password
     * @return true if the password is correct, false if not
     */
    public static boolean check(HmacAlgorithm algorithm, String password,
                                String correctHash) {
        return check(algorithm, password.toCharArray(), correctHash);
    }

    /**
     * Validates a password using a hash.
     * @param password the password to check
     * @param correctHash the hash of the valid password
     * @return true if the password is correct, false if not
     */
    public static boolean check(HmacAlgorithm alg, char[] password, String correctHash) {
        // Decode the hash into its parameters
        String[] params = correctHash.split("\\" + SEPARATOR);
        int iterations = Integer.parseInt(params[0]);
        byte[] salt = Base64.getUrlDecoder().decode(params[1]);
        byte[] hash = Base64.getUrlDecoder().decode(params[2]);
        // Compute the hash of the provided password, using the same salt, 
        // iteration count, and hash length
        byte[] testHash = pbkdf2(alg.name(), password, salt, iterations, hash.length);
        // Compare the hashes in constant time. The password is correct if
        // both hashes match.

        if (hash.length != testHash.length) {
            return false;
        }
        byte ret = 0;
        for (int i = 0; i < testHash.length; i++) {
            ret |= hash[i] ^ testHash[i];
        }
        return ret == 0;
    }

    /**
     * Computes the PBKDF2 hash of a password.
     * @param password the password to hash.
     * @param salt the salt
     * @param iterations the iteration count (slowness factor)
     * @param bytes the length of the hash to compute in bytes
     * @return the PBDKF2 hash of the password
     */
    private static byte[] pbkdf2(String algorithm, char[] password, byte[] salt,
                                 int iterations, int bytes) {
        PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, bytes * 8);
        try {
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2With" + algorithm);
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
        HmacAlgorithm alg = HmacAlgorithm.HmacSHA256;
        // Print out 10 hashes
        for (int i = 0; i < 10; i++) {
            System.out.println(create("p\r\nassw0Rd!"));
        }
        System.out.println("============================================\n");

        // Test password validation
        boolean failure = false;
        System.out.println("Running tests...");
        for (int i = 0; i < 1000; i++) {
            String password = "" + i;
            String hash = create(alg, password);
            String secondHash = create(alg, password);
            if (hash.equals(secondHash)) {
                System.out.println("FAILURE: TWO HASHES ARE EQUAL!");
                failure = true;
            }
            String wrongPassword = "" + (i + 1);
            if (check(alg, wrongPassword, hash)) {
                System.out.println("FAILURE: WRONG PASSWORD ACCEPTED!");
                failure = true;
            }
            if (!check(alg, password, hash)) {
                System.out.println("FAILURE: GOOD PASSWORD NOT ACCEPTED!");
                failure = true;
            }
        }
        if (failure) {
            System.out.println("TESTS FAILED!");
        } else {
            System.out.println("TESTS PASSED!");
        }
    }

}
