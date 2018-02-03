package code.ponfee.commons.jce.ecc;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.Arrays;

import static org.apache.commons.lang3.math.NumberUtils.min;

import code.ponfee.commons.io.Files;

/**
 * RSA Cryptor, Without padding
 * @author Ponfee
 */
public class RSACryptor extends Cryptor {

    private static final byte ZERO = 0;

    public @Override byte[] encrypt(byte[] input, int length, Key ek) {
        //return new BigInteger(1, input).modPow(rsaKey.e, rsaKey.n).toByteArray();
        RSAKey rsaKey = (RSAKey) ek;
        BigInteger exponent = getExponent(rsaKey);

        int originBlockSize = getOriginBlockSize(rsaKey), // 加密前原文数据块的大小
            cipherBlockSize = getCipherBlockSize(rsaKey); // 加密后密文数据块大小

        ByteArrayOutputStream out = new ByteArrayOutputStream(input.length);
        byte[] origin, encrypted;
        try {
            for (int offset = 0, len = input.length; offset < len; offset += originBlockSize) {
                // 切割原文数据块
                origin = Arrays.copyOfRange(input, offset, min(len, offset + originBlockSize));

                // 加密：encrypted = origin^e mode n
                encrypted = new BigInteger(1, origin).modPow(exponent, rsaKey.n).toByteArray();

                // 固定密文长度
                fixedByteArray(encrypted, cipherBlockSize, out);
            }
            return out.toByteArray();
        } catch (IOException e) {
            throw new SecurityException(e); // canot happened
        }
    }

    public @Override byte[] decrypt(byte[] input, Key dk) {
        //return new BigInteger(1, input).modPow(rsaKey.d, rsaKey.n).toByteArray();
        RSAKey rsaKey = (RSAKey) dk;
        BigInteger exponent = getExponent(rsaKey);

        int cipherBlockSize = getCipherBlockSize(rsaKey), // 加密后密文数据块的大小
            originBlockSize = getOriginBlockSize(rsaKey);
        ByteArrayOutputStream output = new ByteArrayOutputStream(input.length);
        byte[] encrypted, origin;
        try {
            for (int offset = 0, len = input.length; offset < len; offset += cipherBlockSize) {
                // 切割密文数据块
                encrypted = Arrays.copyOfRange(input, offset, min(len, offset + cipherBlockSize));

                // 解密：origin = encrypted^d mode n
                origin = new BigInteger(1, encrypted).modPow(exponent, rsaKey.n).toByteArray();

                if (offset + cipherBlockSize < len) {
                    // 固定明文长度
                    fixedByteArray(origin, originBlockSize, output);
                } else {
                    // 去掉原文前缀0
                    trimByteArray(origin, output);
                }
            }
            return output.toByteArray();
        } catch (IOException e) {
            throw new SecurityException(e); // canot happened
        }
    }

    public void encrypt(InputStream input, Key ek, OutputStream output) {
        RSAKey rsaKey = (RSAKey) ek;
        BigInteger exponent = getExponent(rsaKey);
        int cipherBlockSize = getCipherBlockSize(rsaKey); // 加密后密文数据块大小

        byte[] buffer = new byte[getOriginBlockSize(rsaKey)], origin, encrypted;
        try {
            for (int len; (len = input.read(buffer)) != Files.EOF;) {
                // 切割原文数据块
                origin = Arrays.copyOfRange(buffer, 0, len);

                // 加密：encrypted = origin^e mode n
                encrypted = new BigInteger(1, origin).modPow(exponent, rsaKey.n).toByteArray();

                // 固定密文长度
                fixedByteArray(encrypted, cipherBlockSize, output);
            }
            output.flush();
        } catch (IOException e) {
            throw new SecurityException(e);
        }
    }

    public void decrypt(InputStream input, Key dk, OutputStream output) {
        RSAKey rsaKey = (RSAKey) dk;
        BigInteger exponent = getExponent(rsaKey);

        int cipherBlockSize = getCipherBlockSize(rsaKey), // 加密后密文数据块的大小
            originBlockSize = getOriginBlockSize(rsaKey);
        byte[] buffer = new byte[cipherBlockSize], encrypted, origin;
        try {
            int inputLen = input.available();
            for (int len, offset = 0; (len = input.read(buffer)) != Files.EOF; offset += cipherBlockSize) {
                // 切割密文数据块
                encrypted = Arrays.copyOfRange(buffer, 0, len);

                // 解密：origin = encrypted^d mode n
                origin = new BigInteger(1, encrypted).modPow(exponent, rsaKey.n).toByteArray();

                if (offset + cipherBlockSize < inputLen) {
                    // 固定明文长度
                    fixedByteArray(origin, originBlockSize, output);
                } else {
                    // 去掉原文前缀0
                    trimByteArray(origin, output);
                }
            }
            output.flush();
        } catch (IOException e) {
            throw new SecurityException(e);
        }
    }

    /**
     * This method generates a new key for the cryptosystem.
     * @return the new key generated
     */
    public @Override Key generateKey() {
        return generateKey(2048);
    }

    public Key generateKey(int keySize) {
        return new RSAKey(keySize);
    }

    public String toString() {
        return this.getClass().getSimpleName();
    }

    public void fixedByteArray(byte[] data, int fixedSize, OutputStream out)
        throws IOException {
        if (data.length < fixedSize) {
            // 加前缀0补全到固定字节数：encryptedBlockSize
            for (int i = 0, heading = fixedSize - data.length; i < heading; i++) {
                out.write(ZERO);
            }
            out.write(data, 0, data.length);
        } else {
            // 舍去前面的0
            out.write(data, data.length - fixedSize, fixedSize);
        }
    }

    public void trimByteArray(byte[] data, OutputStream out)
        throws IOException {
        int i = 0, len = data.length;
        for (; i < len; i++) {
            if (data[i] != ZERO) {
                break;
            }
        }
        if (i < len) {
            out.write(data, i, len - i);
        }
    }

    public int getOriginBlockSize(RSAKey rsaKey) {
        // 减一个byte为了防止溢出
        // 此时BigInteger(1, byte[getOriginBlockSize(rsaKey)]) < rsaKey.n
        return rsaKey.n.bitLength() / 8 - 1;
    }

    public int getCipherBlockSize(RSAKey rsaKey) {
        return rsaKey.n.bitLength() / 8;
    }

    public BigInteger getExponent(RSAKey rsaKey) {
        return rsaKey.secret ? rsaKey.d : rsaKey.e;
    }
}
