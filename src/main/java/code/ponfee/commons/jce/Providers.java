package code.ponfee.commons.jce;

import java.security.Provider;
import java.security.Security;

/**
 * provider holder
 * @author fupf
 */
@SuppressWarnings("restriction")
@FunctionalInterface
public interface Providers {

    Provider get();

    Providers BC = () -> {
        Provider bc = new org.bouncycastle.jce.provider.BouncyCastleProvider();
        Security.addProvider(bc);
        return bc;
    };

    Providers SUN_RSA_SIGN = () -> {
        Provider sunRsaSign = new sun.security.rsa.SunRsaSign();
        Security.addProvider(sunRsaSign);
        return sunRsaSign;
    };

    Providers SUN_JCE = () -> {
        Provider sunJCE = new com.sun.crypto.provider.SunJCE();
        Security.addProvider(sunJCE);
        return sunJCE;
    };
}
