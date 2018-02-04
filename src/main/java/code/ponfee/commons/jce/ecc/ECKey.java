package code.ponfee.commons.jce.ecc;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;

import code.ponfee.commons.jce.Cryptor;
import code.ponfee.commons.jce.Key;

/**
 * This is Elliptic Curve key
 * @author Ponfee
 */
public class ECKey implements Key {

    protected boolean secret; // 是否是私钥
    protected BigInteger dk; // decrypt key
    protected ECPoint beta; // the public key of ECPoint
    protected final EllipticCurve curve; // the Elliptic cCurve

    /**
     * ECKey generates a random secret key (contains also the public key)
     * @param ec
     */
    public ECKey(EllipticCurve ec) {
        this.curve = ec;
        this.secret = true;

        // dk is a random num.
        if (curve.getN() != null) {
            this.dk = Cryptor.random(curve.getN());
        } else {
            this.dk = Cryptor.random(ec.getp().bitLength() + 17);
        }

        // beta = generator*dk (public key)
        beta = (curve.getGenerator()).multiply(dk);
        beta.fastCache();
    }

    public String toString() {
        if (secret) {
            return ("Private key: " + dk + " " + beta + " " + curve);
        } else {
            return ("Public key:" + beta + " " + curve);
        }
    }

    public @Override boolean isPublic() {
        return (!secret);
    }

    public @Override void writeKey(OutputStream out) throws IOException {
        DataOutputStream output = new DataOutputStream(out);
        curve.writeCurve(output);
        output.writeBoolean(secret);
        if (secret) {
            byte[] skb = dk.toByteArray();
            output.writeInt(skb.length);
            output.write(skb);
        }
        byte[] betab = beta.compress();
        output.writeInt(betab.length);
        output.write(betab);
    }

    public @Override Key readKey(InputStream in) throws IOException {
        DataInputStream input = new DataInputStream(in);
        ECKey k = new ECKey(new EllipticCurve(input));
        k.secret = input.readBoolean();
        if (k.secret) {
            byte[] skb = new byte[input.readInt()];
            input.read(skb);
            k.dk = new BigInteger(1, skb);
        }
        byte[] betab = new byte[input.readInt()];
        input.read(betab);
        k.beta = new ECPoint(betab, k.curve);
        return k;
    }

    /**
     * get the public key
     */
    public @Override Key getPublic() {
        ECKey pubKey = new ECKey(curve);
        pubKey.beta = beta;
        pubKey.dk = BigInteger.ZERO;
        pubKey.secret = false;
        System.gc();
        return pubKey;
    }

    /**
     * Turns this key into a public key 
     * (does nothing if this key is public)
     * @return
     */
    public Key getECPublic() {
        ECKey pubKey = new ECKey(curve);
        int ppodbf = curve.getPPODBF().intValue();
        int[] k = new int[ppodbf];
        for (int i = 0; i < ppodbf; i++) {
            if (i == 0) {
                k[i] = 0;
            } else if (k[i - 1] == 0) {
                k[i] = 1;
            } else {
                k[i] = 0;
            }
        }

        ECPoint R0 = pubKey.beta;
        ECPoint R1 = pubKey.beta.multiply(new BigInteger("2"));

        for (int i = ppodbf - 1; i >= 0; i--) {
            if (k[i] == 0) {
                R1 = R0.add(R1);
                R0 = R0.multiply(new BigInteger("2"));
            } else {
                R0 = R0.add(R1);
                R1 = R1.multiply(new BigInteger("2"));
            }
        }

        pubKey.beta = R0;
        pubKey.dk = BigInteger.ZERO;
        pubKey.secret = false;
        System.gc();
        return pubKey;
    }
}
