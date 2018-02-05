package code.ponfee.commons.jce.ecc;

import java.math.BigInteger;
import java.util.Arrays;

import code.ponfee.commons.jce.Cryptor;
import code.ponfee.commons.jce.Key;
import code.ponfee.commons.jce.hash.HashUtils;

/**
 * EC Cryptor based xor
 * 
 * origin ≡ origin ⊕   key ⊕  key
 * 
 * 一、首先：生成随机数dk，在曲线上计算得到dk的倍点beta point， 
 *        beta point(public key) = basePointG(public key) * dk，
 *        beta point(public key)作为公钥，dk作为私钥
 * 
 * 二、加密：1）生成随机数rk，在曲线上计算得到rk的倍点gamma point，
 *          gamma point(public key) = basePointG(public key) * rk，
 *          由椭圆曲线特性可得出：beta point(public key) * rk = ECPoint S = gamma point(public key) * dk
 * 
 *          2）ECPoint S = beta point(public key) * rk，把ECPoint S作为中间对称密钥，
 *            通过HASH函数计算对称加密密钥：key = SHA-512(ECPoint S)
 * 
 *          3）加密：origin ⊕  key = cipher 
 * 
 *          4）打包加密数据Encrypted = {gamma point(public key), cipher}
 * 
 * 三、解密：1）解析加密数据Encrypted： {gamma point(public key), cipher}，得到：gamma point(public key)，cipher
 * 
 *          2）用第一步的私钥dk与gamma point(public key)进行计算得到：ECPoint S = gamma point(public key) * dk
 * 
 *          3）通过HASH函数计算对称加密密钥：key = SHA-512(ECPoint S)
 * 
 *          4）解密：cipher ⊕   key = origin
 * 
 * @author Ponfee
 */
public class ECCryptor extends Cryptor {

    private final EllipticCurve curve;

    public ECCryptor(EllipticCurve curve) {
        this.curve = curve;
    }

    /**
     * 加密数据逻辑：
     * origin ≡ origin ⊕ data ⊕ data
     */
    public @Override byte[] encrypt(byte[] input, int length, Key ek) {
        // ek is an Elliptic key (dk=secret, beta=public)
        ECKey ecKey = (ECKey) ek;

        // 生成随机数rk
        BigInteger rk;
        if (ecKey.curve.getN() != null) {
            rk = Cryptor.random(ecKey.curve.getN());
        } else {
            rk = Cryptor.random(ecKey.curve.getP().bitLength() + 17);
        }

        // 计算曲线上rk倍点gamma：ECPoint gamma = basePointG(public key) * rk
        ECPoint gamma = ecKey.curve.getBasePointG().multiply(rk);

        // PCS is compressed point size.
        int offset = ecKey.curve.getPCS();

        // 导出该rk倍点gamma point(public key)
        byte[] result = Arrays.copyOf(gamma.compress(), offset + length);

        // 生成需要hash的数据：ECPoint S = beta point(public key) * rk
        ECPoint secure = ecKey.beta.multiply(rk);

        // 用hash值与原文进行xor操作
        byte[] hashed = HashUtils.sha512(secure.getX().toByteArray(), secure.getY().toByteArray());
        for (int hLen = hashed.length, i = 0, j = 0; i < length; i++, j++) {
            if (j == hLen) {
                j = 0;
            }
            result[i + offset] = (byte) (input[i] ^ hashed[j]);
        }
        return result;
    }

    public @Override byte[] decrypt(byte[] input, Key dk) {
        ECKey ecKey = (ECKey) dk;
        int offset = ecKey.curve.getPCS();

        // 取出gamma point(public key)
        byte[] gammacom = Arrays.copyOfRange(input, 0, offset);
        ECPoint gamma = new ECPoint(gammacom, ecKey.curve);

        // beta point(public key) * rk = ECPoint S = gamma point(public key) * dk
        // ECPoint S = gamma point(public key) * dk
        ECPoint secure = gamma.multiply(ecKey.dk);

        byte[] hashed;
        if (secure.isZero()) {
            hashed = HashUtils.sha512(BigInteger.ZERO.toByteArray(), BigInteger.ZERO.toByteArray());
        } else {
            hashed = HashUtils.sha512(secure.getX().toByteArray(), secure.getY().toByteArray());
        }

        int length = input.length - offset;
        byte[] result = new byte[length];
        for (int hLen = hashed.length, i = 0, j = 0; i < length; i++, j++) {
            if (j == hLen) {
                j = 0;
            }
            result[i] = (byte) (input[i + offset] ^ hashed[j]);
        }
        return result;
    }

    /**
     * generate ECKey
     */
    public @Override Key generateKey() {
        return new ECKey(curve);
    }

    public String toString() {
        return "ECCryptor - " + curve.toString();
    }

}
