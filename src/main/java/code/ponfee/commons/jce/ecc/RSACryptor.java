package code.ponfee.commons.jce.ecc;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.util.Arrays;

import org.apache.commons.lang3.math.NumberUtils;

import code.ponfee.commons.jce.hash.HashUtils;
import code.ponfee.commons.util.Bytes;
import code.ponfee.commons.util.ObjectUtils;

public class RSACryptor extends Cryptor {

    private static final int SHORT_BYTE_LEN = 2;
    private static final int HEAD_ADD_LEN = 1;

    public byte[] encrypt(byte[] input, int length, Key ek) {
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
            adding(out, encryptedBlockSize - encrypted.length);

            out.write(encrypted, 0, encrypted.length);
        }
        return out.toByteArray();

        //return new BigInteger(1, input).modPow(rsaKey.e, rsaKey.n).toByteArray();
    }

    public byte[] decrypt(byte[] input, Key dk) {
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
     * (origin ⊕ passwd) ⊕ passwd = origin
     * @param input
     * @param length
     * @param ek
     * @return
     */
    public byte[] encryptByHash(byte[] input, int length, Key ek) {
        RSAKey rsaKey = (RSAKey) ek;

        // 随机生成密码（需要mod n，以免溢出）
        BigInteger passwd = new BigInteger(rsaKey.n.bitLength() + 17, Cryptor.SECURE_RANDOM);
        passwd = new BigInteger(1, Bytes.concat(passwd.toByteArray(), ObjectUtils.uuid())).mod(rsaKey.n);

        // 对密码进行RSA加密，encryptedPasswd = passwd^e mode n
        byte[] encryptedPasswd = passwd.modPow(rsaKey.e, rsaKey.n).toByteArray();

        // 对密码进行HASH
        byte[] hashedPasswd = HashUtils.sha512(passwd.toByteArray());

        int metaLen = SHORT_BYTE_LEN + encryptedPasswd.length;
        byte[] result = Arrays.copyOf(Bytes.fromShort((short) encryptedPasswd.length), metaLen + length);
        System.arraycopy(encryptedPasswd, 0, result, SHORT_BYTE_LEN, encryptedPasswd.length);
        int dLen = hashedPasswd.length;
        for (int j = 0; j < length; j++) {
            result[metaLen + j] = (byte) (input[j] ^ hashedPasswd[j % dLen]);
        }
        return result;
    }

    public byte[] decryptByHash(byte[] input, Key dk) {
        RSAKey rsaKey = (RSAKey) dk;

        // 获取被加密的密码数据
        byte[] encryptedPasswd = Arrays.copyOfRange(input, SHORT_BYTE_LEN, SHORT_BYTE_LEN + Bytes.toShort(input));

        // 解密被加密的密码数据，passwd = encryptedPasswd^d mode n
        BigInteger passwd = new BigInteger(1, encryptedPasswd).modPow(rsaKey.d, rsaKey.n);

        // 对密码进行HASH
        byte[] hashedPasswd = HashUtils.sha512(passwd.toByteArray());

        int hLen = hashedPasswd.length, kLen = SHORT_BYTE_LEN + encryptedPasswd.length;
        byte[] result = new byte[input.length - SHORT_BYTE_LEN - encryptedPasswd.length];
        for (int j = 0; j < result.length; j++) {
            result[j] = (byte) (input[kLen + j] ^ hashedPasswd[j % hLen]);
        }
        return result;
    }

    /**
     * This method generates a new key for the cryptosystem.
     * @return the new key generated
     */
    public Key generateKey() {
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

    private static void adding(ByteArrayOutputStream out, int len) {
        for (int i = 0; i < len; i++) {
            out.write((byte) 0);
        }
    }
}
