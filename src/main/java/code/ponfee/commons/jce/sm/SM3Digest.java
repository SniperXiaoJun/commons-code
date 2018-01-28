package code.ponfee.commons.jce.sm;

import org.apache.commons.codec.binary.Hex;

/**
 * SM3摘要算法实现
 * @author Ponfee
 */
public class SM3Digest {

    /** SM3值的长度 */
    private static final int BYTE_LENGTH = 32;

    /** SM3分组长度 */
    private static final int BLOCK_LENGTH = 64;

    /** 缓冲区长度 */
    private static final int BUFFER_LENGTH = BLOCK_LENGTH * 1;

    /** 缓冲区 */
    private byte[] xBuf = new byte[BUFFER_LENGTH];

    /** 缓冲区偏移量 */
    private int xBufOff;

    /** 初始向量 */
    private byte[] V = SM3.IV.clone();

    private int cntBlock = 0;

    public SM3Digest() {}

    public SM3Digest(SM3Digest t) {
        System.arraycopy(t.xBuf, 0, this.xBuf, 0, t.xBuf.length);
        this.xBufOff = t.xBufOff;
        System.arraycopy(t.V, 0, this.V, 0, t.V.length);
    }

    /**
     * SM3结果输出
     * @param out 保存SM3结构的缓冲区
     * @param outOff 缓冲区偏移量
     * @return
     */
    public void doFinal(byte[] out, int outOff) {
        byte[] tmp = doFinal();
        System.arraycopy(tmp, 0, out, 0, tmp.length);
    }

    public byte[] doFinal(byte[] in) {
        this.update(in);
        return this.doFinal();
    }

    public byte[] doFinal() {
        byte[] B = new byte[BLOCK_LENGTH];
        byte[] buffer = new byte[xBufOff];
        System.arraycopy(xBuf, 0, buffer, 0, buffer.length);
        byte[] tmp = SM3.padding(buffer, cntBlock);
        for (int i = 0; i < tmp.length; i += BLOCK_LENGTH) {
            System.arraycopy(tmp, i, B, 0, B.length);
            doHash(B);
        }
        return V;
    }

    public void reset() {
        xBufOff = 0;
        cntBlock = 0;
        V = SM3.IV.clone();
    }

    public void update(byte[] in) {
        this.update(in, 0, in.length);
    }

    /**
     * 明文输入
     * @param in 明文输入缓冲区
     * @param inOff 缓冲区偏移量
     * @param len 明文长度
     */
    public void update(byte[] in, int inOff, int len) {
        int partLen = BUFFER_LENGTH - xBufOff;
        int inputLen = len;
        int dPos = inOff;
        if (partLen < inputLen) {
            System.arraycopy(in, dPos, xBuf, xBufOff, partLen);
            inputLen -= partLen;
            dPos += partLen;
            doUpdate();
            while (inputLen > BUFFER_LENGTH) {
                System.arraycopy(in, dPos, xBuf, 0, BUFFER_LENGTH);
                inputLen -= BUFFER_LENGTH;
                dPos += BUFFER_LENGTH;
                doUpdate();
            }
        }

        System.arraycopy(in, dPos, xBuf, xBufOff, inputLen);
        xBufOff += inputLen;
    }

    public void update(byte in) {
        byte[] buffer = new byte[] { in };
        update(buffer, 0, 1);
    }

    public int getDigestSize() {
        return BYTE_LENGTH;
    }

    public String getKey(String inputStr) {
        byte[] md = new byte[32];
        byte[] msg1 = inputStr.getBytes();
        SM3Digest sm3 = new SM3Digest();
        sm3.update(msg1, 0, msg1.length);
        sm3.doFinal(md, 0);

        char c = string2char(inputStr);
        String finalStr = convertKey(Hex.encodeHexString(md), c);

        String hahaStr = "";
        char[] arr = finalStr.toCharArray();
        for (int i = 0; i < arr.length / 2; i++) {
            int integer1 = arr[i];
            int integer2 = arr[arr.length - 1 - i];
            int integer = (integer1 + integer2) % 95 + 32;
            char ch = (char) integer;
            hahaStr += ch;
        }
        return hahaStr.substring(4, 20);
    }

    private void doUpdate() {
        byte[] B = new byte[BLOCK_LENGTH];
        for (int i = 0; i < BUFFER_LENGTH; i += BLOCK_LENGTH) {
            System.arraycopy(xBuf, i, B, 0, B.length);
            doHash(B);
        }
        xBufOff = 0;
    }

    private void doHash(byte[] B) {
        byte[] tmp = SM3.cf(V, B);
        System.arraycopy(tmp, 0, V, 0, V.length);
        cntBlock++;
    }

    private static char string2char(String string) {
        int n = 0;
        for (int i = 0; i < string.length(); i++) {
            n += string.charAt(i);
        }
        n = n % 95 + 32;
        return (char) n;
    }

