package code.ponfee.commons.jce.ecc;

import java.math.BigInteger;

/**
 * The EC point of lie on the curve
 * @author Ponfee
 */
public class ECPoint {

    public final static BigInteger TWO = new BigInteger("2");
    public final static BigInteger THREE = new BigInteger("3");

    private EllipticCurve curve;

    private BigInteger x;
    private BigInteger y;
    private boolean iszero;

    // fastcache is an array of ECPoints
    private ECPoint[] fastcache = null;

    public void fastCache() {
        if (fastcache == null) {
            // fastcache initialised to 256 EC Points.
            fastcache = new ECPoint[256];
            // First point is null.
            fastcache[0] = new ECPoint(curve);
            for (int i = 1; i < fastcache.length; i++) { // From 1 to 256
                // add the point repeatedly (Cumulative sum). P,2P,...
                fastcache[i] = fastcache[i - 1].add(this);
            }
        }
    }

    /** 
     * Constructs a point on an elliptic curve.
     * @param curve The elliptic curve on wich the point is surposed to lie
     * @param x     The x coordinate of the point
     * @param y     The y coordinate of the point
     */
    public ECPoint(EllipticCurve curve, BigInteger x, BigInteger y) {
        this.curve = curve;
        this.x = x;
        this.y = y;
        if (!curve.onCurve(this)) {
            throw new IllegalArgumentException("(x,y) is not on this curve!");
        }
        this.iszero = false;
    }

    /**
     * Decompresses a compressed point stored in a byte-array into a new ECPoint.
     * @param bytes the array of bytes to be decompressed
     * @param curve the EllipticCurve the decompressed point is supposed to lie on.
     */
    public ECPoint(byte[] bytes, EllipticCurve curve) {
        this.curve = curve;
        if (bytes[0] == 2) {
            this.iszero = true;
            return;
        }
        boolean ymt = false;
        if (bytes[0] != 0) ymt = true;
        bytes[0] = 0;
        this.x = new BigInteger(1, bytes);
        if (curve.getPPODBF() == null) {
            System.err.println("ppodbf is null");
        }
        this.y = this.x.multiply(this.x).add(curve.geta()).multiply(this.x)
                       .add(curve.getb()).modPow(curve.getPPODBF(), curve.getp());
        if (ymt != this.y.testBit(0)) {
            this.y = curve.getp().subtract(this.y);
        }
        this.iszero = false;
    }

    /**
     * IMPORTANT this renders the values of x and y to be null! 
     * Use this constructor only to create instances of a Zero class!
     */
    public ECPoint(EllipticCurve e) {
        this.x = this.y = BigInteger.ZERO;
        this.curve = e;
        this.iszero = true;
    }

    public byte[] compress() {
        byte[] cmp = new byte[curve.getPCS()];
        if (iszero) {
            cmp[0] = 2;
        }
        byte[] xb = x.toByteArray();
        System.arraycopy(xb, 0, cmp, curve.getPCS() - xb.length, xb.length);
        if (y.testBit(0)) {
            cmp[0] = 1;
        }
        return cmp;
    }

    /**
     * Adds another elliptic curve point to this point.
     * @param q The point to be added
     * @return the sum of this point on the argument
     */
    public ECPoint add(ECPoint q) {
        if (!hasCurve(q)) {
            throw new IllegalArgumentException("the q point don't lie on "
                                             + "the same elliptic curve.");
        }

        if (this.iszero) {
            return q;
        } else if (q.isZero()) {
            return this;
        }

        BigInteger y1 = this.y;
        BigInteger y2 = q.gety();
        BigInteger x1 = this.x;
        BigInteger x2 = q.getx();

        BigInteger alpha;
        if (x2.compareTo(x1) == 0) {
            if (!(y2.compareTo(y1) == 0)) {
                return new ECPoint(curve);
            } else {
                alpha = ((x1.modPow(TWO, curve.getp())).multiply(THREE)).add(curve.geta());
                alpha = (alpha.multiply((TWO.multiply(y1)).modInverse(curve.getp()))).mod(curve.getp());
            }
        } else {
            BigInteger i = x2.subtract(x1).modInverse(curve.getp());
            alpha = y2.subtract(y1).multiply(i).mod(curve.getp());
        }

        BigInteger x3, y3;
        x3 = (((alpha.modPow(TWO, curve.getp())).subtract(x2)).subtract(x1)).mod(curve.getp());
        y3 = ((alpha.multiply(x1.subtract(x3))).subtract(y1)).mod(curve.getp());

        return new ECPoint(curve, x3, y3);
    }

    public ECPoint multiply(BigInteger coef) {
        int nk = coef.bitCount(); // nk in paper.
        ECPoint result = this;
        for (int i = nk - 1; i > 0; i--) {
            try {
                result = result.add(result);
                if (coef.testBit(i)) result = result.add(this);
            } catch (Exception e) {
                System.err.println("Error in multiplying");
            }
        }
        return result;
    }

    public BigInteger getx() {
        return x;
    }

    public BigInteger gety() {
        return y;
    }

    public EllipticCurve getCurve() {
        return curve;
    }

    public String toString() {
        return "(" + x.toString() + ", " + y.toString() + ")";
    }

    public boolean hasCurve(ECPoint p) {
        return this.curve.equals(p.getCurve());
    }

    public boolean isZero() {
        return iszero;
    }
}
