package code.ponfee.commons.jce.hash;

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.codec.binary.Hex;

import code.ponfee.commons.jce.HashAlgorithms;
import code.ponfee.commons.util.Bytes;
import code.ponfee.commons.util.MavenProjects;
import code.ponfee.commons.util.SecureRandoms;

/**
 * The SHA-1 digest implementation（max 2^64 bit length）
 * 
 * 异或⊕，同或⊙
 * 同或 = 异或  ^ 1
 * a与b的异或：a ^ b
 * a与b的同或：(a ^ b) ^ 1
 * https://www.cnblogs.com/scu-cjx/p/6878853.html
 * http://www.cnblogs.com/dacainiao/p/5554756.html
 * 
 * 安全性：SHA1所产生的摘要比MD5长32位。若两种散列函数在结构上没有任何问题的话，SHA1比MD5更安全。
 *  速度：两种方法都是主要考虑以32位处理器为基础的系统结构。但SHA1的运算步骤比MD5多了16步，
 *      而且SHA1记录单元的长度比MD5多了32位。因此若是以硬件来实现SHA1，其速度大约比MD5慢了25％。
 * 简易性：两种方法都是相当的简单，在实现上不需要很复杂的程序或是大量存储空间。然而总体上来讲，SHA1对每一步骤的操作描述比MD5简单<p>
 *      与MD5不同的是SHA1的原始报文长度不能超过2的64次方，另外SHA1的明文长度从低位开始填充<p>
 * 
 * 1、按每512bit（64byte）长度进行分组block，可以划分成L份明文分组，我们用Y0,Y1, ...YL-1表示，对于每一个明文分组，都要重复反复的处理
 * 
 * 2、最后一组先补一个字节1000 0000(-128)，直到长度满足对512取模后余数是448（若已经是56byte即448bit，补后有57byte，
 *   因此还需要补64-57+56=63byte，会多出一组）
 * 
 * 3、最后补8byte即64bit的原始数据长度long值(位长)，此时为448+64=512bit
 * 
 * 4、将512位的明文分组划分为16个子明文分组（sub-block），每个子明文分组为32位，使用W[t]（t=0,1,...,15）来表示这16份子明文分组
 *   W[t]存的是int数据，即4个byte为一组的32位的word字
 * 
 * 5、16份子明文分组扩展为80份，记为W[t]（t=0,1,...,79），扩充的方法：
 *   > W[t] = W[t]，当0≤t≤15
 *   > W[t] = (W[t-3] ⊕ W[t-8] ⊕ W[t-14] ⊕ W [t-16]) << 1，当16≤t≤79
 * 
 * 6、分组处理：接下来，对输入分组进行80个步骤的处理，目的是根据输入分组的信息来改变内部状态，
 *   在对分组处理时，SHA-1中常数Kt如下：
 *   K0 = 0x5A827999    0≤t≤19
 *   K1 = 0x6ED9EBA1   20≤t≤39
 *   K2 = 0x8F1BBCDC   40≤t≤59
 *   K3 = 0xCA62C1D6   60≤t≤79
 * 
 *   5个链变量a,b,c,d,e如下：
 *   a = 0x67452301
 *   b = 0xEFCDAB89
 *   c = 0x98BADCFE
 *   d = 0x10325476
 *   e = 0xC3D2E1F0
 * 
 *   SHA1有4轮运算，每一轮包括20个步骤一共80步，当第1轮运算中的第1步骤开始处理时a、b、c、d、e五个链接变量中的值先赋值到另外
 *   5个记录单元a′、b′、c′、d′、e′中，这5个值将保留，用于在第4轮的最后一个步骤完成之后与链接变量a、b、c、d、e进行求和操作
 * 
 * 7、SHA-1使用了F0,F1,....,F79这样的一个逻辑函数序列，每一个Ft对3个32位双字b,c,d进行操作，产生一个32位双字的输出。
 *   Ft(b,c,d) = (b&c)|((~b)&d)      0≤t≤19
 *   Ft(b,c,d) = b^c^d              20≤t≤39
 *   Ft(b,c,d) = (b&c)|(b&d)|(c&d)  40≤t≤59
 *   Ft(b,c,d) = b^c^d              60≤t≤79
 * 
 * 8、W[0] ~ W[19]处理：（注：S为左移位操作）
 *   for (int t=0; t<20; t++) {
 *     tmp=K0+F0(b,c,d)+S(5,a)+e+(sh->W[t]); // 将Kt+Ft(b,c,d)+(a<<5)+e+W[t]的结果赋值给临时变量tmp
 *     e=d;                                  // 将链接变量d初始值赋值给链接变量e
 *     d=c;                                  // 将链接变量c初始值赋值给链接变量d
 *     c=S(30,b);                            // 将链接变量b初始值循环左移30位赋值给链接变量c
 *     b=a; a=tmp;                           // 将链接变量a初始值赋值给链接变量b，再将tmp赋值给a
 *   }
 * 
 *   W[20] ~ W[39]处理：
 *   for (int t=20; t<40; t++) {
 *     tmp=K1+F1(b,c,d)+S(5,a)+e+(sh->W[t]);
 *     e=d; d=c;
 *     c=S(30,b);
 *     b=a; a=tmp;
 *   }
 * 
 *   W[40] ~ W[59]处理：
 *   for (int t=40; t<60; t++) {
 *     tmp=K2+F2(b,c,d)+S(5,a)+e+(sh->W[t]);
 *     e=d; d=c;
 *     c=S(30,b);
 *     b=a; a=tmp;
 *   }
 * 
 *   W[60] ~ W[79]处理：
 *   for (int t=60; t<80; t++) {
 *     tmp=K3+F3(b,c,d)+S(5,a)+e+(sh->W[t]);
 *     e=d; d=c;
 *     c=S(30,b);
 *     b=a; a=tmp;
 *   }
 *   即：a,b,c,d,e ← [(a<<5)+ Ft(b,c,d)+e+Wt+Kt], a, (b<<30), c, d
 * 
 * 9、将循环80个步骤后的值a,b,c,d,e与原始链变量a′、b′、c′、d′、e′相加作为下一个明文分组的输入重复进行以上操作
 *   sh->a′+=a; sh->b′+=b; sh->c′+=c; 
 *   sh->d′+=d; sh->e′+=e; 
 * 
 * 10、最后一个分组处理完成后，最终得到的a,b,c,d,e即为160位的消息摘要
 * 
 * @author Ponfee
 */