    private static String convertKey(String inStr, char c) {
        char[] a = inStr.toCharArray();
        for (int i = 0; i < a.length; i++) {
            a[i] = (char) (a[i] ^ c);
        }
        String s = new String(a);
        return s;
    }

    private static class SM3 {
        static final byte[] IV = {
            0x73, (byte) 0x80, 0x16, 0x6f, 0x49,
            0x14, (byte) 0xb2, (byte) 0xb9, 0x17, 0x24, 0x42, (byte) 0xd7,
            (byte) 0xda, (byte) 0x8a, 0x06, 0x00, (byte) 0xa9, 0x6f, 0x30,
            (byte) 0xbc, (byte) 0x16, 0x31, 0x38, (byte) 0xaa, (byte) 0xe3,
            (byte) 0x8d, (byte) 0xee, 0x4d, (byte) 0xb0, (byte) 0xfb, 0x0e,
            0x4e
        };

        static final int[] T_J = new int[64];
        static {
            for (int i = 0; i < 16; i++) {
                T_J[i] = 0x79cc4519;
            }

            for (int i = 16; i < 64; i++) {
                T_J[i] = 0x7a879d8a;
            }
        }

        static byte[] cf(byte[] V, byte[] B) {
            int[] v, b;
            v = convert(V);
            b = convert(B);
            return convert(cf(v, b));
        }

        /**
         * 对最后一个分组字节数据padding
         * @param in
         * @param bLen 分组个数
         * @return
         */
        static byte[] padding(byte[] in, int bLen) {
            int k = 448 - (8 * in.length + 1) % 512;
            if (k < 0) {
                k = 960 - (8 * in.length + 1) % 512;
            }
            k += 1;
            byte[] padd = new byte[k / 8];
            padd[0] = (byte) 0x80;
            long n = in.length * 8 + bLen * 512;
            byte[] out = new byte[in.length + k / 8 + 64 / 8];
            int pos = 0;
            System.arraycopy(in, 0, out, 0, in.length);
            pos += in.length;
            System.arraycopy(padd, 0, out, pos, padd.length);
            pos += padd.length;
            byte[] tmp = back(longToByteArray(n));
            System.arraycopy(tmp, 0, out, pos, tmp.length);
            return out;
        }

        static int[] convert(byte[] arr) {
            int[] out = new int[arr.length / 4];
            byte[] tmp = new byte[4];
            for (int i = 0; i < arr.length; i += 4) {
                System.arraycopy(arr, i, tmp, 0, 4);
                out[i / 4] = bigEndianByteToInt(tmp);
            }
            return out;
        }

        static byte[] convert(int[] arr) {
            byte[] out = new byte[arr.length * 4];
            byte[] tmp = null;
            for (int i = 0; i < arr.length; i++) {
                tmp = bigEndianIntToByte(arr[i]);
                System.arraycopy(tmp, 0, out, i * 4, 4);
            }
            return out;
        }

        static int[] cf(int[] V, int[] B) {
            int a, b, c, d, e, f, g, h;
            int ss1, ss2, tt1, tt2;
            a = V[0];
            b = V[1];
            c = V[2];
            d = V[3];
            e = V[4];
            f = V[5];
            g = V[6];
            h = V[7];

            int[][] arr = expand(B);
            int[] w = arr[0];
            int[] w1 = arr[1];

            for (int j = 0; j < 64; j++) {
                ss1 = (bitCycleLeft(a, 12) + e + bitCycleLeft(T_J[j], j));
                ss1 = bitCycleLeft(ss1, 7);
                ss2 = ss1 ^ bitCycleLeft(a, 12);
                tt1 = FFj(a, b, c, j) + d + ss2 + w1[j];
                tt2 = GGj(e, f, g, j) + h + ss1 + w[j];
                d = c;
                c = bitCycleLeft(b, 9);
                b = a;
                a = tt1;
                h = g;
                g = bitCycleLeft(f, 19);
                f = e;
                e = P0(tt2);
            }

            int[] out = new int[8];
            out[0] = a ^ V[0];
            out[1] = b ^ V[1];
            out[2] = c ^ V[2];
            out[3] = d ^ V[3];
            out[4] = e ^ V[4];
            out[5] = f ^ V[5];
            out[6] = g ^ V[6];
            out[7] = h ^ V[7];

            return out;
        }

        static int[][] expand(int[] B) {
            int W[] = new int[68];
            int W1[] = new int[64];
            for (int i = 0; i < B.length; i++) {
                W[i] = B[i];
            }

            for (int i = 16; i < 68; i++) {
                W[i] = P1(W[i - 16] ^ W[i - 9] ^ bitCycleLeft(W[i - 3], 15))
                    ^ bitCycleLeft(W[i - 13], 7) ^ W[i - 6];
            }

            for (int i = 0; i < 64; i++) {
                W1[i] = W[i] ^ W[i + 4];
            }

            int arr[][] = new int[][] { W, W1 };
            return arr;
        }

        static byte[] bigEndianIntToByte(int num) {
            return back(intToByteArray(num));
        }

