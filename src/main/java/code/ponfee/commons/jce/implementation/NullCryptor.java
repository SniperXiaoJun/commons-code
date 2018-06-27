package code.ponfee.commons.jce.implementation;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

/**
 * Null Cryptor is the do nothing
 * @author Ponfee
 */
public class NullCryptor extends Cryptor {

    public @Override byte[] encrypt(byte[] input, int length, Key ek) {
        if (input.length == length) {
            return input;
        }
        return Arrays.copyOfRange(input, 0, length);
    }

    public @Override byte[] decrypt(byte[] cipher, Key dk) {
        return cipher;
    }

    public @Override Key generateKey() {
        return new NullKey();
    }

    public @Override String toString() {
        return NullCryptor.class.getSimpleName();
    }

    private static final class NullKey implements Key {
        @Override
        public Key readKey(InputStream in) {
            return null;
        }

        @Override
        public void writeKey(OutputStream out) {}

        @Override
        public Key getPublic() {
            return null;
        }

        @Override
        public boolean isPublic() {
            return false;
        }
    }
}
