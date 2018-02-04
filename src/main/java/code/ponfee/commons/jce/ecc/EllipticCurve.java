package code.ponfee.commons.jce.ecc;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigInteger;

import code.ponfee.commons.jce.ECParameters;

/**
 * An implementation of an elliptic curve over a -finite- field.
 * @author Ponfee
 */
public class EllipticCurve {

    public static final BigInteger COEFA = new BigInteger("4");
    public static final BigInteger COEFB = new BigInteger("27");
    public static final int PRIMESECURITY = 500;

    private BigInteger a, b, p, n;
    private ECPoint generator; // point G
    private BigInteger ppodbf;
    private int pointcmpsize;
    private String name;

    /** 
     * Constructs an elliptic curve over the finite field of 'mod' elements.
     * The equation of the curve is on the form : y^2 = x^3 + ax + b.
     * @param a the value of 'a' where y^2 = x^3 + ax + b
     * @param b the value of 'b' where y^2 = x^3 + ax + b
     */
    public EllipticCurve(BigInteger a, BigInteger b, BigInteger p) {
        this.a = a;
        this.b = b;
        this.p = p;
        if (!p.isProbablePrime(PRIMESECURITY)) {
            throw new IllegalArgumentException("the p is not prime!");
        }
        if (isSingular()) {
            throw new IllegalArgumentException("is singular!");
        }

        byte[] pb = p.toByteArray();
        if (pb[0] == 0) {
            this.pointcmpsize = pb.length;
        } else {
            this.pointcmpsize = pb.length + 1;
        }
        name = "cust";
        calculateN();
        calculateGenerator();
    }

    public EllipticCurve(ECParameters ecp) {
        this(ecp.a, ecp.b, ecp.p);
        this.n = ecp.n;
        this.name = ecp.toString();
        this.generator = new ECPoint(this, ecp.gx, ecp.gy);
        generator.fastCache();
    }

    public void writeCurve(DataOutputStream output) 
        throws IOException {
        byte[] ab = a.toByteArray();
        output.writeInt(ab.length);
        output.write(ab);
        byte[] bb = b.toByteArray();
        output.writeInt(bb.length);
        output.write(bb);
        byte[] pb = p.toByteArray();
        output.writeInt(pb.length);
        output.write(pb);
        byte[] ob = n.toByteArray();
        output.writeInt(ob.length);
        output.write(ob);
        byte[] gb = generator.compress();
        output.writeInt(gb.length);
        output.write(gb);
        byte[] ppb = getPPODBF().toByteArray();
        output.writeInt(ppb.length);
        output.write(ppb);
        output.writeInt(pointcmpsize);
        output.writeUTF(name);
    }

    protected EllipticCurve(DataInputStream input) 
        throws IOException {
        byte[] ab = new byte[input.readInt()];
        input.read(ab);
        a = new BigInteger(1, ab);
        byte[] bb = new byte[input.readInt()];
        input.read(bb);
        b = new BigInteger(1, bb);
        byte[] pb = new byte[input.readInt()];
        input.read(pb);
        p = new BigInteger(1, pb);
        byte[] ob = new byte[input.readInt()];
        input.read(ob);
        n = new BigInteger(1, ob);
        byte[] gb = new byte[input.readInt()];
        input.read(gb);
        generator = new ECPoint(gb, this);
        byte[] ppb = new byte[input.readInt()];
        input.read(ppb);
        ppodbf = new BigInteger(1, ppb);
        pointcmpsize = input.readInt();
        name = input.readUTF();
        generator.fastCache();
    }

    public boolean isSingular() {
        BigInteger aa = a.pow(3);
        BigInteger bb = b.pow(2);
        BigInteger result = aa.multiply(COEFA).add(bb.multiply(COEFB)).mod(p);
        return result.compareTo(BigInteger.ZERO) == 0;
    }

    public BigInteger calculateN() {
        return null; // TODO
    }

    public ECPoint calculateGenerator() {
        return null; // TODO
    }

    public boolean onCurve(ECPoint q) {
        if (q.isZero()) {
            return true;
        }
        BigInteger y_square = (q.gety()).modPow(new BigInteger("2"), p);
        BigInteger x_cube = (q.getx()).modPow(new BigInteger("3"), p);
        BigInteger x = q.getx();

        BigInteger dum = ((x_cube.add(a.multiply(x))).add(b)).mod(p);
        return y_square.compareTo(dum) == 0;
    }

    public BigInteger getN() {
        return n;
    }

    public ECPoint getZero() {
        return new ECPoint(this);
    }

    public BigInteger geta() {
        return a;
    }

    public BigInteger getb() {
        return b;
    }

    public BigInteger getp() {
        return p;
    }

    public int getPCS() {
        return pointcmpsize;
    }

    public ECPoint getGenerator() {
        return generator;
    }

    public String toString() {
        if (name == null || name.length() == 0) {
            return "y^2 = x^3 + " + a + "x + " + b + " ( mod " + p + " )";
        } else {
            return name;
        }
    }

    public BigInteger getPPODBF() {
        if (ppodbf == null) {
            ppodbf = p.add(BigInteger.ONE).shiftRight(2);
        }
        return ppodbf;
    }
}