        static int bigEndianByteToInt(byte[] bytes) {
            return byteArrayToInt(back(bytes));
        }

        static int FFj(int X, int Y, int Z, int j) {
            if (j >= 0 && j <= 15) {
                return FF1j(X, Y, Z);
            } else {
                return FF2j(X, Y, Z);
            }
        }

        static int GGj(int X, int Y, int Z, int j) {
            if (j >= 0 && j <= 15) {
                return GG1j(X, Y, Z);
            } else {
                return GG2j(X, Y, Z);
            }
        }

        // 逻辑位运算函数
        static int FF1j(int X, int Y, int Z) {
            int tmp = X ^ Y ^ Z;
            return tmp;
        }

        static int FF2j(int X, int Y, int Z) {
            int tmp = ((X & Y) | (X & Z) | (Y & Z));
            return tmp;
        }

        static int GG1j(int X, int Y, int Z) {
            int tmp = X ^ Y ^ Z;
            return tmp;
        }

        static int GG2j(int X, int Y, int Z) {
            int tmp = (X & Y) | (~X & Z);
            return tmp;
        }

        static int P0(int X) {
            int y = rotateLeft(X, 9);
            y = bitCycleLeft(X, 9);
            int z = rotateLeft(X, 17);
            z = bitCycleLeft(X, 17);
            int t = X ^ y ^ z;
            return t;
        }

        static int P1(int X) {
            int t = X ^ bitCycleLeft(X, 15) ^ bitCycleLeft(X, 23);
            return t;
        }

        /**
         * 字节数组逆序
         * 
         * @param in
         * @return
         */
        static byte[] back(byte[] in) {
            byte[] out = new byte[in.length];
            for (int i = 0; i < out.length; i++) {
                out[i] = in[out.length - i - 1];
            }
            return out;
        }

        static int rotateLeft(int x, int n) {
            return (x << n) | (x >> (32 - n));
        }

        static int bitCycleLeft(int n, int bitLen) {
            bitLen %= 32;
            byte[] tmp = bigEndianIntToByte(n);
            int byteLen = bitLen / 8;
            int len = bitLen % 8;
            if (byteLen > 0) {
                tmp = byteCycleLeft(tmp, byteLen);
            }

            if (len > 0) {
                tmp = bitSmall8CycleLeft(tmp, len);
            }

            return bigEndianByteToInt(tmp);
        }

        static byte[] bitSmall8CycleLeft(byte[] in, int len) {
            byte[] tmp = new byte[in.length];
            int t1, t2, t3;
            for (int i = 0; i < tmp.length; i++) {
                t1 = (byte) ((in[i] & 0x000000ff) << len);
                t2 = (byte) ((in[(i + 1) % tmp.length] & 0x000000ff) >> (8 - len));
                t3 = (byte) (t1 | t2);
                tmp[i] = (byte) t3;
            }

            return tmp;
        }

        static byte[] byteCycleLeft(byte[] in, int byteLen) {
            byte[] tmp = new byte[in.length];
            System.arraycopy(in, byteLen, tmp, 0, in.length - byteLen);
            System.arraycopy(in, 0, tmp, in.length - byteLen, byteLen);
            return tmp;
        }

        /**
         * 整形转换成网络传输的字节流（字节数组）型数据
         * @param num 一个整型数据
         * @return 4个字节的自己数组
         */
        static byte[] intToByteArray(int num) {
            byte[] bytes = new byte[4];
            bytes[0] = (byte) (0xff & (num >> 0));
            bytes[1] = (byte) (0xff & (num >> 8));
            bytes[2] = (byte) (0xff & (num >> 16));
            bytes[3] = (byte) (0xff & (num >> 24));
            return bytes;
        }

        /**
         * 四个字节的字节数据转换成一个整形数据
         * @param bytes 4个字节的字节数组
         * @return 一个整型数据
         */
        static int byteArrayToInt(byte[] bytes) {
            int num = 0;
            int temp;
            temp = (0x000000ff & (bytes[0])) << 0;
            num = num | temp;
            temp = (0x000000ff & (bytes[1])) << 8;
            num = num | temp;
            temp = (0x000000ff & (bytes[2])) << 16;
            num = num | temp;
            temp = (0x000000ff & (bytes[3])) << 24;
            num = num | temp;
            return num;
        }

        /**
         * 长整形转换成网络传输的字节流（字节数组）型数据
         * @param num 一个长整型数据
         * @return 4个字节的自己数组
         */
        static byte[] longToByteArray(long num) {
            byte[] bytes = new byte[8];
            for (int i = 0; i < 8; i++) {
                bytes[i] = (byte) (0xff & (num >> (i * 8)));
            }

            return bytes;
        }
    }

    public static void main(String[] args) {
        byte[] hash = new SM3Digest().doFinal("ererfeiisgod".getBytes());
        System.out.println(Hex.encodeHexString(hash));

        hash = new SM3Digest().doFinal("ererfeiisgod".getBytes());
        System.out.println(Hex.encodeHexString(hash));
    }
}
