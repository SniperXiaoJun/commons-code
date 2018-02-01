package code.ponfee.commons.jce.sm;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.math.ec.ECPoint;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

import code.ponfee.commons.jce.ECParameters;
import code.ponfee.commons.util.Bytes;
import code.ponfee.commons.util.MavenProjects;

/**
 * SM2非对称加密算法实现
 * support encrypt/decrypt
 * and sign/verify signature
 * @author Ponfee
 */
public final class SM2 {

    public static final String PRIVATE_KEY = "SM2PrivateKey";
    public static final String PUBLIC_KEY = "SM2PublicKey";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final ECPoint point;
    private final byte[] key;
    private SM3Digest sm3keybase;
    private SM3Digest sm3c3;
    private int ct;
    private byte keyOffset;

    private SM2(ECPoint publicKey, BigInteger privateKey) {
        Preconditions.checkArgument(publicKey != null, 
                                    "public key cannot be empty.");
        Preconditions.checkArgument(privateKey != null, 
                                    "private key cannot be empty.");

        this.point = publicKey.multiply(privateKey); // S = [h]point
        this.key = new byte[32];
        this.reset();
    }

    private void reset() {
        this.sm3keybase = SM3Digest.getInstance();
        this.sm3c3 = SM3Digest.getInstance();

        byte[] p = to32ByteArray(this.point.normalize().getXCoord().toBigInteger());
        this.sm3keybase.update(p);
        this.sm3c3.update(p);

        p = to32ByteArray(this.point.normalize().getYCoord().toBigInteger());
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
        byte[] p = to32ByteArray(this.point.normalize().getYCoord().toBigInteger());
        p = this.sm3c3.doFinal();
        reset();
        return p;
    }

    /**
     * generate the SM2 key pair
     * a public key and a private key
     * @param ecParam the ec parameter
     * @return sm2 key store the map
     */
    public static Map<String, byte[]> generateKeyPair(ECParameters ecParam) {
        AsymmetricCipherKeyPair key = ecParam.keyPairGenerator.generateKeyPair();
        ECPrivateKeyParameters ecpriv = (ECPrivateKeyParameters) key.getPrivate();
        ECPublicKeyParameters ecpub = (ECPublicKeyParameters) key.getPublic();

        BigInteger priKey = ecpriv.getD(); // k
        ECPoint pubKey = ecpub.getQ(); // K = [k]POINT_G

        return ImmutableMap.of(PRIVATE_KEY, priKey.toByteArray(), 
                               PUBLIC_KEY, pubKey.getEncoded(false));
    }

    public static byte[] getPublicKey(Map<String, byte[]> keyMap) {
        return keyMap.get(PUBLIC_KEY);
    }

    public static byte[] getPrivateKey(Map<String, byte[]> keyMap) {
        return keyMap.get(PRIVATE_KEY);
    }

    public static ECPoint getPublicKey(ECParameters ecParam, byte[] publicKey) {
        return ecParam.curve.decodePoint(publicKey);
    }

    public static BigInteger getPrivateKey(byte[] privateKey) {
        return new BigInteger(1, privateKey);
    }

    /**
     * encrypt data by public key
     * @param ecParam the ec parameter
     * @param publicKey SM2 public key, point K = [k]POINT_G
     * @param data the data to be encrypt
     * @return encrypted byte array
     */
    public static byte[] encrypt(ECParameters ecParam, byte[] publicKey, byte[] data) {
        if (ArrayUtils.isEmpty(publicKey) || ArrayUtils.isEmpty(data)) {
            return null;
        }

        // create C1 point
        AsymmetricCipherKeyPair key = ecParam.keyPairGenerator.generateKeyPair(); // point M
        ECPublicKeyParameters ecPub = (ECPublicKeyParameters) key.getPublic();
        ECPrivateKeyParameters ecPri = (ECPrivateKeyParameters) key.getPrivate();

        SM2 sm2 = new SM2(ecParam.curve.decodePoint(publicKey), ecPri.getD());

        byte[] c1 = ecPub.getQ().getEncoded(false); // generate random r, C1=M+rK
        byte[] c2 = Arrays.copyOf(data, data.length); // C2=rG
        sm2.encrypt(c2); // 加密数据

        byte[] c3 = sm2.doFinal(); // 摘要

        // return the C1(65) + C2(data.length) + C3(32)
        return Bytes.concat(c1, c2, c3);
    }

