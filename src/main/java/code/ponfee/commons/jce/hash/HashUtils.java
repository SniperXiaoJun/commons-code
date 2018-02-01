package code.ponfee.commons.jce.hash;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Security;

import org.apache.commons.codec.binary.Hex;

import code.ponfee.commons.io.Files;
import code.ponfee.commons.jce.Providers;

/**
 * hash算法封装
 * 
 * 异或⊕，同或⊙
 * 同或 = 异或  ^ 1
 * a与b的异或：a ^ b
 * a与b的同或：(a ^ b) ^ 1
 * https://www.cnblogs.com/scu-cjx/p/6878853.html
 * 
 * 安全性：SHA1所产生的摘要比MD5长32位。若两种散列函数在结构上没有任何问题的话，SHA1比MD5更安全。
 *  速度：两种方法都是主要考虑以32位处理器为基础的系统结构。但SHA1的运算步骤比MD5多了16步，
 *      而且SHA1记录单元的长度比MD5多了32位。因此若是以硬件来实现SHA1，其速度大约比MD5慢了25％。
 * 简易性：两种方法都是相当的简单，在实现上不需要很复杂的程序或是大量存储空间。然而总体上来讲，SHA1对每一步骤的操作描述比MD5简单<p>
 *      与MD5不同的是SHA1的原始报文长度不能超过2的64次方，另外SHA1的明文长度从低位开始填充<p>
 * 
 * 1、按每512bit（64byte）长度进行分组block，可以划分成L份明文分组，我们用Y0,Y1, ...YL-1表示这些明文分组，对于每一个明文分组，都要重复反复的处理
 * 2、最后一组先补一个1，然后再补0，直到长度满足对512取模后余数是448（若已经是56byte即448bit，在补1后有449位，还要补511个0，会多出一组）
 * 3、最后补8byte即64bit的原始数据长度long值，此时为448+64=512bit
 * 5、将512位的明文分组划分为16个子明文分组（sub-block），每个子明文分组为32位，使用M[k]（k=0,1,...,15）来表示这16份子明文分组
 * 6、16份子明文分组扩展为80份，记为W[k]（k=0,1,...,79），扩充的方法：
 *   * W[t] = M[t]，当0≤t≤15
 *   * W[t] = (W[t-3] ⊕ W[t-8] ⊕ W[t-14] ⊕ W [t-16]) <<< 1，当16≤t≤79
 * 7、SHA1有4轮运算，每一轮包括20个步骤（一共80步），最后产生160位摘要，这160位摘要存放在5个32位的链接变量中，分别标记为A、B、C、D、E。这5个链接变量的初始值以16进制位表示如下：
 *   * A=0x67452301
 *   * B=0xEFCDAB89
 *   * C=0x98BADCFE
 *   * D=0x10325476
 *   * E=0xC3D2E1F0
 * 8、A、B、C、D、E五个链接变量中的值先赋值到另外5个记录单元A′，B′，C′，D′，E′中。这5个值将保留，
 *   用于在第4轮的最后一个步骤完成之后与链接变量A，B，C，D，E进行求和操作。SHA1的4轮运算，共80个步骤使用同一个操作程序，如下：
 *   A,B,C,D,E←[(A<<<5)+ ft(B,C,D)+E+Wt+Kt],A,(B<<<30),C,D，其中 ft(B,C,D)为逻辑函数，Wt为子明文分组W[t]，Kt为固定常数。这个操作程序的意义为：
 *   * 将[(A<<<5)+ ft(B,C,D)+E+Wt+Kt]的结果赋值给链接变量A；
 *   * 将链接变量A初始值赋值给链接变量B；
 *   * 将链接变量B初始值循环左移30位赋值给链接变量C；
 *   * 将链接变量C初始值赋值给链接变量D；
 *   * 将链接变量D初始值赋值给链接变量E。
 * 9、链接变量作为下一个明文分组的输入重复进行以上操作
 * 10、最后，5个链接变量里面的数据就是SHA1摘要
 * <p>
 * 
 * @author fupf
 */
public final class HashUtils {

    private static final int BUFF_SIZE = 4096;

    public static byte[] md5(InputStream input) {
        return digest(input, "MD5");
    }

    public static byte[] md5(byte[] data) {
        return digest("MD5", data);
    }

    public static String md5Hex(InputStream input) {
        return Hex.encodeHexString(md5(input));
    }

    public static String md5Hex(byte[] data) {
        return Hex.encodeHexString(md5(data));
    }

