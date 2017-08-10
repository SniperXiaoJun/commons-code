package code.ponfee.commons.jce;

import java.security.Provider;
import java.security.Security;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * provider getter
 * @author fupf
 */
@SuppressWarnings("restriction")
@FunctionalInterface
public interface Providers {

    Provider get();

    static Provider get(Class<? extends Provider> type) {
        Provider provider = ProvidersHolder.HOLDER.get(type);
        if (provider != null) {
            return provider;
        } else if (ProvidersHolder.HOLDER.containsKey(type)) {
            return null;
        }

        try {
            provider = type.getDeclaredConstructor().newInstance();
            Security.addProvider(provider);
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }
        ProvidersHolder.HOLDER.put(type, provider);
        return provider;
    }

    Providers BC =           () -> get(org.bouncycastle.jce.provider.BouncyCastleProvider.class);
    Providers SUN_RSA_SIGN = () -> get(sun.security.rsa.SunRsaSign.class);
    Providers SUN_JCE =      () -> get(com.sun.crypto.provider.SunJCE.class);

    static final class ProvidersHolder {
        private static final Map<Class<? extends Provider>, Provider> HOLDER = new ConcurrentHashMap<>();
        static {
            Provider[] providers = Security.getProviders();
            if (providers != null && providers.length > 0) {
                for (Provider provider : providers) {
                    HOLDER.put(provider.getClass(), provider);
                }
            }
        }
    }

    public static void main(String[] args) {
        SUN_RSA_SIGN.get();
        SUN_RSA_SIGN.get();
        SUN_JCE.get();
        SUN_JCE.get();
        BC.get();
        BC.get();
    }
}
