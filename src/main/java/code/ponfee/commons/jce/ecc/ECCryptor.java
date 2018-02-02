package code.ponfee.commons.jce.ecc;

import java.math.BigInteger;
import java.util.Arrays;

import code.ponfee.commons.jce.hash.HashUtils;
import code.ponfee.commons.util.Bytes;
import code.ponfee.commons.util.ObjectUtils;

/**
 * EC Cryptor base xor
 * 首先：在曲线上生成dk倍点beta， beta point为公钥beta(public key)，dk为私钥
 * 加密：生成随机数rk，在曲线上计算得到rk倍点的alpha，alpha point为公钥alpha(public key)，rk为私钥
 *     ECPoint S = beta(public key) * rk，并把alpha(public key)数据放入加密数据中
 * 解密：解析得到alpha(public key)，用私钥dk解密
 *     ECPoint S = alpha(public key) * dk
 * 
 * 即：[beta(public key), dk]，[alpha(public key), rk]
 *    beta(public key) * rk = ECPoint S = alpha(public key) * dk
 * @author Ponfee
 */
public class ECCryptor extends Cryptor {

    private EllipticCurve curve;

    public ECCryptor(EllipticCurve curve) {
        this.curve = curve;
    }

    public byte[] encrypt(byte[] input, int length, Key ek) {
        // ek is an Elliptic key (sk=secret, beta=public)
        ECKey ecKey = (ECKey) ek;

        // PCS is compressed point size.
        int offset = ecKey.mother.getPCS();

        // 生成随机数rk
        BigInteger rk = new BigInteger(ecKey.mother.getp().bitLength() + 17, Cryptor.SECURE_RANDOM);
        rk = new BigInteger(1, Bytes.concat(rk.toByteArray(), ObjectUtils.uuid()));
        if (ecKey.mother.getN() != null) {
            rk = rk.mod(ecKey.mother.getN()); // rk = rk % n
        }

        // 计算曲线上rk倍点alpha：ECPoint gamma = alpha(public key) * rk
        ECPoint gamma = ecKey.mother.getGenerator().multiply(rk);

        // 导出该rk倍点alpha(public key)
        byte[] result = Arrays.copyOf(gamma.compress(), offset + length);

        // 生成需要hash的数据：ECPoint sec = beta(public key) * rk
        ECPoint sec = ecKey.beta.multiply(rk);

        // 用HASH值与原文进行xor操作
        byte[] digest = HashUtils.sha512(sec.getx().toByteArray(), sec.gety().toByteArray());
        int dLen = digest.length;
        for (int j = 0; j < length; j++) {
            result[j + offset] = (byte) (input[j] ^ digest[j % dLen]);
        }
        return result;
    }

    public byte[] decrypt(byte[] input, Key dk) {
        ECKey ecKey = (ECKey) dk;
        int offset = ecKey.mother.getPCS();

        // 取出被加密的密码：alpha(public key)
        byte[] gammacom = Arrays.copyOfRange(input, 0, offset);
        ECPoint gamma = new ECPoint(gammacom, ecKey.mother);

        // EC解密被加密的密码：ECPoint sec = alpha(public key) * dk
        // beta(public key) * rk = ECPoint S = alpha(public key) * dk
        ECPoint sec = gamma.multiply(ecKey.dk);

        byte[] digest;
        if (sec.isZero()) {
            digest = HashUtils.sha512(BigInteger.ZERO.toByteArray(), BigInteger.ZERO.toByteArray());
        } else {
            digest = HashUtils.sha512(sec.getx().toByteArray(), sec.gety().toByteArray());
        }

        byte[] result = new byte[input.length - offset];
        int dLen = digest.length;
        for (int j = 0; j < input.length - offset; j++) {
            result[j] = (byte) (input[j + offset] ^ digest[j % dLen]);
        }
        return result;
    }

    /**
     * This method generates a new key for the cryptosystem.
     * @return the new key generated
     */
    public Key generateKey() {
        return new ECKey(curve);
    }

    public String toString() {
        return "ECCryptor - " + curve.toString();
    }

}
