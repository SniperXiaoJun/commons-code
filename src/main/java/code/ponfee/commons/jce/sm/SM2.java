package code.ponfee.commons.jce.sm;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.generators.ECKeyPairGenerator;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECKeyGenerationParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.util.Arrays;

import com.google.common.collect.ImmutableMap;

import code.ponfee.commons.util.Bytes;
import code.ponfee.commons.util.MavenProjects;

/**
 * SM2非对称加密算法实现
 * @author Ponfee
 */
public final class SM2 {

    public static final String PRIVATE_KEY = "SM2PrivateKey";
    public static final String PUBLIC_KEY = "SM2PublicKey";

    private static final String[] ECC_PARAM = { // 正式参数
        "FFFFFFFEFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF00000000FFFFFFFFFFFFFFFF",
        "FFFFFFFEFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF00000000FFFFFFFFFFFFFFFC",
        "28E9FA9E9D9F5E344D5A9E4BCF6509A7F39789F515AB8F92DDBCBD414D940E93",
        "FFFFFFFEFFFFFFFFFFFFFFFFFFFFFFFF7203DF6B21C6052B53BBF40939D54123",
        "32C4AE2C1F1981195F9904466A39C9948FE30BBFF2660BE1715A4589334C74C7",
        "BC3736A2F4F6779C59BDCEE36B692153D0A9877CC62A474002DF32E52139F0A0"
    };

    /*public static final String[] ECC_PARAM = { // 测试参数  
        "8542D69E4C044F18E8B92435BF6FF7DE457283915C45517D722EDB8B08F1DFC3",
        "787968B4FA32C3FD2417842E73BBFEFF2F3C848B6831D7E0EC65228B3937E498",
        "63E4C6D3B23B0C849CF84241484BFE48F61D59A5B16BA06E6E12D1DA27C5249A",
        "8542D69E4C044F18E8B92435BF6FF7DD297720630485628D5AE74EE7C32E79B7",
        "421DEBD61B62EAB6746434EBC3CC315E32220B3BADD50BDC4C4E6C147FEDD43D",
        "0680512BCBB42C07D47349D2153B70C4E5D7FDFCBFA36EA1A85841B9E46E09A2"
    };*/

    private final ECCurve curve;
    private final ECKeyPairGenerator keyPairGenerator;

    private ECPoint pointM;
    private ECPoint p2;
    private int ct;
    private SM3Digest sm3keybase;
    private SM3Digest sm3c3;
    private byte[] key;
    private byte keyOffset;

    private SM2() {
        this.curve = new ECCurve.Fp(new BigInteger(ECC_PARAM[0], 16), // q
                                    new BigInteger(ECC_PARAM[1], 16), // a
                                    new BigInteger(ECC_PARAM[2], 16)); // b

        ECPoint pointG = this.curve.createPoint(new BigInteger(ECC_PARAM[4], 16), // x
                                                   new BigInteger(ECC_PARAM[5], 16)); // y

        ECDomainParameters bcSpec = new ECDomainParameters(this.curve, // curve
                                                           pointG, // G
                                                           new BigInteger(ECC_PARAM[3], 16)); // n

        this.keyPairGenerator = new ECKeyPairGenerator();
        this.keyPairGenerator.init(new ECKeyGenerationParameters(bcSpec, new SecureRandom()));
    }

    private SM2(byte[] pubKey) {
        this(null, pubKey);
    }

    private SM2(BigInteger privateKey, byte[] publicKey) {
        this();

        ECPoint pointG = this.curve.decodePoint(publicKey); // base point
        if (privateKey == null) { // 构造加密参数
            AsymmetricCipherKeyPair key = this.keyPairGenerator.generateKeyPair();

            ECPublicKeyParameters ecpub = (ECPublicKeyParameters) key.getPublic();
            this.pointM = ecpub.getQ();

            ECPrivateKeyParameters ecpriv = (ECPrivateKeyParameters) key.getPrivate();
            this.p2 = pointG.multiply(ecpriv.getD()); // pubKey * ecpriv.getD()

        } else { // 构造解密参数
            this.p2 = pointG.multiply(privateKey); // pubKey * privateKey

        }

        this.key = new byte[32];
        this.reset();
    }

    private void reset() {
        this.sm3keybase = SM3Digest.getInstance();
        this.sm3c3 = SM3Digest.getInstance();

        byte[] p = to32ByteArray(this.p2.normalize().getXCoord().toBigInteger());
        this.sm3keybase.update(p);
        this.sm3c3.update(p);

        p = to32ByteArray(this.p2.normalize().getYCoord().toBigInteger());
        this.sm3keybase.update(p);
        this.ct = 1;
        nextKey();
    }

