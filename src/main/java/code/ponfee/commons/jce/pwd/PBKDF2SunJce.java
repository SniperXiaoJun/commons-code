package code.ponfee.commons.jce.pwd;

import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import code.ponfee.commons.util.Bytes;

/**
 * PBKDF2 salted password hashing.
 * Author: havoc AT defuse.ca
 * www: http://crackstation.net/hashing-security.htm
 * 
 * The OpenJDK implementation does only provide a PBKDF2HmacSHA1Factory.java which has the "HmacSHA1" 
 * digest harcoded. As far as I tested, the Oracle JDK is not different in that sense.
 * 
 * 参考自网络 fupf
 */
public class PBKDF2SunJce {

    public static final String HMAC_SHA1 = "HmacSHA1";
    public static final String HMAC_SHA224 = "HmacSHA224";
    public static final String HMAC_SHA256 = "HmacSHA256";
    public static final String HMAC_SHA384 = "HmacSHA384";
    public static final String HMAC_SHA512 = "HmacSHA512";

    private static final int ITERATION_INDEX = 0;
    private static final int SALT_INDEX = 1;
    private static final int PBKDF2_INDEX = 2;
    private static final String SEPARATOR = "$";

    // The following constants may be changed without breaking existing hashes.
    private static final int SALT_BYTE_SIZE = 24;
    private static final int HASH_BYTE_SIZE = 24;
    private static final int PBKDF2_ITERATIONS = 64;

    public static String create(String password) {
        return create(HMAC_SHA1, password.toCharArray());
    }

    /**
     * Returns a salted PBKDF2 hash of the password.
     * @param password the password to hash
     * @return a salted PBKDF2 hash of the password
     */
    public static String create(String algorithm, String password) {
        return create(algorithm, password.toCharArray());
    }

    /**
     * Returns a salted PBKDF2 hash of the password.
     * @param password the password to hash
     * @return a salted PBKDF2 hash of the password
     */
    public static String create(String algorithm, char[] password) {
        // Generate a random salt
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_BYTE_SIZE];
        random.nextBytes(salt);

        // Hash the password
        byte[] hash = pbkdf2(algorithm, password, salt, PBKDF2_ITERATIONS, HASH_BYTE_SIZE);
        // format iterations:salt:hash
        return PBKDF2_ITERATIONS + SEPARATOR + Bytes.base64Encode(salt) + SEPARATOR + Bytes.base64Encode(hash);
    }

    public static boolean check(String password, String correctHash) {
        return check(HMAC_SHA1, password.toCharArray(), correctHash);
    }

    /**
     * Validates a password using a hash.
     * @param password the password to check
     * @param correctHash the hash of the valid password
     * @return true if the password is correct, false if not
     */
    public static boolean check(String algorithm, String password, String correctHash) {
        return check(algorithm, password.toCharArray(), correctHash);
    }

    /**
     * Validates a password using a hash.
     * @param password the password to check
     * @param correctHash the hash of the valid password
     * @return true if the password is correct, false if not
     */
    public static boolean check(String algorithm, char[] password, String correctHash) {
        // Decode the hash into its parameters
        String[] params = correctHash.split("\\" + SEPARATOR);
        int iterations = Integer.parseInt(params[ITERATION_INDEX]);
        byte[] salt = Bytes.base64Decode(params[SALT_INDEX]);
        byte[] hash = Bytes.base64Decode(params[PBKDF2_INDEX]);
        // Compute the hash of the provided password, using the same salt, 
        // iteration count, and hash length
        byte[] testHash = pbkdf2(algorithm, password, salt, iterations, hash.length);
        // Compare the hashes in constant time. The password is correct if
        // both hashes match.

        if (hash.length != testHash.length) return false;
        byte ret = 0;
        for (int i = 0; i < testHash.length; i++)
            ret |= hash[i] ^ testHash[i];
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
    private static byte[] pbkdf2(String algorithm, char[] password, byte[] salt, int iterations, int bytes) {
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
        String alg = "HMACSHA224";
        // Print out 10 hashes
        for (int i = 0; i < 10; i++)
            System.out.println(create("p\r\nassw0Rd!"));
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
        if (failure) System.out.println("TESTS FAILED!");
        else System.out.println("TESTS PASSED!");
    }

}
