package code.ponfee.commons.jce.security;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

/**
 * 密钥库解析类
 * @author fupf
 */
public class KeyStoreResolver {

    public static enum KeyStoreType {
        JKS, PKCS12;
    }

    private final KeyStore keyStore;

    public KeyStoreResolver(KeyStoreType type) {
        this(type, null);
    }

    public KeyStoreResolver(KeyStoreType type, String storePassword) {
        this(type, (InputStream) null, storePassword);
    }

    public KeyStoreResolver(KeyStoreType type, byte[] keyStore, String storePassword) {
        this(type, new ByteArrayInputStream(keyStore), storePassword);
    }

    /**
     * 创建密钥库
     * @param type           密钥库类型
     * @param input          密钥库输入流数据
     * @param storePassword  用于解锁密钥库
     */
    public KeyStoreResolver(KeyStoreType type, InputStream input, String storePassword) {
        try {
            this.keyStore = KeyStore.getInstance(type.name());
            this.keyStore.load(input, toCharArray(storePassword));
        } catch (Exception e) {
            throw new SecurityException(e);
        } finally {
            if (input != null) try {
                input.close();
            } catch (IOException ignored) {
                ignored.printStackTrace();
            }
        }
    }

    /**
     * 添加证书
     * @param alias    别名
     * @param cert     证书
     */
    public void setCertificateEntry(String alias, Certificate cert) {
        try {
            checkAliasNotExists(alias);
            this.keyStore.setCertificateEntry(alias, cert);
        } catch (KeyStoreException e) {
            throw new SecurityException(e);
        }
    }

    /**
     * 设置私钥
     * @param alias          别名
     * @param key            私钥
     * @param keyPassword    私钥加锁密码
     * @param chain
     */
    public final void setKeyEntry(String alias, PrivateKey key, String keyPassword, Certificate[] chain) {
        try {
            checkAliasNotExists(alias);
            this.keyStore.setKeyEntry(alias, key, keyPassword.toCharArray(), chain);
        } catch (KeyStoreException e) {
            throw new SecurityException(e);
        }
    }

    /*public final void setKeyEntry(String alias, byte[] key, Certificate[] chain) {
        try {
            checkAliasNotExists(alias);
            this.keyStore.setKeyEntry(alias, key, chain);
        } catch (KeyStoreException e) {
            throw new SecurityException(e);
        }
    }*/

    public byte[] export(String storePassword) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        export(out, storePassword);
        return out.toByteArray();
    }

    /**
     * 导出密钥库
     * @param out             目标输出流
     * @param storePassword   设置要导出密钥库的密码
     */
    public void export(OutputStream out, String storePassword) {
        try {
            keyStore.store(out, toCharArray(storePassword));
            out.flush();
        } catch (Exception e) {
            throw new SecurityException(e);
        }
    }

    /**
     * 枚举密钥库条目
     * @return
     */
    public List<String> listAlias() {
        try {
            List<String> alias = new ArrayList<String>();
            Enumeration<String> e = keyStore.aliases();
            while (e.hasMoreElements()) {
                alias.add(e.nextElement());
            }
            return alias;
        } catch (KeyStoreException e) {
            throw new SecurityException(e);
        }
    }

    public void delAlias(String alias) {
        try {
            if (keyStore.containsAlias(alias)) {
                keyStore.deleteEntry(alias);// 删除别名对应的条目
            }
        } catch (KeyStoreException e) {
            throw new SecurityException(e);
        }
    }

    public PublicKey getPublicKey() {
        try {
            return getPublicKey(keyStore.aliases().nextElement());
        } catch (KeyStoreException e) {
            throw new SecurityException(e);
        }
    }

    /**
     * 获取公钥
     * @param alias
     * @return
     */
    public PublicKey getPublicKey(String alias) {
        try {
            /*if (!keyStore.isCertificateEntry(alias)) {
                throw new SecurityException("alias[" + alias + "] is not certificate entry.");
            }*/
            Certificate cert = keyStore.getCertificate(alias);
            return cert.getPublicKey();
        } catch (KeyStoreException e) {
            throw new SecurityException(e);
        }
    }

    public PrivateKey getPrivateKey(String keyPassword) {
        try {
            return getPrivateKey(keyStore.aliases().nextElement(), keyPassword);
        } catch (KeyStoreException e) {
            throw new SecurityException(e);
        }
    }

    /**
     * 获取私钥
     * @param alias         别名
     * @param keyPassword   the password for recovering the PrivateKey
     * @return
     */
    public PrivateKey getPrivateKey(String alias, String keyPassword) {
        try {
            if (!keyStore.isKeyEntry(alias)) {
                throw new SecurityException("alias[" + alias + "] is not key entry.");
            }
            return (PrivateKey) keyStore.getKey(alias, toCharArray(keyPassword));
        } catch (UnrecoverableKeyException e) {
            throw new SecurityException("invalid key password: " + keyPassword, e);
        } catch (KeyStoreException | NoSuchAlgorithmException e) {
            throw new SecurityException(e);
        }
    }

    public X509Certificate[] getX509CertChain() {
        try {
            return getX509CertChain(keyStore.aliases().nextElement());
        } catch (KeyStoreException e) {
            throw new SecurityException(e);
        }
    }

    /**
     * 获取证书链
     * @param alias
     * @return
     */
    public X509Certificate[] getX509CertChain(String alias) {
        try {
            Certificate[] certs = keyStore.getCertificateChain(alias);
            X509Certificate[] x509Certchain = new X509Certificate[certs.length];
            for (int i = 0; i < certs.length; i++) {
                if (!(certs[i] instanceof X509Certificate)) {
                    throw new SecurityException("certificate[" + i + "] in chain '" + alias + "' is not a X509Certificate.");
                }
                x509Certchain[i] = (X509Certificate) certs[i];
            }
            return x509Certchain;
        } catch (KeyStoreException e) {
            throw new SecurityException(e);
        }
    }

    public SSLContext getSSLContext(String keyPassword) {
        return this.getSSLContext(keyPassword, null);
    }

    /**
     * 获取SSLContext
     * @param keyPassword   the password for recovering the PrivateKey
     * @param trustStore    受信任的证书库
     * @return
     */
    public SSLContext getSSLContext(String keyPassword, KeyStore trustStore) {
        String algorithm = "SunX509";
        try {
            TrustManager[] trusts = null;
            if (trustStore != null) {
                TrustManagerFactory tmf = TrustManagerFactory.getInstance(algorithm);
                tmf.init(trustStore);
                trusts = tmf.getTrustManagers();
            }

            KeyManagerFactory kmf = KeyManagerFactory.getInstance(algorithm);
            kmf.init(this.keyStore, toCharArray(keyPassword));

            SSLContext context = SSLContext.getInstance("TLS");
            context.init(kmf.getKeyManagers(), trusts, new SecureRandom());
            return context;
        } catch (Exception e) {
            throw new SecurityException(e);
        }
    }

    public KeyStore getKeyStore() {
        return keyStore;
    }

    private char[] toCharArray(String str) {
        if (null == str || str.length() == 0) return null;
        else return str.toCharArray();
    }

    private void checkAliasNotExists(String alias) throws KeyStoreException {
        if (keyStore.containsAlias(alias)) {
            throw new SecurityException("alias[" + alias + "] is exists.");
        }
    }

}
