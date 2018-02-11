package code.ponfee.commons.jce.crypto;

import java.util.Arrays;

import org.apache.commons.lang3.ArrayUtils;

import code.ponfee.commons.util.MavenProjects;

/**
 * RC4 implementation
 * 
 * 给定一个短的密码，储存在key[MAX]数组里，还有一个数组S[256]，令S[i]=i。
 * 然后利用数组key来对数组S做一个置换，也就是对S数组里的数重新排列，排列算法为
 * KSA：密钥调度算法
 * j := 0
 * for i from 0 to 255
 *   j := (j + S[i] + key[i mod keyLen]) mod sboxLen
 *   swap values of S[i] and S[j]
 * endfor
 * 
 * @author Ponfee
 */
public class RC4 {

    private final static int STATE_LENGTH = 256;

    /** variables to hold the state of the RC4  during encryption and decryption */
    private final byte[] sBox;

    public RC4(byte[] keyBytes) {
        // KSA：密钥调度算法
        // 生成并填充s-box
        this.sBox = new byte[STATE_LENGTH];
        for (int i = 0; i < STATE_LENGTH; i++) {
            this.sBox[i] = (byte) i;
        }

        // 置换s-box
        for (int i = 0, j = 0, k = 0, keyLen = keyBytes.length; i < STATE_LENGTH; i++) {
            j = (j + sBox[i] + keyBytes[k++]) & 0xFF; // & 0xFF -> mode sboxLen

            ArrayUtils.swap(this.sBox, i, j);

            if (k == keyLen) {
                k = 0;
            }
        }
    }

    public byte decrypt(byte in) {
        return this.encrypt(in);
    }

    public byte encrypt(byte in) {
        byte[] sBox = Arrays.copyOf(this.sBox, this.sBox.length);
        int x = 1;
        int y = sBox[x] & 0xFF;

        ArrayUtils.swap(sBox, x, y);

        // xor
        return (byte) (in ^ sBox[(sBox[x] + sBox[y]) & 0xFF]);
    }

    public byte[] decrypt(byte[] in) {
        return this.encrypt(in);
    }

    public byte[] encrypt(byte[] in) {
        byte[] out = new byte[in.length];
        this.encrypt(in, 0, in.length, out, 0);
        return out;
    }

    public int decrypt(byte[] in, int inOff, int len, byte[] out, int outOff) {
        return this.encrypt(in, inOff, len, out, outOff);
    }

    public int encrypt(byte[] in, int inOff, int len, byte[] out, int outOff) {
        byte[] sBox = Arrays.copyOf(this.sBox, this.sBox.length);

        // RPGA：伪随机生成算法，利用上面重新排列的S盒来产生任意长度的密钥流
        for (int i = 0, x = 0, y = 0; i < len; i++) {
            x = (x + 1) & 0xFF;
            y = (sBox[x] + y) & 0xFF;

            ArrayUtils.swap(sBox, x, y);

            // xor
            out[i + outOff] = (byte) (in[i + inOff] ^ sBox[(sBox[x] + sBox[y]) & 0xFF]);
        }

        return len;
    }

    public static void main(String[] args) {
        byte[] key = "0123456789123456".getBytes();
        byte[] data = MavenProjects.getMainJavaFileAsByteArray(RC4.class);
        RC4 rc4 = new RC4(key);
        byte[] encrypted = rc4.encrypt(data);
        encrypted = rc4.encrypt(data);
        if (!Arrays.equals(rc4.decrypt(encrypted), data)
            && !Arrays.equals(rc4.decrypt(encrypted), data)) {
            System.err.println("rc4 crypt fail!");
        } else {
            //System.out.println(new String(rc4.crypt(encrypted)));
        }
        SymmetricCryptor rc = SymmetricCryptorBuilder.newBuilder(Algorithm.RC4).key(key).build();
        if (!Arrays.equals(rc.decrypt(encrypted), data)
            && !Arrays.equals(rc.decrypt(encrypted), data)) {
            System.err.println("rc4 crypt fail!");
        } else {
            //System.out.println(new String(rc4.crypt(encrypted)));
        }

        encrypted = rc.encrypt(data);
        encrypted = rc.encrypt(data);
        if (!Arrays.equals(rc4.decrypt(encrypted), data)
            && !Arrays.equals(rc4.decrypt(encrypted), data)) {
            System.err.println("rc4 crypt fail!");
        } else {
            //System.out.println(new String(rc4.decrypt(encrypted)));
        }
    }
}
