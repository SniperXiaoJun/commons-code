package code.ponfee.commons.jce.sm;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Map;

import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.generators.ECKeyPairGenerator;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECKeyGenerationParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.math.ec.ECFieldElement;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.math.ec.ECFieldElement.Fp;
import org.bouncycastle.util.Arrays;

import com.google.common.collect.ImmutableMap;

import code.ponfee.commons.util.Bytes;

/**
 * SM2非对称加密算法实现
 * @author Ponfee
 */
@SuppressWarnings("deprecation")
public final class SM2 {

    public static final String PRIVATE_KEY = "private";
    public static final String PUBLIC_KEY = "public";

    private static final String[] ECC_PARAM = {
        "FFFFFFFEFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF00000000FFFFFFFFFFFFFFFF",
        "FFFFFFFEFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF00000000FFFFFFFFFFFFFFFC",
        "28E9FA9E9D9F5E344D5A9E4BCF6509A7F39789F515AB8F92DDBCBD414D940E93",
        "FFFFFFFEFFFFFFFFFFFFFFFFFFFFFFFF7203DF6B21C6052B53BBF40939D54123",
        "32C4AE2C1F1981195F9904466A39C9948FE30BBFF2660BE1715A4589334C74C7",
        "BC3736A2F4F6779C59BDCEE36B692153D0A9877CC62A474002DF32E52139F0A0"
    };

    private final BigInteger ecc_p;
    private final BigInteger ecc_a;
    private final BigInteger ecc_b;
    private final BigInteger ecc_n;
    private final BigInteger ecc_gx;
    private final BigInteger ecc_gy;
    private final ECCurve ecc_curve;
    private final ECPoint ecc_point_g;
    private final ECDomainParameters ecc_bc_spec;
    private final ECKeyPairGenerator ecc_key_pair_generator;
    private final ECFieldElement ecc_gx_fieldelement;
    private final ECFieldElement ecc_gy_fieldelement;

    private SM2() {
        this.ecc_p  = new BigInteger(ECC_PARAM[0], 16);
        this.ecc_a  = new BigInteger(ECC_PARAM[1], 16);
        this.ecc_b  = new BigInteger(ECC_PARAM[2], 16);
        this.ecc_n  = new BigInteger(ECC_PARAM[3], 16);
        this.ecc_gx = new BigInteger(ECC_PARAM[4], 16);
        this.ecc_gy = new BigInteger(ECC_PARAM[5], 16);

        this.ecc_gx_fieldelement = new Fp(this.ecc_p, this.ecc_gx);
        this.ecc_gy_fieldelement = new Fp(this.ecc_p, this.ecc_gy);

        this.ecc_curve = new ECCurve.Fp(this.ecc_p, this.ecc_a, this.ecc_b);
        this.ecc_point_g = new ECPoint.Fp(this.ecc_curve, this.ecc_gx_fieldelement, this.ecc_gy_fieldelement);

        this.ecc_bc_spec = new ECDomainParameters(this.ecc_curve, this.ecc_point_g, this.ecc_n);

        ECKeyGenerationParameters ecc_ecgenparam;
        ecc_ecgenparam = new ECKeyGenerationParameters(this.ecc_bc_spec, new SecureRandom());

        this.ecc_key_pair_generator = new ECKeyPairGenerator();
        this.ecc_key_pair_generator.init(ecc_ecgenparam);
    }

    private static class Cipher {
        int ct;
        ECPoint p2;
        SM3Digest sm3keybase;
        SM3Digest sm3c3;
        byte[] key;
        byte keyOff;

        Cipher() {
            this.ct = 1;
            this.key = new byte[32];
            this.keyOff = 0;
        }

        void reset() {
            this.sm3keybase = new SM3Digest();
            this.sm3c3 = new SM3Digest();

            byte p[] = to32ByteArray(p2.getX().toBigInteger());
            this.sm3keybase.update(p, 0, p.length);
            this.sm3c3.update(p, 0, p.length);

            p = to32ByteArray(p2.getY().toBigInteger());
            this.sm3keybase.update(p, 0, p.length);
            this.ct = 1;
            nextKey();
        }

        void nextKey() {
            SM3Digest sm3keycur = new SM3Digest(this.sm3keybase);
            sm3keycur.update((byte) (ct >> 24 & 0xff));
            sm3keycur.update((byte) (ct >> 16 & 0xff));
            sm3keycur.update((byte) (ct >> 8 & 0xff));
            sm3keycur.update((byte) (ct & 0xff));
            sm3keycur.doFinal(key, 0);
            this.keyOff = 0;
            this.ct++;
        }

