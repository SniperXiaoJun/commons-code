package code.ponfee.commons.jce;

import java.math.BigInteger;
import java.security.SecureRandom;

import org.bouncycastle.crypto.generators.ECKeyPairGenerator;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECKeyGenerationParameters;
import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.math.ec.ECPoint;

import code.ponfee.commons.math.Numbers;

/** 
 * Specifications completely defining an elliptic curve. Used to define an
 * elliptic curve by EllipticCurve.define(ECParamters ecp).
 * NOTE: This is designed for an elliptic curve on the form:
 * y^2 = x^3 + ax + b (mod p)
 * with fixed generator and precomputed order.
 * @author Ponfee
*/
public class ECParameters implements java.io.Serializable {

    private static final long serialVersionUID = 6479779256927237118L;

    private static final char SEPARATOR = ',';

    public static final ECParameters SM2_BEST = new ECParameters(
        "FFFFFFFEFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF00000000FFFFFFFFFFFFFFFF",
        "FFFFFFFEFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF00000000FFFFFFFFFFFFFFFC",
        "28E9FA9E9D9F5E344D5A9E4BCF6509A7F39789F515AB8F92DDBCBD414D940E93",
        "32C4AE2C1F1981195F9904466A39C9948FE30BBFF2660BE1715A4589334C74C7",
        "BC3736A2F4F6779C59BDCEE36B692153D0A9877CC62A474002DF32E52139F0A0",
        "FFFFFFFEFFFFFFFFFFFFFFFFFFFFFFFF7203DF6B21C6052B53BBF40939D54123",
        "1", // placeholder
        "sm2-best"
    );

    public static final ECParameters SM2_CUST = new ECParameters(
       "8542D69E4C044F18E8B92435BF6FF7DE457283915C45517D722EDB8B08F1DFC3",
       "787968B4FA32C3FD2417842E73BBFEFF2F3C848B6831D7E0EC65228B3937E498",
       "63E4C6D3B23B0C849CF84241484BFE48F61D59A5B16BA06E6E12D1DA27C5249A",
       "421DEBD61B62EAB6746434EBC3CC315E32220B3BADD50BDC4C4E6C147FEDD43D",
       "0680512BCBB42C07D47349D2153B70C4E5D7FDFCBFA36EA1A85841B9E46E09A2",
       "8542D69E4C044F18E8B92435BF6FF7DD297720630485628D5AE74EE7C32E79B7",
       "1", // placeholder
       "sm2-cust"
   );

    public static final ECParameters secp112r1 = new ECParameters(
        "DB7C2ABF62E35E668076BEAD208B", 
        "DB7C2ABF62E35E668076BEAD2088", 
        "659EF8BA043916EEDE8911702B22", 
        "09487239995A5EE76B55F9C2F098", 
        "A89CE5AF8724C0A23E0E0FF77500", 
        "DB7C2ABF62E35E7628DFAC6561C5", 
        "00F50B028E4D696E676875615175290472783FB1", 
        "secp112r1"
    );

    public static final ECParameters secp160r1 = new ECParameters(
        "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF7FFFFFFF", 
        "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF7FFFFFFC", 
        "1C97BEFC54BD7A8B65ACF89F81D4D4ADC565FA45", 
        "4A96B5688EF573284664698968C38BB913CBFC82", 
        "23A628553168947D59DCC912042351377AC5FB32", 
        "0100000000000000000001F4C8F927AED3CA752257", 
        "1053CDE42C14D696E67687561517533BF3F83345", 
        "secp160r1"
    );

    public static final ECParameters secp256r1 = new ECParameters(
        "FFFFFFFF00000001000000000000000000000000FFFFFFFFFFFFFFFFFFFFFFFF", 
        "FFFFFFFF00000001000000000000000000000000FFFFFFFFFFFFFFFFFFFFFFFC", 
        "5AC635D8AA3A93E7B3EBBD55769886BC651D06B0CC53B0F63BCE3C3E27D2604B", 
        "6B17D1F2E12C4247F8BCE6E563A440F277037D812DEB33A0F4A13945D898C296", 
        "4FE342E2FE1A7F9B8EE7EB4A7C0F9E162BCE33576B315ECECBB6406837BF51F5", 
        "FFFFFFFF00000000FFFFFFFFFFFFFFFFBCE6FAADA7179E84F3B9CAC2FC632551", 
        "C49D360886E704936A6678E1139D26B7819F7E90", 
        "secp256r1"
    );

    /** init parameter */
    public final BigInteger p;
    public final BigInteger a;
    public final BigInteger b;
    public final BigInteger n;
    public final BigInteger gx;
    public final BigInteger gy;
    public final BigInteger S;
    public final String name;

    /** build parameter */
    public transient final ECCurve curve; // the curve
    public transient final ECPoint pointG; // the base point
    public transient final ECDomainParameters bcSpec;
    public transient final ECKeyPairGenerator keyPairGenerator;
 
    public ECParameters(String p, String a, String b, 
                       String gx, String gy, String n, 
                       String S, String name) {
        this.p  = Numbers.toBigInteger(p);
        this.a  = Numbers.toBigInteger(a);
        this.b  = Numbers.toBigInteger(b);
        this.n  = Numbers.toBigInteger(n);
        this.gx = Numbers.toBigInteger(gx);
        this.gy = Numbers.toBigInteger(gy);
        this.S  = Numbers.toBigInteger(S);
        this.name = name;
        
        ECCurve curve = null;
        ECPoint pointG = null;
        ECDomainParameters bcSpec = null;
        ECKeyPairGenerator keyPairGenerator = null;
        try {
            curve = new ECCurve.Fp(this.p, this.a, this.b);
            pointG = curve.createPoint(this.gx, this.gy);
            bcSpec = new ECDomainParameters(curve, pointG, this.n);
            keyPairGenerator = new ECKeyPairGenerator();
            keyPairGenerator.init(new ECKeyGenerationParameters(bcSpec, new SecureRandom()));
        } catch (Exception ignored) {
            System.err.println(name + " -> " + ignored.getMessage());
        }

        this.curve = curve;
        this.pointG = pointG;
        this.bcSpec = bcSpec;
        this.keyPairGenerator = keyPairGenerator;
    }

    public @Override String toString() {
        return new StringBuilder()
                   .append(Numbers.toHex(p)).append(SEPARATOR)
                   .append(Numbers.toHex(a)).append(SEPARATOR)
                   .append(Numbers.toHex(b)).append(SEPARATOR)
                   .append(Numbers.toHex(n)).append(SEPARATOR)
                   .append(Numbers.toHex(gx)).append(SEPARATOR)
                   .append(Numbers.toHex(gy)).append(SEPARATOR)
                   .append(Numbers.toHex(S)).append(SEPARATOR)
                   .append(name).toString();
    }

    public static ECParameters fromString(String parameter) {
        String[] array = parameter.split(String.valueOf(SEPARATOR), 8);
        return new ECParameters(array[0], array[1], 
                               array[2], array[3], 
                               array[4], array[5],
                               array[6], array[7]);
    }

    public static void main(String[] args) {
        String s = SM2_BEST.toString();
        System.out.println(s);
        System.out.println(fromString(s));
    }
}
