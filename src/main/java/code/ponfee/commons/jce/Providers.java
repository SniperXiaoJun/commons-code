package code.ponfee.commons.jce;

import java.security.Provider;
import java.security.Security;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * security provider
 * @author fupf
 */
@SuppressWarnings("restriction")
@FunctionalInterface
public interface Providers {

    Provider get();

    static Provider get(Class<? extends Provider> type) {
        Provider provider = ProvidersHolder.HOLDER.get(type);
        if (provider != null) {
            return NullProvider.INSTANCE.equals(provider) ? null : provider;
        }

        try {
            provider = type.getDeclaredConstructor().newInstance();
            Security.addProvider(provider);
        } catch (Exception ignored) {
            provider = NullProvider.INSTANCE;
            ignored.printStackTrace();
        }
        ProvidersHolder.HOLDER.put(type, provider);
        return provider;
    }

    // BouncyCastleProvider.PROVIDER_NAME
    Providers BC =         () -> get(org.bouncycastle.jce.provider.BouncyCastleProvider.class);
    Providers SUN =        () -> get(sun.security.provider.Sun.class);
    Providers SunRsaSign = () -> get(sun.security.rsa.SunRsaSign.class);
    Providers SunEC =      () -> get(sun.security.ec.SunEC.class);
    Providers SunJSSE =    () -> get(com.sun.net.ssl.internal.ssl.Provider.class);
    Providers SunJCE =     () -> get(com.sun.crypto.provider.SunJCE.class);
    Providers SunJGSS =    () -> get(sun.security.jgss.SunProvider.class);
    Providers SunSASL =    () -> get(com.sun.security.sasl.Provider.class);
    Providers XMLDSig =    () -> get(org.jcp.xml.dsig.internal.dom.XMLDSigRI.class);
    Providers SunPCSC =    () -> get(sun.security.smartcardio.SunPCSC.class);
    Providers SunMSCAPI =  () -> get(sun.security.mscapi.SunMSCAPI.class);

    /**
     * provider holder
     */
    static final class ProvidersHolder {
        private static final Map<Class<? extends Provider>, Provider> HOLDER = new ConcurrentHashMap<>(16);
        static {
            Provider[] providers = Security.getProviders();
            if (providers != null && providers.length > 0) {
                for (Provider provider : providers) {
                    HOLDER.put(provider.getClass(), provider);
                }
            }
        }
    }

    /**
     * The NullProvider representing the not exists provider
     */
    static final class NullProvider extends Provider {
        private static final long serialVersionUID = 7420890884380155994L;
        private static final Provider INSTANCE = new NullProvider();

        private NullProvider() {
            super("Null", 1.0D, "null");
        }
    }

    public static void main(String[] args) {
        BC.get();
        BC.get();
    }
}
