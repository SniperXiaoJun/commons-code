package code.ponfee.commons.jce.ecc;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.util.Arrays;

import org.apache.commons.lang3.math.NumberUtils;

/**
 * RSA Cryptor 
 * @author Ponfee
 */
public class RSACryptor extends Cryptor {

    private static final int HEAD_ADD_LEN = 1;

    public @Override byte[] encrypt(byte[] input, int length, Key ek) {
        RSAKey rsaKey = (RSAKey) ek;
        int originBlockSize = getBlockSize(ek); // 加密前原文数据块的大小
        int encryptedBlockSize = rsaKey.n.bitLength() / 8 + HEAD_ADD_LEN; // 加密后密文数据块的大小
        ByteArrayOutputStream out = new ByteArrayOutputStream(input.length);
        byte[] origin, encrypted;
        for (int offSet = 0, len = input.length; offSet < len; offSet += originBlockSize) {
            // 切割原文数据块
            origin = Arrays.copyOfRange(input, offSet, NumberUtils.min(len, offSet + originBlockSize));

            // 加密：encrypted = origin^e mode n
            encrypted = new BigInteger(1, origin).modPow(rsaKey.e, rsaKey.n).toByteArray();

            // 加前缀0补全到固定字节数：encryptedBlockSize
            heading(out, encryptedBlockSize - encrypted.length);

            out.write(encrypted, 0, encrypted.length);
        }
        return out.toByteArray();

        //return new BigInteger(1, input).modPow(rsaKey.e, rsaKey.n).toByteArray();
    }

    public @Override byte[] decrypt(byte[] input, Key dk) {
        RSAKey rsaKey = (RSAKey) dk;

        int encryptedBlockSize = rsaKey.n.bitLength() / 8 + HEAD_ADD_LEN; // 加密后密文数据块的大小
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] encrypted, origin;
        for (int offSet = 0, len = input.length; offSet < len; offSet += encryptedBlockSize) {
            // 切割密文数据块
            encrypted = Arrays.copyOfRange(input, offSet, NumberUtils.min(len, offSet + encryptedBlockSize));

            // 解密：origin = encrypted^d mode n
            origin = new BigInteger(1, encrypted).modPow(rsaKey.d, rsaKey.n).toByteArray();

            out.write(origin, 0, origin.length);
        }
        return out.toByteArray();

        //return new BigInteger(1, input).modPow(rsaKey.d, rsaKey.n).toByteArray();
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

    public @Override int getBlockSize(Key ek) {
        return ((RSAKey) ek).n.bitLength() / 8 - 11;
    }

    public String toString() {
        return "RSACryptor";
    }

    private static void heading(ByteArrayOutputStream out, int len) {
        for (int i = 0; i < len; i++) {
            out.write((byte) 0);
        }
    }
}
