package code.ponfee.commons.jce.ecc;

import java.security.SecureRandom;

/**
 * This interface is used to model a modern cryptosystem. It contains methods 
 * to encrypt and decrypt and methods to generate keys for the specific cryptosystem.
 * In an actual implementation it would be a good idea to initialize a key inside
 * the constructor method.
 */
public abstract class Cryptor {

    public static final SecureRandom SECURE_RANDOM = new SecureRandom();

    public final byte[] encrypt(byte[] original, Key ek) {
        return encrypt(original, original.length, ek);
    }

    /**
     * Encrypts the string p.
     * @param original the original to be encrypted.
     * @param length the original to be encrypted.
     * @param ek The (public) key to use for encryption.
     * @return the input string encrypted with the current key.
     */
    public abstract byte[] encrypt(byte[] original, int length, Key ek);

    /**  Decrypts the string c.  
     *@param cipher the ciphertext to be decrypted.
     *@param sk     the (secret) key to use for decryption.
     *@return the input string decrypted with the current key.
     */
    public abstract byte[] decrypt(byte[] cipher, Key sk);

    /** 
     * This method generates a new key for the cryptosystem.
     * @return the new key generated
     */
    public abstract Key generateKey();

    /**
     * This method returns the maximum size of blocks it can encrypt.
     * @return the maximum block size the system can encrypt. 
     */
    public int blockSize() {
        return 20;
    }

    /** Returns a String describing this CryptoSystem*/
    public abstract String toString();
}
