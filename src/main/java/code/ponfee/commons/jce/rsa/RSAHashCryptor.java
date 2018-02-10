package code.ponfee.commons.jce.rsa;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.Arrays;

import code.ponfee.commons.io.Files;
import code.ponfee.commons.jce.Key;
import code.ponfee.commons.jce.hash.HashUtils;
import code.ponfee.commons.util.SecureRandoms;

/**
 * RSA Cryptor based sha512 xor 
 * @author Ponfee
 */
public class RSAHashCryptor extends RSANoPaddingCryptor {

    /**
     * (origin ⊕ passwd) ⊕ passwd = origin
     * @param input
     * @param length
     * @param ek
     * @return
     */
    public @Override byte[] encrypt(byte[] input, int length, Key ek) {
        RSAKey rsaKey = (RSAKey) ek;
        int keyByteLen = rsaKey.n.bitLength() / 8;
        BigInteger exponent = getExponent(rsaKey);

        // 生成随机对称密钥
        BigInteger key = SecureRandoms.random(rsaKey.n); // mode是以1XX开头，key是以01X开头

        // 对密钥进行HASH
        byte[] hashedKey = HashUtils.sha512(key.toByteArray());

        // 对密钥进行RSA加密，encryptedKey = key^e mode n
        byte[] encryptedKey = key.modPow(exponent, rsaKey.n).toByteArray();
        encryptedKey = fixedByteArray(encryptedKey, keyByteLen); // mode pow之后可能被去0或加0

        byte[] result = Arrays.copyOf(encryptedKey, encryptedKey.length + length);
        for (int hLen = hashedKey.length, i = 0, j = 0; i < length; i++, j++) {
            if (j == hLen) {
                j = 0;
            }
            result[keyByteLen + i] = (byte) (input[i] ^ hashedKey[j]);
        }
        return result;
    }

    public @Override byte[] decrypt(byte[] input, Key dk) {
        RSAKey rsaKey = (RSAKey) dk;
        int keyByteLen = rsaKey.n.bitLength() / 8;
        BigInteger exponent = getExponent(rsaKey);

        // 获取被加密的对称密钥数据
        byte[] encryptedKey = Arrays.copyOfRange(input, 0, keyByteLen);

        // 解密被加密的密钥数据，key = encryptedKey^d mode n
        BigInteger key = new BigInteger(1, encryptedKey).modPow(exponent, rsaKey.n);

        // 对密钥进行HASH
        byte[] hashedKey = HashUtils.sha512(key.toByteArray());

        byte[] result = new byte[input.length - keyByteLen];
        for (int hLen = hashedKey.length, i = 0, j = 0; i < result.length; i++, j++) {
            if (j == hLen) {
                j = 0;
            }
            result[i] = (byte) (input[keyByteLen + i] ^ hashedKey[j]);
        }
        return result;
    }

    public @Override void encrypt(InputStream input, Key ek, OutputStream output) {
        RSAKey rsaKey = (RSAKey) ek;
        int keyByteLen = rsaKey.n.bitLength() / 8;
        BigInteger exponent = getExponent(rsaKey);

        // 生成随机对称密钥
        BigInteger key = SecureRandoms.random(rsaKey.n);

        // 对密钥进行HASH
        byte[] hashedKey = HashUtils.sha512(key.toByteArray());

        // 对密钥进行RSA加密，encryptedKey = key^e mode n
        byte[] encryptedKey = key.modPow(exponent, rsaKey.n).toByteArray();
        encryptedKey = fixedByteArray(encryptedKey, keyByteLen); // mode pow之后可能被去0或加0

        try {
            output.write(encryptedKey); // encrypted key
            byte[] buffer = new byte[this.getOriginBlockSize(rsaKey)];
            for (int hLen = hashedKey.length, len, i, j; (len = input.read(buffer)) != Files.EOF;) {
                for (i = 0, j = 0; i < len; i++, j++) {
                    if (j == hLen) {
                        j = 0;
                    }
                    output.write((byte) (buffer[i] ^ hashedKey[j]));
                }
            }
            output.flush();
        } catch (IOException e) {
            throw new SecurityException(e);
        }
    }

    public @Override void decrypt(InputStream input, Key dk, OutputStream output) {
        RSAKey rsaKey = (RSAKey) dk;
        int keyByteLen = rsaKey.n.bitLength() / 8;
        BigInteger exponent = getExponent(rsaKey);
        try {
            if (input.available() < keyByteLen) {
                throw new IllegalArgumentException("Invalid cipher data");
            }

            // 获取被加密的对称密钥数据
            byte[] encryptedKey = new byte[keyByteLen];
            input.read(encryptedKey);

            // 解密被加密的密钥数据，key = encryptedKey^d mode n
            BigInteger key = new BigInteger(1, encryptedKey).modPow(exponent, rsaKey.n);

            // 对密钥进行HASH
            byte[] hashedKey = HashUtils.sha512(key.toByteArray());

            byte[] buffer = new byte[this.getCipherBlockSize(rsaKey)];
            for (int len, hLen = hashedKey.length, i, j; (len = input.read(buffer)) != Files.EOF;) {
                for (i = 0, j = 0; i < len; i++, j++) {
                    if (j == hLen) {
                        j = 0;
                    }
                    output.write((byte) (buffer[i] ^ hashedKey[j]));
                }
            }
            output.flush();
        } catch (IOException e) {
            throw new SecurityException(e);
        }
    }

    public @Override int getOriginBlockSize(RSAKey rsaKey) {
        return 4096;
    }

    public @Override int getCipherBlockSize(RSAKey rsaKey) {
        return this.getOriginBlockSize(rsaKey);
    }

}
