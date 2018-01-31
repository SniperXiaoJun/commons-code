package code.ponfee.commons.jce.sm;

import java.math.BigInteger;
import java.security.SecureRandom;

import org.bouncycastle.crypto.generators.ECKeyPairGenerator;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECKeyGenerationParameters;
import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.math.ec.ECPoint;

import code.ponfee.commons.math.Numbers;

/**
 * EC parameter
 * @author Ponfee
 */
public class ECParameter {

    private static final char SEPARATOR = ',';

    public static final ECParameter DEFAULT_EC_PARAM = new ECParameter(
        new BigInteger("FFFFFFFEFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF00000000FFFFFFFFFFFFFFFF", 16), 
        new BigInteger("FFFFFFFEFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF00000000FFFFFFFFFFFFFFFC", 16), 
        new BigInteger("28E9FA9E9D9F5E344D5A9E4BCF6509A7F39789F515AB8F92DDBCBD414D940E93", 16), 
        new BigInteger("FFFFFFFEFFFFFFFFFFFFFFFFFFFFFFFF7203DF6B21C6052B53BBF40939D54123", 16), 
        new BigInteger("32C4AE2C1F1981195F9904466A39C9948FE30BBFF2660BE1715A4589334C74C7", 16), 
        new BigInteger("BC3736A2F4F6779C59BDCEE36B692153D0A9877CC62A474002DF32E52139F0A0", 16)
    );

    public static final ECParameter TEST_EC_PARAM = new ECParameter(
       new BigInteger("8542D69E4C044F18E8B92435BF6FF7DE457283915C45517D722EDB8B08F1DFC3", 16), 
       new BigInteger("787968B4FA32C3FD2417842E73BBFEFF2F3C848B6831D7E0EC65228B3937E498", 16), 
       new BigInteger("63E4C6D3B23B0C849CF84241484BFE48F61D59A5B16BA06E6E12D1DA27C5249A", 16), 
       new BigInteger("8542D69E4C044F18E8B92435BF6FF7DD297720630485628D5AE74EE7C32E79B7", 16), 
       new BigInteger("421DEBD61B62EAB6746434EBC3CC315E32220B3BADD50BDC4C4E6C147FEDD43D", 16), 
       new BigInteger("0680512BCBB42C07D47349D2153B70C4E5D7FDFCBFA36EA1A85841B9E46E09A2", 16)
   );

    /** init parameter */
    public final BigInteger p;
    public final BigInteger a;
    public final BigInteger b;
    public final BigInteger n;
    public final BigInteger gx;
    public final BigInteger gy;

    /** build parameter */
    public final ECCurve curve; // the curve
    public final ECPoint pointG; // the base point
    public final ECDomainParameters bcSpec;
    public final ECKeyPairGenerator keyPairGenerator = new ECKeyPairGenerator();
 
    public ECParameter(BigInteger p, BigInteger a, BigInteger b, 
                       BigInteger n, BigInteger gx, BigInteger gy) {
        this.p = p;
        this.a = a;
        this.b = b;
        this.n = n;
        this.gx = gx;
        this.gy = gy;
        this.curve = new ECCurve.Fp(p, a, b);
        this.pointG = curve.createPoint(gx, gy);
        this.bcSpec = new ECDomainParameters(curve, pointG, n);
        this.keyPairGenerator.init(new ECKeyGenerationParameters(bcSpec, new SecureRandom()));
    }

    public @Override String toString() {
        return new StringBuilder()
                   .append(Numbers.toHex(p)).append(SEPARATOR)
                   .append(Numbers.toHex(a)).append(SEPARATOR)
                   .append(Numbers.toHex(b)).append(SEPARATOR)
                   .append(Numbers.toHex(n)).append(SEPARATOR)
                   .append(Numbers.toHex(gx)).append(SEPARATOR)
                   .append(Numbers.toHex(gy)).toString();
    }

    public static ECParameter fromString(String parameter) {
        String[] array = parameter.split(String.valueOf(SEPARATOR), 6);
        return new ECParameter(new BigInteger(array[0], 16), 
                               new BigInteger(array[1], 16), 
                               new BigInteger(array[2], 16), 
                               new BigInteger(array[3], 16), 
                               new BigInteger(array[4], 16), 
                               new BigInteger(array[5], 16));
    }

    public static void main(String[] args) {
        String s = DEFAULT_EC_PARAM.toString();
        System.out.println(s);
        System.out.println(fromString(s));
    }
}
