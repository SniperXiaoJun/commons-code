package code.ponfee.commons.jce.ecc;

import java.util.Arrays;

public class NullCryptor extends Cryptor {

    public byte[] encrypt(byte[] input, int length, Key ek) {
        if (input.length == length) {
            return input;
        }
        return Arrays.copyOfRange(input, 0, length);
    }

    public byte[] decrypt(byte[] cipher, Key dk) {
        return cipher;
    }

    public Key generateKey() {
        return null;
    }

    public int blockSize() {
        return -1;
    }

    public String toString() {
        return "NullCryptor";
    }

}
