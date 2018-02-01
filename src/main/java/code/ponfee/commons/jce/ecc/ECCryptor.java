package code.ponfee.commons.jce.ecc;

import java.math.BigInteger;
import java.security.MessageDigest;

public class ECCryptor extends Cryptor {
    MessageDigest hash;

    private EllipticCurve ec;

    public ECCryptor(EllipticCurve ec) {
        this.ec = ec;
        try {
            hash = MessageDigest.getInstance("SHA-1");
        } catch (java.security.NoSuchAlgorithmException e) {
            System.err.println("RSACryptoSystem: THIS CANNOT HAPPEN\n" + e);
            System.exit(0);
        }
    }

    public byte[] encrypt(byte[] input, int length, Key key) {
        ECKey ek = (ECKey) key; //ek is an Elliptic key (sk=secret, beta=public)
        byte[] res = new byte[ek.mother.getPCS() + length]; // PCS is compressed point size.
        hash.reset();

        // Get a random integer rk of ek.mother.getp()...+17 bits
        BigInteger rk = new BigInteger(ek.mother.getp().bitLength() + 17, Cryptor.SECURE_RANDOM);
        if (ek.mother.getN() != null) {
            rk = rk.mod(ek.mother.getN()); //rk=rk%order
        }
        ECPoint gamma = ek.mother.getGenerator().multiply(rk); //ECPoint gamma=generator(pre-defined) *rk
        ECPoint sec = ek.beta.multiply(rk); //ECPoint sec =beta(public key) *rk
        System.arraycopy(gamma.compress(), 0, res, 0, ek.mother.getPCS()); //arraycopy(src,srcpos,dest,destpos,length)
        hash.update(sec.getx().toByteArray()); //Update hash accordingly, will be xored with its digest, bitwise
        hash.update(sec.gety().toByteArray());
        byte[] digest = hash.digest();
        for (int j = 0; j < length; j++) {
            res[j + ek.mother.getPCS()] = (byte) (input[j] ^ digest[j]);
        }
        return res;
    }

    public byte[] decrypt(byte[] input, Key key) {
        ECKey dk = (ECKey) key;
        byte[] res = new byte[input.length - dk.mother.getPCS()];
        byte[] gammacom = new byte[dk.mother.getPCS()]; //gammacom is gamma in compressed fmt.
        hash.reset();

        System.arraycopy(input, 0, gammacom, 0, dk.mother.getPCS()); //copy the first (gammacom.size()) bytes from input, to decompress.
        ECPoint gamma = new ECPoint(gammacom, dk.mother); //gamma is gammacom, when decompressed.
        ECPoint sec = gamma.multiply(dk.sk); //sec is when gamma is multiplied with sk.(secret key of the supplied key)
        if (sec.isZero()) {
            hash.update(BigInteger.ZERO.toByteArray());
            hash.update(BigInteger.ZERO.toByteArray());
        } else {
            hash.update(sec.getx().toByteArray());
            hash.update(sec.gety().toByteArray());
        }
        byte[] digest = hash.digest();
        for (int j = 0; j < input.length - dk.mother.getPCS(); j++) {
            res[j] = (byte) (input[j + dk.mother.getPCS()] ^ digest[j]);
        }
        return res;
    }

    /** This method generates a new key for the cryptosystem.
     *@return the new key generated*/
    public Key generateKey() {
        return new ECKey(ec);
    }

    public String toString() {
        return "ECCryptor - " + ec.toString();
    }

}