    public static String md5Hex(String data) {
        return md5Hex(data.getBytes());
    }

    public static String md5Hex(String data, String charset) {
        return md5Hex(data.getBytes(Charset.forName(charset)));
    }

    public static byte[] sha1(InputStream input) {
        return digest(input, "SHA-1");
    }

    public static byte[] sha1(byte[] data) {
        return digest("SHA-1", data);
    }

    public static String sha1Hex(InputStream input) {
        return Hex.encodeHexString(sha1(input));
    }

    public static String sha1Hex(byte[] data) {
        return Hex.encodeHexString(sha1(data));
    }

    public static String sha1Hex(String data) {
        return sha1Hex(data.getBytes());
    }

    public static String sha1Hex(String data, String charset) {
        return sha1Hex(data.getBytes(Charset.forName(charset)));
    }

    public static byte[] sha224(byte[] data) {
        return digest("SHA-224", data);
    }

    public static String sha224Hex(byte[] data) {
        return Hex.encodeHexString(sha224(data));
    }

    public static byte[] sha256(byte[] data) {
        return digest("SHA-256", data);
    }

    public static String sha256Hex(byte[] data) {
        return Hex.encodeHexString(sha256(data));
    }

    public static byte[] sha384(byte[] data) {
        return digest("SHA-384", data);
    }

    public static String sha384Hex(byte[] data) {
        return Hex.encodeHexString(sha384(data));
    }

    public static byte[] sha512(byte[]... data) {
        return digest("SHA-512", data);
    }

    public static String sha512Hex(byte[] data) {
        return Hex.encodeHexString(sha512(data));
    }

    // ---------------------------------------RipeMD---------------------------------------
    public static byte[] ripeMD128(byte[] data) {
        Security.addProvider(Providers.BC);
        return digest("RipeMD128", data);
    }

    public static String ripeMD128Hex(byte[] data) {
        return Hex.encodeHexString(ripeMD128(data));
    }

    public static byte[] ripeMD160(byte[] data) {
        Security.addProvider(Providers.BC);
        return digest("RipeMD160", data);
    }

    public static String ripeMD160Hex(byte[] data) {
        return Hex.encodeHexString(ripeMD160(data));
    }

    public static byte[] ripeMD256(byte[] data) {
        Security.addProvider(Providers.BC);
        return digest("RipeMD256", data);
    }

    public static String ripeMD256Hex(byte[] data) {
        return Hex.encodeHexString(ripeMD256(data));
    }

    public static byte[] ripeMD320(byte[] data) {
        Security.addProvider(Providers.BC);
        return digest("RipeMD320", data);
    }

    public static String ripeMD320Hex(byte[] data) {
        return Hex.encodeHexString(ripeMD320(data));
    }

    // ---------------------------------------private methods---------------------------------------
    /**
     * 数据摘要
     * @param data      hash data of byte array
     * @param algorithm hash算法
     * @return
     */
    private static byte[] digest(String algorithm, byte[]... data) {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException(e); // cannot happened
        }
        for (byte[] input : data) {
            md.update(input);
        }
        return md.digest();
    }

    /**
     * 数据摘要
     * @param data      hash data of input stream
     * @param algorithm hash 算法
     * @return
     */
    private static byte[] digest(InputStream input, String algorithm) {
        byte[] buffer = new byte[BUFF_SIZE];

        /*try (InputStream in = input) {
            MessageDigest digest = MessageDigest.getInstance(algorithm);
            for (int len; (len = in.read(buffer)) != Files.EOF;) {
                digest.update(buffer, 0, len);
            }
            return digest.digest();
        } catch (NoSuchAlgorithmException | IOException e) {
            throw new IllegalArgumentException(e);
        }*/

        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException(e); // cannot happened
        }

        try (InputStream in = input; 
             DigestInputStream dIn = new DigestInputStream(input, digest)
         ) {
            while (dIn.read(buffer) != Files.EOF) {
                //  do-non
            }
            return digest.digest();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println(sha224Hex("1".getBytes()));
        System.out.println(ripeMD160Hex("1234567890".getBytes()));
        //System.out.println(ObjectUtils.toString(shortText("http://www.manong5.com/102542001/")));
        long start = System.currentTimeMillis();
        System.out.println(sha1Hex(new FileInputStream("E:\\tools\\develop\\linux\\CentOS-6.6-x86_64-bin-DVD1.iso")));
        System.out.println((System.currentTimeMillis() - start) / 1000);
    }
}