public class SHA1Digest {

    /** SHA-1分组块大小 */
    private static final int BLOCK_SIZE = 64;

    /** SHA-1分组块大小 */
    private static final int DIGEST_SIZE = 20;

    /** 填充的界限 */
    private static final int PADDING_BOUNDS = 448 / 8;

    /** 链变量 */
    private static final int A = 0x67452301;
    private static final int B = 0xEFCDAB89;
    private static final int C = 0x98BADCFE;
    private static final int D = 0x10325476;
    private static final int E = 0xC3D2E1F0;

    /** 常数K */
    private static final int K0 = 0x5A827999;
    private static final int K1 = 0x6ED9EBA1;
    private static final int K2 = 0x8F1BBCDC;
    private static final int K3 = 0xCA62C1D6;

    private final int[] W = new int[80];
    private final byte[] block = new byte[BLOCK_SIZE];

    private int a, b, c, d, e, blockOffset;
    private long byteCount;

    private SHA1Digest() {
        this.reset();
    }

    private SHA1Digest(SHA1Digest d) {
        this.a = d.a;
        this.b = d.b;
        this.c = d.c;
        this.d = d.d;
        this.e = d.e;

        this.blockOffset = d.blockOffset;
        this.byteCount = d.byteCount;

        System.arraycopy(d.W, 0, this.W, 0, d.W.length);
    }

    public static SHA1Digest getInstance() {
        return new SHA1Digest();
    }

    public static SHA1Digest getInstance(SHA1Digest d) {
        return new SHA1Digest(d);
    }

    public void update(byte input) {
        this.update(new byte[] { input });
    }

    public void update(byte[] input) {
        this.update(input, 0, input.length);
    }

    public void update(byte[] input, int offset, int length) {
        for (int i = offset, end = offset + length; i < end; i++) {
            this.block[this.blockOffset++] = input[i];
            if (this.blockOffset == BLOCK_SIZE) {
                this.digestBlock(this.block);
                this.blockOffset = 0;
                this.byteCount += BLOCK_SIZE;
            }
        }
    }

    public byte[] doFinal(byte[] data) {
        this.update(data);
        return this.doFinal();
    }

    public byte[] doFinal() {
        this.byteCount += this.blockOffset;
        this.block[this.blockOffset++] = -128; // 填充：先补1000 0000
        if (this.blockOffset > PADDING_BOUNDS) {
            padding0(this.block, this.blockOffset, BLOCK_SIZE); // 填充0
            this.digestBlock(this.block);

            // 56 byte 0 and 8 bit length
            this.blockOffset = 0;
        }

        padding0(this.block, this.blockOffset, PADDING_BOUNDS);

        byte[] dataBitLength = Bytes.fromLong(this.byteCount << 3);
        System.arraycopy(dataBitLength, 0, this.block, PADDING_BOUNDS, 8);

        this.digestBlock(this.block);

        byte[] digest = new byte[DIGEST_SIZE];
        toByteArray(this.a, digest,  0);
        toByteArray(this.b, digest,  4);
        toByteArray(this.c, digest,  8);
        toByteArray(this.d, digest, 12);
        toByteArray(this.e, digest, 16);

        this.reset();

        return digest;
    }

    public void reset() {
        this.a = A;
        this.b = B;
        this.c = C;
        this.d = D;
        this.e = E;

        this.blockOffset = 0;
        this.byteCount = 0;

        for (int j = 0; j < 16; j++) {
            this.W[j] = 0;
        }
    }

    public int getDigestSize() {
        return DIGEST_SIZE;
    }

