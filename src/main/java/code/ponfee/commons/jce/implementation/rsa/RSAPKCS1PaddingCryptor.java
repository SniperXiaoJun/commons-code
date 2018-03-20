package code.ponfee.commons.jce.implementation.rsa;

import java.io.InputStream;
import java.io.OutputStream;

import code.ponfee.commons.jce.implementation.Key;

/**
 * RSA Cryptor with PKCS1 padding
 * @author Ponfee
 */
public class RSAPKCS1PaddingCryptor extends RSANoPaddingCryptor {

    public @Override byte[] encrypt(byte[] input, int length, Key ek) {
        return encrypt(input, length, ek, true);
    }

    public @Override byte[] decrypt(byte[] input, Key dk) {
        return decrypt(input, dk, true);
    }

    public @Override void encrypt(InputStream input, Key ek, OutputStream output) {
        encrypt(input, ek, output, true);
    }

    public @Override void decrypt(InputStream input, Key dk, OutputStream output) {
        decrypt(input, dk, output, true);
    }

    public @Override int getOriginBlockSize(RSAKey rsaKey) {
        return rsaKey.n.bitLength() / 8 - 11;
    }

}