        ECPoint initEncrypt(SM2 sm2, ECPoint userKey) {
            AsymmetricCipherKeyPair key = sm2.ecc_key_pair_generator.generateKeyPair();
            ECPrivateKeyParameters ecpriv = (ECPrivateKeyParameters) key.getPrivate();
            ECPublicKeyParameters ecpub = (ECPublicKeyParameters) key.getPublic();
            BigInteger k = ecpriv.getD();
            ECPoint c1 = ecpub.getQ();
            this.p2 = userKey.multiply(k);
            this.reset();
            return c1;
        }

        void encrypt(byte data[]) {
            this.sm3c3.update(data, 0, data.length);
            for (int i = 0; i < data.length; i++) {
                if (keyOff == key.length) {
                    nextKey();
                }
                data[i] ^= key[keyOff++];
            }
        }

        void initDecrypt(BigInteger userD, ECPoint c1) {
            this.p2 = c1.multiply(userD);
            reset();
        }

        void decrypt(byte data[]) {
            for (int i = 0; i < data.length; i++) {
                if (keyOff == key.length) {
                    nextKey();
                }
                data[i] ^= key[keyOff++];
            }

            this.sm3c3.update(data, 0, data.length);
        }

        void doFinal(byte c3[]) {
            byte p[] = to32ByteArray(p2.getY().toBigInteger());
            this.sm3c3.update(p, 0, p.length);
            this.sm3c3.doFinal(c3, 0);
            reset();
        }
    }

    private static byte[] to32ByteArray(BigInteger n) {
        if (n == null) {
            return null;
        }

        byte[] bytes = n.toByteArray();
        if (bytes.length == 32) {
            return bytes;
        } else if (bytes.length > 32) {
            return Arrays.copyOfRange(bytes, 1, 33);
        } else {
            byte[] result = new byte[32];
            for (int i = 0; i < 32 - bytes.length; i++) {
                result[i] = 0;
            }
            System.arraycopy(bytes, 0, result, 32 - bytes.length, bytes.length);
            return result;
        }
    }

    //生成随机秘钥对
    public static Map<String, byte[]> generateKeyPair() {
        AsymmetricCipherKeyPair key = new SM2().ecc_key_pair_generator.generateKeyPair();
        ECPrivateKeyParameters ecpriv = (ECPrivateKeyParameters) key.getPrivate();
        ECPublicKeyParameters ecpub = (ECPublicKeyParameters) key.getPublic();

        BigInteger privateKey = ecpriv.getD();
        ECPoint publicKey = ecpub.getQ();

        return ImmutableMap.of(PRIVATE_KEY, privateKey.toByteArray(), 
                               PUBLIC_KEY, publicKey.getEncoded(true));
    }

    //数据加密
    public static byte[] encrypt(byte[] publicKey, byte[] data) {
        if (publicKey == null || publicKey.length == 0
            || data == null || data.length == 0) {
            return null;
        }

        Cipher cipher = new Cipher();
        SM2 sm2 = new SM2();
        ECPoint userKey = sm2.ecc_curve.decodePoint(publicKey);
        ECPoint c1 = cipher.initEncrypt(sm2, userKey);

        byte[] c2 = Arrays.copyOf(data, data.length);
        cipher.encrypt(c2);

        byte[] c3 = new byte[32];
        cipher.doFinal(c3);

        //C1 C2 C3拼装成加密字串
        return Bytes.concat(c1.getEncoded(false), c2, c3);
    }

    //数据解密
    public static byte[] decrypt(byte[] privateKey, byte[] encrypted) {
        if (privateKey == null || privateKey.length == 0
            || encrypted == null || encrypted.length == 0) {
            return null;
        }

        // 分解加密数据
        // C1 = C1标志位1位 + C1实体部分64位，共65位
        // C3 = C3实体部分32位
        // C2 = encrypted.length-C1-C2 = encrypted.length-97
        int c2Len = encrypted.length - 97;
        byte[] c1 = Arrays.copyOf(encrypted, 65);
        byte[] c2 = Arrays.copyOfRange(encrypted, 65, 65 + c2Len);
        byte[] c3 = Arrays.copyOfRange(encrypted, 65 + c2Len, /*97 + c2Len*/encrypted.length);

        BigInteger userD = new BigInteger(1, privateKey);

        //通过C1实体字节来生成ECPoint
        ECPoint ec = new SM2().ecc_curve.decodePoint(c1);
        Cipher cipher = new Cipher();
        cipher.initDecrypt(userD, ec);
        cipher.decrypt(c2);
        cipher.doFinal(c3);

        //返回解密结果
        return c2;
    }

    public static void main(String[] args) {
        Map<String, byte[]> map = generateKeyPair();
        byte[] encrypted = encrypt(map.get(PUBLIC_KEY), "data".getBytes());
        byte[] decrypted = decrypt(map.get(PRIVATE_KEY), encrypted);
        System.out.println(new String(decrypted));
    }
}
