package code.ponfee.commons.jce.ecc;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.Arrays;

import static org.apache.commons.lang3.math.NumberUtils.min;

import code.ponfee.commons.io.Files;
import code.ponfee.commons.util.SecureRandoms;

/**
 * RSA Cryptor, Without padding
 * @author Ponfee
 */
public class RSAPKCS1PaddingCryptor extends RSACryptor {

    private static final byte ZERO = 0;

    public @Override byte[] encrypt(byte[] input, int length, Key ek) {
        RSAKey rsaKey = (RSAKey) ek;
        BigInteger exponent = getExponent(rsaKey);

        int originBlockSize = getOriginBlockSize(rsaKey), // 加密前原文数据块的大小
            cipherBlockSize = getCipherBlockSize(rsaKey); // 加密后密文数据块大小

        ByteArrayOutputStream out = new ByteArrayOutputStream(input.length);
        byte[] origin, encrypted;
        try {
            for (int offset = 0, len = input.length; offset < len; offset += originBlockSize) {
                // 切割并填充原文数据块
                origin = encodeBlock(input, offset, min(len, offset + originBlockSize), cipherBlockSize, rsaKey);

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
        RSAKey rsaKey = (RSAKey) dk;
        BigInteger exponent = getExponent(rsaKey);

        int cipherBlockSize = getCipherBlockSize(rsaKey); // 加密后密文数据块的大小
        ByteArrayOutputStream output = new ByteArrayOutputStream(input.length);
        byte[] encrypted, origin;
        try {
            for (int offset = 0, len = input.length; offset < len; offset += cipherBlockSize) {
                // 切割密文数据块
                encrypted = Arrays.copyOfRange(input, offset, min(len, offset + cipherBlockSize));

                // 解密：origin = encrypted^d mode n
                origin = new BigInteger(1, encrypted).modPow(exponent, rsaKey.n).toByteArray();

                // 解码数据块
                decodeBlock(origin, cipherBlockSize, output);
            }
            return output.toByteArray();
        } catch (IOException e) {
            throw new SecurityException(e); // canot happened
        }
    }

    public @Override void encrypt(InputStream input, Key ek, OutputStream output) {
        RSAKey rsaKey = (RSAKey) ek;
        BigInteger exponent = getExponent(rsaKey);
        int cipherBlockSize = getCipherBlockSize(rsaKey); // 加密后密文数据块大小

        byte[] buffer = new byte[getOriginBlockSize(rsaKey)], origin, encrypted;
        try {
            for (int len; (len = input.read(buffer)) != Files.EOF;) {
                // 切割并填充原文数据块
                origin = encodeBlock(buffer, 0, len, cipherBlockSize, rsaKey);

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

    public @Override void decrypt(InputStream input, Key dk, OutputStream output) {
        RSAKey rsaKey = (RSAKey) dk;
        BigInteger exponent = getExponent(rsaKey);

        int cipherBlockSize = getCipherBlockSize(rsaKey); // 加密后密文数据块的大小
        byte[] buffer = new byte[cipherBlockSize], encrypted, origin;
        try {
            for (int len; (len = input.read(buffer)) != Files.EOF;) {
                // 切割密文数据块
                encrypted = Arrays.copyOfRange(buffer, 0, len);

                // 解密：origin = encrypted^d mode n
                origin = new BigInteger(1, encrypted).modPow(exponent, rsaKey.n).toByteArray();

                // 解码数据块
                decodeBlock(origin, cipherBlockSize, output);
            }
            output.flush();
        } catch (IOException e) {
            throw new SecurityException(e);
        }
    }

    /**
     * 原文进行编码填充
     * 
     * EB = 00 || BT || PS || 00 || D
     * BT：公钥为0x02；私钥为0x00或0x01
     * PS：BT为0则PS全部为0x00；BT为0x01则全部为0xFF；BT为0x02则为随机数，但不能为0
     * 
     * 对于BT为00的，数据D就不能以00字节开头，因为这时候你PS填充的也是00，会分不清哪些是填充数据哪些是明文数据
     * 如果你使用私钥加密，建议你BT使用01，保证了安全性
     * 对于BT为02和01的，PS至少要有8个字节长
     * 
     * @param input   数据
     * @param from   开始位置
     * @param to     结束位置
     * @param cipherBlockSize 模长（modules/8）
     * @param rsaKey 密钥
     * @return
     */
    private byte[] encodeBlock(byte[] input, int from, int to, 
                               int cipherBlockSize, RSAKey rsaKey) {
        int length = to - from;
        if (length > cipherBlockSize) {
            throw new IllegalArgumentException("input data too large");
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream(cipherBlockSize);
        baos.write(0x00); // 00

        if (rsaKey.secret) {
            // 私钥填充
            baos.write(0x01); // BT
            for (int i = 2, pLen = cipherBlockSize - length - 1; i < pLen; i++) {
                baos.write(0xFF);
            }
        } else {
            // 公钥填充，规定此处至少要8个字节
            baos.write(0x02); // BT
            byte b;
            for (int i = 2, pLen = cipherBlockSize - length - 1; i < pLen; i++) {
                do {
                    b = (byte) SecureRandoms.nextInt();
                } while (b == ZERO);
                baos.write(b);
            }
        }

        baos.write(0x00); // 00

        baos.write(input, from, length); // D
        return baos.toByteArray();
    }

    /**
     * 解码原文填充（前缀0被舍去，只有127位）
     * @param input
     * @param cipherBlockSize
     * @param out
     * @throws IOException
     */
    private void decodeBlock(byte[] input, int cipherBlockSize, OutputStream out)
        throws IOException {
        int removedZeroLen;
        if (input[0] == ZERO) {
            removedZeroLen = 0;
        } else {
            // 前缀0已在BigInteger转byte[]时被舍去
            removedZeroLen = 1;
        }

        // 输入数据长度必须等于数据块长
        if (input.length != cipherBlockSize - removedZeroLen) {
            throw new IllegalArgumentException("block incorrect size");
        }


        // check BT
        byte type = input[1 - removedZeroLen];
        if (type != 1 && type != 2) {
            throw new IllegalArgumentException("unknown block type");
        }

        // PS
        int start = 2 - removedZeroLen;
        for (; start != input.length; start++) {
            byte pad = input[start];
            if (pad == 0) {
                break;
            }
            if (type == 1 && pad != (byte) 0xff) {
                throw new IllegalArgumentException("block padding incorrect");
            }
        }

        // get D
        start++; // data should start at the next byte
        if (start > input.length || start < 11 - removedZeroLen) {
            throw new IllegalArgumentException("no data in block");
        }
        out.write(input, start, input.length - start);
    }

    public @Override int getOriginBlockSize(RSAKey rsaKey) {
        return rsaKey.n.bitLength() / 8 - 11;
    }

}
