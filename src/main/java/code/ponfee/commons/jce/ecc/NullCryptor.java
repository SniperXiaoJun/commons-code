package code.ponfee.commons.jce.ecc;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
        return new NullKey();
    }

    public int blockSize() {
        return -1;
    }

    public String toString() {
        return "NullCryptor";
    }

    private static final class NullKey implements Key {
        @Override
        public Key readKey(InputStream in) throws IOException {
            return null;
        }

        @Override
        public void writeKey(OutputStream out) throws IOException {}

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
