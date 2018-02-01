package code.ponfee.commons.jce.ecc;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;

public class ECKey implements Key {
    /** There are to kinds of keys secret and public */
    protected boolean secret;
    protected BigInteger sk;
    protected ECPoint beta;
    protected EllipticCurve mother;

    /** ECKey generates a random secret key (contains also the public key)*/
    public ECKey(EllipticCurve ec) {
        mother = ec;
        secret = true;
        sk = new BigInteger(ec.getp().bitLength() + 17, Cryptor.SECURE_RANDOM); //sk is a random num.
        if (mother.getN() != null) {
            sk = sk.mod(mother.getN()); //sk=sk%order
        }
        beta = (mother.getGenerator()).multiply(sk); //beta=generator*sk (public key)
        beta.fastCache();
    }

    public String toString() {
        if (secret) {
            return ("Private key: " + sk + " " + beta + " " + mother);
        } else {
            return ("Public key:" + beta + " " + mother);
        }
    }

    public boolean isPublic() {
        return (!secret);
    }

    public void writeKey(OutputStream out) throws IOException {
        DataOutputStream output = new DataOutputStream(out);
        mother.writeCurve(output);
        output.writeBoolean(secret);
        if (secret) {
            byte[] skb = sk.toByteArray();
            output.writeInt(skb.length);
            output.write(skb);
        }
        byte[] betab = beta.compress();
        output.writeInt(betab.length);
        output.write(betab);
    }

    public Key readKey(InputStream in) throws IOException {
        DataInputStream input = new DataInputStream(in);
        ECKey k = new ECKey(new EllipticCurve(input));
        k.secret = input.readBoolean();
        if (k.secret) {
            byte[] skb = new byte[input.readInt()];
            input.read(skb);
            k.sk = new BigInteger(skb);
        }
        byte[] betab = new byte[input.readInt()];
        input.read(betab);
        k.beta = new ECPoint(betab, k.mother);
        return k;
    }

    /** Turns this key into a public key (does nothing if this key is public) */
    public Key getPublic() {
        ECKey pubKey = new ECKey(mother);
        pubKey.beta = beta;
        pubKey.sk = BigInteger.ZERO;
        pubKey.secret = false;
        System.gc();
        return pubKey;
    }

    /** Turns this key into a public key (does nothing if this key is public) */
    public Key getECPublic() throws NoCommonMotherException {
        ECKey pubKey = new ECKey(mother);
        int ppodbf = mother.getPPODBF().intValue();
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
        pubKey.sk = BigInteger.ZERO;
        pubKey.secret = false;
        System.gc();
        return pubKey;
    }
}