    private void nextKey() {
        SM3Digest sm3keycur = SM3Digest.getInstance(this.sm3keybase);
        sm3keycur.update((byte) (ct >> 24 & 0xff));
        sm3keycur.update((byte) (ct >> 16 & 0xff));
        sm3keycur.update((byte) (ct >> 8 & 0xff));
        sm3keycur.update((byte) (ct & 0xff));
        sm3keycur.doFinal(key, 0);
        this.keyOffset = 0;
        this.ct++;
    }

    private void encrypt(byte data[]) {
        this.sm3c3.update(data);
        for (int i = 0; i < data.length; i++) {
            if (keyOffset == key.length) {
                nextKey();
            }
            data[i] ^= key[keyOffset++];
        }
    }

    private void decrypt(byte data[]) {
        for (int i = 0; i < data.length; i++) {
            if (keyOffset == key.length) {
                nextKey();
            }
            data[i] ^= key[keyOffset++];
        }

        this.sm3c3.update(data);
    }

    private byte[] doFinal() {
        byte[] p = to32ByteArray(this.p2.normalize().getYCoord().toBigInteger());
        p = this.sm3c3.doFinal();
        reset();
        return p;
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

    /**
     * generate the SM2 key pair
     * a public key and a private key
     * @return sm2 key store the map
     */
    public static Map<String, byte[]> generateKeyPair() {
        AsymmetricCipherKeyPair key = new SM2().keyPairGenerator.generateKeyPair();
        ECPrivateKeyParameters ecpriv = (ECPrivateKeyParameters) key.getPrivate();
        ECPublicKeyParameters ecpub = (ECPublicKeyParameters) key.getPublic();

        BigInteger privateKey = ecpriv.getD();
        ECPoint publicKey = ecpub.getQ(); // the point G of base point

        return ImmutableMap.of(PRIVATE_KEY, privateKey.toByteArray(), 
                               PUBLIC_KEY, publicKey.getEncoded(false));
    }

    /**
     * encrypt data by public key
     * @param publicKey SM2 public key
     * @param data the data to be encrypt
     * @return encrypted byte array
     */
    public static byte[] encrypt(byte[] publicKey, byte[] data) {
        if (ArrayUtils.isEmpty(publicKey) || ArrayUtils.isEmpty(data)) {
            return null;
        }

        SM2 sm2 = new SM2(publicKey);

        byte[] c2 = Arrays.copyOf(data, data.length);
        sm2.encrypt(c2); // 加密数据

        byte[] c3 = sm2.doFinal(); // 摘要

        // return the C1(65) + C2(data.length) + C3(32)
        return Bytes.concat(sm2.pointM.getEncoded(false), c2, c3);
    }

    /**
     * decrypt the encrypted byte array data by private key
     * @param privateKey SM2 private key
     * @param encrypted the encrypted byte array data
     * @return the origin byte array data
     */
    public static byte[] decrypt(byte[] privateKey, byte[] encrypted) {
        if (ArrayUtils.isEmpty(privateKey) || ArrayUtils.isEmpty(privateKey)) {
            return null;
        }

        // 分解加密数据
        // C1公钥 = 1位标志位+64位公钥（共65位）
        // C2数据 = encrypted.length-C1-C3
        // C3摘要 = 32
        int c1Len = 65, c3Len = 32, c2Len = encrypted.length - (c1Len + c3Len);
        byte[] c1 = Arrays.copyOf(encrypted, c1Len);
        byte[] c2 = Arrays.copyOfRange(encrypted, c1Len, c1Len + c2Len);
        byte[] c3 = Arrays.copyOfRange(encrypted, c1Len + c2Len, encrypted.length);

        SM2 sm2 = new SM2(new BigInteger(1, privateKey), c1);

        sm2.decrypt(c2);
        byte[] c3Actual = sm2.doFinal();

        if (!Arrays.areEqual(c3, c3Actual)) {
            throw new SecurityException("Invalid SM3 digest.");
        }

        //返回解密结果
        return c2;
    }

    public static void main(String[] args) {
        Map<String, byte[]> map = generateKeyPair();
        byte[] encrypted = encrypt(map.get(PUBLIC_KEY), MavenProjects.getMainJavaFileAsLineString(SM2.class).getBytes());
        byte[] decrypted = decrypt(map.get(PRIVATE_KEY), encrypted);
        System.out.println(new String(decrypted));
    }
}
