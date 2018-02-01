package test.jce.ecc0;

import java.math.BigInteger;

import code.ponfee.commons.jce.ECParameters;
import code.ponfee.commons.jce.ecc.ECPoint;
import code.ponfee.commons.jce.ecc.EllipticCurve;

public class EllipticCurveTest {

    public static void main(String[] args) throws Exception {
        BigInteger a = new BigInteger("1");
        BigInteger b = new BigInteger("6");
        BigInteger mod = new BigInteger("11");
        EllipticCurve e = new EllipticCurve(a, b, mod);
        System.out.println("EllipticCurve: " + e + " created succesfully!");

        ECPoint p1 = new ECPoint(e, new BigInteger("2"), new BigInteger("7"));
        ECPoint p2 = new ECPoint(e, new BigInteger("7"), new BigInteger("2"));
        ECPoint p3 = new ECPoint(e, new BigInteger("2"), new BigInteger("-7"));
        System.out.println(p1 + " + " + p2 + " = " + p1.add(p2));
        System.out.println(p1 + " + " + p1 + " = " + p1.add(p1));
        System.out.println(p1 + " + " + p3 + " = " + p1.add(p3));
        System.out.println(p1 + " * " + mod + " = " + p1.multiply(mod));
        System.out.println(p1 + " + " + e.getZero() + " = " + p1.add(e.getZero()));
        System.out.println(e.getZero() + " + " + e.getZero() + " = " + e.getZero().add(e.getZero()));

        System.out.println("\nTesting secp256r1===============");
        e = new EllipticCurve(ECParameters.secp256r1);
        System.out.println("New curve: " + e + " OK!");
        System.out.println("Generator: " + e.getGenerator());
        System.out.println("N: " + e.getN());

        System.out.println("\nTesting decompression of compression...");
        p1 = new ECPoint(e.getGenerator().compress(), e);
        if (e.getGenerator().getx().compareTo(p1.getx()) == 0) {
            System.out.println("x values agree...");
        } else {
            System.out.println("x values disagree...");
            System.out.println("x-before:");
            System.out.println(e.getGenerator().gety());
            System.out.println("y-after:");
            System.out.println(p1.gety());
        }
        if (e.getGenerator().gety().compareTo(p1.gety()) == 0) {
            System.out.println("y values agree...");
        } else {
            System.out.println("y values disagree...");
            System.out.println("y-before:");
            System.out.println(e.getGenerator().gety());
            System.out.println("y-after:");
            System.out.println(p1.gety());
        }
    }
}