    // --------------------------------------------------private methods
    private void digestBlock(byte[] data) {
        int i = 0;

        // sub-block（子明文分组）
        for (int j = 0; i < 16; j += 4) {
            this.W[i++] = Bytes.toInt(data, j);
        }

        // ext-block（扩展明文分组）
        for (; i < 80; i++) {
            this.W[i] = shiftLeft(this.W[i -  3] ^ this.W[i -  8] 
                                ^ this.W[i - 14] ^ this.W[i - 16], 1);
        }

        int a1 = this.a, b1 = this.b,
            c1 = this.c, d1 = this.d,
            e1 = this.e;

        for (int t = 0; t < 20; t++) {
            // 将Kt+Ft(b,c,d)+(a<<5)+e+W[t]的结果赋值给临时变量tmp
            int tmp = K0 + f0(b1, c1, d1) + shiftLeft(a1, 5) + e1 + this.W[t];
            e1 = d1; // 将链接变量d初始值赋值给链接变量e
            d1 = c1; // 将链接变量c初始值赋值给链接变量d
            c1 = shiftLeft(b1, 30); // 将链接变量b初始值循环左移30位赋值给链接变量c
            b1 = a1; // 将链接变量a初始值赋值给链接变量b
            a1 = tmp; // tmp赋值给a
        }

        for (int t = 20; t < 40; t++) {
            int tmp = K1 + f1(b1, c1, d1) + shiftLeft(a1, 5) + e1 + this.W[t];
            e1 = d1;
            d1 = c1;
            c1 = shiftLeft(b1, 30);
            b1 = a1;
            a1 = tmp;
        }

        for (int t = 40; t < 60; t++) {
            int tmp = K2 + f2(b1, c1, d1) + shiftLeft(a1, 5) + e1 + this.W[t];
            e1 = d1;
            d1 = c1;
            c1 = shiftLeft(b1, 30);
            b1 = a1;
            a1 = tmp;
        }

        for (int t = 60; t < 80; t++) {
            int tmp = K3 + f3(b1, c1, d1) + shiftLeft(a1, 5) + e1 + this.W[t];
            e1 = d1;
            d1 = c1;
            c1 = shiftLeft(b1, 30);
            b1 = a1;
            a1 = tmp;
        }

        // add
        this.a += a1;
        this.b += b1;
        this.c += c1;
        this.d += d1;
        this.e += e1;

        // reset W
        for (int j = 0; j < 16; j++) {
            this.W[j] = 0;
        }
    }

    private static int f0(int b, int c, int d) {
        return (b & c) | ((~b) & d);
    }

    private static int f1(int b, int c, int d) {
        return b ^ c ^ d;
    }

    private static int f2(int b, int c, int d) {
        return (b & c) | (b & d) | (c & d);
    }

    private static int f3(int b, int c, int d) {
        return b ^ c ^ d;
    }

    private static int shiftLeft(int n, int count) {
        return n << count | n >>> (32 - count);
    }

    private static void padding0(byte[] bytes, int from, int to) {
        for (int i = from; i < to; i++) {
            bytes[i] = 0;
        }
    }

    public static void toByteArray(int n, byte[] bytes, int offset) {
        bytes[  offset] = (byte) (n >>> 24);
        bytes[++offset] = (byte) (n >>> 16);
        bytes[++offset] = (byte) (n >>>  8);
        bytes[++offset] = (byte) (n       );
    }

    public static void main(String[] args) {
        byte[] data = MavenProjects.getMainJavaFileAsByteArray(SHA1Digest.class);

        SHA1Digest sha1 = SHA1Digest.getInstance();
        System.out.println(Hex.encodeHexString(sha1.doFinal(data)));
        System.out.println(HashUtils.sha1Hex(data));

        for (int i = 0; i < 1000; i++) {
            byte[] data1 = SecureRandoms.nextBytes(ThreadLocalRandom.current().nextInt(65537) + 1);
            byte[] data2 = SecureRandoms.nextBytes(ThreadLocalRandom.current().nextInt(65537) + 1);
            byte[] data3 = SecureRandoms.nextBytes(ThreadLocalRandom.current().nextInt(65537) + 1);
            byte[] data4 = SecureRandoms.nextBytes(ThreadLocalRandom.current().nextInt(65537) + 1);
            byte[] data5 = SecureRandoms.nextBytes(ThreadLocalRandom.current().nextInt(65537) + 1);
            byte[] data6 = SecureRandoms.nextBytes(ThreadLocalRandom.current().nextInt(65537) + 1);
            sha1.reset();
            sha1.update(data1);
            sha1.update(data2);
            sha1.update(data3);
            sha1.update(data4);
            sha1.update(data5);
            sha1.update(data6);
            if (!Arrays.equals(HashUtils.digest(HashAlgorithms.SHA1, data1, data2, data3, data4, data5, data6), 
                               sha1.doFinal())) {
                System.err.println("FAIL!" + "   " + data.length);
            }
        }
    }
}
