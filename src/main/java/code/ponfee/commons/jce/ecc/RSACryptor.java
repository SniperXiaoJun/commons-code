package code.ponfee.commons.jce.ecc;

import java.math.BigInteger;
import java.security.MessageDigest;

public class RSACryptor extends Cryptor {
    MessageDigest hash;

    public RSACryptor() {
        try {
            hash = MessageDigest.getInstance("SHA-1"); // non-thread-safe
        } catch (java.security.NoSuchAlgorithmException e) {
            System.out.println("RSACryptoSystem: THIS CANNOT HAPPEN\n" + e);
            System.exit(0);
        }
    }

    public byte[] encrypt(byte[] input, int length, Key key) {
        RSAKey k = (RSAKey) key;
        hash.reset();
        BigInteger hashelem = new BigInteger(k.n.bitLength() + 17, Cryptor.SECURE_RANDOM).mod(k.n);
        byte[] cryptelm = hashelem.modPow(k.e, k.n).toByteArray();
        byte[] res = new byte[cryptelm.length + length + 2];
        res[0] = (byte) ((cryptelm.length) >> 8);
        res[1] = (byte) cryptelm.length;
        System.arraycopy(cryptelm, 0, res, 2, cryptelm.length);
        hash.update(hashelem.toByteArray());
        byte[] digest = hash.digest();
        for (int j = 0; j < length; j++) {
            res[cryptelm.length + 2 + j] = (byte) (input[j] ^ digest[j]);
        }
        return res;
    }

    public byte[] decrypt(byte[] input, Key key) {
        RSAKey k = (RSAKey) key;
        byte[] cryptelm = new byte[((input[0] & 255) << 8) + input[1] & 255];
        byte[] res = new byte[input.length - 2 - cryptelm.length];
        System.arraycopy(input, 2, cryptelm, 0, cryptelm.length);
        hash.reset();
        hash.update(new BigInteger(cryptelm).modPow(k.d, k.n).toByteArray());
        byte[] digest = hash.digest();
        for (int j = 0; j < res.length; j++) {
            res[j] = (byte) (input[cryptelm.length + 2 + j] ^ digest[j]);
        }
        return res;
    }

    /**
     * This method generates a new key for the cryptosystem.
     * @return the new key generated
     */
    public Key generateKey() {
        return generateKey(2048);
    }

    public Key generateKey(int keySize) {
        return new RSAKey(keySize);
    }

    public String toString() {
        return "RSACryptor";
    }
}