    /**
     * decrypt the encrypted byte array data by private key
     * @param ecParam the ec parameter
     * @param privateKey SM2 private key
     * @param encrypted the encrypted byte array data
     * @return the origin byte array data
     */
    public static byte[] decrypt(ECParameters ecParam, 
                                 byte[] privateKey, byte[] encrypted) {
        if (ArrayUtils.isEmpty(privateKey) || ArrayUtils.isEmpty(encrypted)) {
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

        SM2 sm2 = new SM2(getPublicKey(ecParam, c1), getPrivateKey(privateKey));

        sm2.decrypt(c2);
        byte[] c3Actual = sm2.doFinal();

        if (!Arrays.equals(c3, c3Actual)) {
            throw new SecurityException("Invalid SM3 digest.");
        }

        //返回解密结果
        return c2;
    }

    /**
     * sm2 sign
     * @param ecParam the ec parameter
     * @param data 签名信息
     * @param ida  签名方唯一标识
     * @param publicKey 公钥
     * @param privateKey 私钥
     * @return 签名信息
     */
    public static byte[] sign(ECParameters ecParam, byte[] data, byte[] ida, 
                              byte[] publicKey, byte[] privateKey) {
        ECPoint pubKey = getPublicKey(ecParam, publicKey);
        BigInteger priKey = getPrivateKey(privateKey);
        data = Bytes.concat(calcZ(ecParam, ida, pubKey), data);
        BigInteger e = new BigInteger(1, SM3Digest.getInstance().doFinal(data));
        BigInteger k;
        BigInteger r;
        do {
            k = random(ecParam.n);
            ECPoint p1 = ecParam.pointG.multiply(k).normalize();
            BigInteger x1 = p1.getXCoord().toBigInteger();
            r = e.add(x1);
            r = r.mod(ecParam.n);
        } while (r.equals(BigInteger.ZERO) || r.add(k).equals(ecParam.n));

        BigInteger n1 = priKey.add(BigInteger.ONE).modInverse(ecParam.n);
        BigInteger n2 = k.subtract(r.multiply(priKey));
        BigInteger n3 = n1.multiply(n2.mod(ecParam.n));
        BigInteger s = n3.mod(ecParam.n);

        return new Signature(r, s).toByteArray();
    }

    /**
     * verify signature
     * @param ecParam the ec parameter
     * @param data
     * @param ida
     * @param signature
     * @param publicKey
     * @return
     */
    public static boolean verify(ECParameters ecParam, byte[] data, byte[] ida, 
                                 byte[] signed, byte[] publicKey) {
        Signature signature = new Signature(signed);
        if (!isBetween(signature.r, BigInteger.ONE, ecParam.n)
            || !isBetween(signature.s, BigInteger.ONE, ecParam.n)) {
            return false;
        }

        ECPoint pubKey = getPublicKey(ecParam, publicKey);
        data = Bytes.concat(calcZ(ecParam, ida, pubKey), data);
        BigInteger e = new BigInteger(1, SM3Digest.getInstance().doFinal(data));
        BigInteger t = signature.r.add(signature.s).mod(ecParam.n);

        if (t.equals(BigInteger.ZERO)) {
            return false;
        }

        ECPoint p1 = ecParam.pointG.multiply(signature.s).normalize();
        ECPoint p2 = pubKey.multiply(t).normalize();
        BigInteger x1 = p1.add(p2).normalize().getXCoord().toBigInteger();
        BigInteger R = e.add(x1).mod(ecParam.n);
        return R.equals(signature.r);
    }

    public static boolean checkPublicKey(ECParameters ecParam, byte[] publicKey) {
        return checkPublicKey(ecParam, getPublicKey(ecParam, publicKey));
    }

    public static boolean checkPublicKey(ECParameters ecParam, ECPoint publicKey) {
        if (publicKey.isInfinity()) {
            return false;
        }

        BigInteger x = publicKey.getXCoord().toBigInteger();
        BigInteger y = publicKey.getYCoord().toBigInteger();
        if (isBetween(x, new BigInteger("0"), ecParam.p)
            && isBetween(y, new BigInteger("0"), ecParam.p)) {
            BigInteger xResult = x.pow(3).add(ecParam.a.multiply(x))
                                         .add(ecParam.b).mod(ecParam.p);
            BigInteger yResult = y.pow(2).mod(ecParam.p);
            if (yResult.equals(xResult)
                && publicKey.multiply(ecParam.n).isInfinity()) {
                return true;
            }
        }

        return false;
    }

    /**
     * 取得用户标识字节数组
     * @param ecParam the ec parameter
     * @param ida
     * @param pubKey
     * @return
     */
    static byte[] calcZ(ECParameters ecParam, byte[] ida, ECPoint pubKey) {
        int entlenA = ida.length * 8;
        byte[] entla = { (byte) (entlenA & 0xFF00), (byte) (entlenA & 0x00FF) };
        byte[] data = Bytes.concat(entla, ida, ecParam.a.toByteArray(), ecParam.b.toByteArray(), 
                                   ecParam.gx.toByteArray(), ecParam.gy.toByteArray(),
                                   pubKey.getXCoord().toBigInteger().toByteArray(),
                                   pubKey.getYCoord().toBigInteger().toByteArray());
        return SM3Digest.getInstance().doFinal(data);
    }

    /**
     * generate the random which is lesser than max number
     * @param max
     * @return BigInteger
     */
    static BigInteger random(BigInteger max) {
        BigInteger random = new BigInteger(256, SECURE_RANDOM);
        while (random.compareTo(max) >= 0) {
            random = new BigInteger(128, SECURE_RANDOM);
        }
        return random;
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
     * check the number is between min(inclusion) and max(exclusion)
     * @param number the value
     * @param min   the min number, inclusion
     * @param max   the max number, exclusion
     * @return {@code true} is between
     */
    private static boolean isBetween(BigInteger number, BigInteger min, 
                                     BigInteger max) {
        return number.compareTo(min) >= 0 && number.compareTo(max) < 0;
    }

    /**
     * SM2WithSM3 signature
     */
    private static class Signature implements java.io.Serializable {
        private static final long serialVersionUID = -2732762291362285185L;
        static final int INT_BYTE_LEN = 4;

        final BigInteger r;
        final BigInteger s;

        Signature(BigInteger r, BigInteger s) {
            this.r = r;
            this.s = s;
        }

        Signature(byte[] signed) {
            int rLen = Bytes.toInt(Arrays.copyOf(signed, INT_BYTE_LEN));
            this.r = new BigInteger(1, Arrays.copyOfRange(signed, INT_BYTE_LEN, INT_BYTE_LEN + rLen));
            this.s = new BigInteger(1, Arrays.copyOfRange(signed, INT_BYTE_LEN + rLen, signed.length));
        }

        byte[] toByteArray() {
            byte[] rBytes = r.toByteArray(); // fixed 32 byte length
            return Bytes.concat(Bytes.fromInt(rBytes.length), rBytes, s.toByteArray());
        }

        public @Override String toString() {
            return Base64.getUrlEncoder().withoutPadding().encodeToString(toByteArray());
        }
    }

    public static void main(String[] args) {
        ECParameters ecParameter = ECParameters.secp256r1;
        for (int i = 0; i < 5; i++) {
            byte[] data = MavenProjects.getMainJavaFileAsLineString(SM2.class).substring(0, 100).getBytes();
            Map<String, byte[]> keyMap = generateKeyPair(ecParameter);

            System.out.println("\n=============================加密/解密============================");
            byte[] encrypted = encrypt(ecParameter, keyMap.get(PUBLIC_KEY), data);
            byte[] decrypted = decrypt(ecParameter, keyMap.get(PRIVATE_KEY), encrypted);
            System.out.println(new String(decrypted));

            System.out.println("\n=============================签名/验签============================");
            byte[] signed = sign(ecParameter, data, "IDA".getBytes(), getPublicKey(keyMap), getPrivateKey(keyMap));
            System.out.println(Base64.getUrlEncoder().withoutPadding().encodeToString(signed));
            System.out.println(verify(ecParameter, data, "IDA".getBytes(), signed, getPublicKey(keyMap)));
        }

        byte[] data = MavenProjects.getMainJavaFileAsLineString(SM2.class).substring(0, 100).getBytes();
        Map<String, byte[]> keyMap = generateKeyPair(ecParameter);
        System.out.println("\ncheckPublicKey: "+checkPublicKey(ecParameter, getPublicKey(keyMap)));
        for (int i = 0; i < 5; i++) {
            System.out.println("\n=============================加密/解密============================");
            byte[] encrypted = encrypt(ecParameter, keyMap.get(PUBLIC_KEY), data);
            byte[] decrypted = decrypt(ecParameter, keyMap.get(PRIVATE_KEY), encrypted);
            System.out.println(new String(decrypted));

            System.out.println("\n=============================签名/验签============================");
            byte[] signed = sign(ecParameter, data, "IDA".getBytes(), getPublicKey(keyMap), getPrivateKey(keyMap));
            System.out.println(Base64.getUrlEncoder().withoutPadding().encodeToString(signed));
            System.out.println(verify(ecParameter, data, "IDA".getBytes(), signed, getPublicKey(keyMap)));
        }
    }
}
