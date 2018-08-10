package code.ponfee.commons.jce;

import java.security.Provider;
import java.security.Security;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * security providers
 * there has not any method defined except a static method
 * @author fupf
 */
@SuppressWarnings("restriction")
public interface Providers {

    static Provider get(Class<? extends Provider> type) {
        Provider provider = ProvidersHolder.HOLDER.get(type);
        if (provider != null) {
            return provider;
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
    Provider BC =         Providers.get(org.bouncycastle.jce.provider.BouncyCastleProvider.class);
    Provider BC_PQC =     Providers.get(org.bouncycastle.pqc.jcajce.provider.BouncyCastlePQCProvider.class);
    Provider BC_JSSE =    Providers.get(org.bouncycastle.jsse.provider.BouncyCastleJsseProvider.class);
    Provider SUN =        Providers.get(sun.security.provider.Sun.class);
    Provider SunRsaSign = Providers.get(sun.security.rsa.SunRsaSign.class);
    Provider SunEC =      Providers.get(sun.security.ec.SunEC.class);
    Provider SunJSSE =    Providers.get(com.sun.net.ssl.internal.ssl.Provider.class);
    Provider SunJCE =     Providers.get(com.sun.crypto.provider.SunJCE.class);
    Provider SunJGSS =    Providers.get(sun.security.jgss.SunProvider.class);
    Provider SunSASL =    Providers.get(com.sun.security.sasl.Provider.class);
    Provider XMLDSig =    Providers.get(org.jcp.xml.dsig.internal.dom.XMLDSigRI.class);
    Provider SunPCSC =    Providers.get(sun.security.smartcardio.SunPCSC.class);
    Provider SunMSCAPI =  Providers.get(sun.security.mscapi.SunMSCAPI.class);

    /**
     * provider holder
     */
    final class ProvidersHolder {
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
    final class NullProvider extends Provider {
        private static final long serialVersionUID = 7420890884380155994L;
        private static final NullProvider INSTANCE = new NullProvider();

        private NullProvider() {
            super("Null", 1.0D, "None provider");
        }
    }

}
