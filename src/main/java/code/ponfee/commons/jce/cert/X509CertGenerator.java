package code.ponfee.commons.jce.cert;

import java.io.IOException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.Signature;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Random;
import java.util.Vector;

import org.bouncycastle.jce.PKCS10CertificationRequest;

import code.ponfee.commons.jce.Providers;
import code.ponfee.commons.jce.RSASignAlgorithm;
import code.ponfee.commons.util.ObjectUtils;
import sun.security.pkcs10.PKCS10;
import sun.security.util.ObjectIdentifier;
import sun.security.x509.AlgorithmId;
import sun.security.x509.CertificateAlgorithmId;
import sun.security.x509.CertificateExtensions;
import sun.security.x509.CertificateIssuerName;
import sun.security.x509.CertificateSerialNumber;
import sun.security.x509.CertificateValidity;
import sun.security.x509.CertificateVersion;
import sun.security.x509.CertificateX509Key;
import sun.security.x509.ExtendedKeyUsageExtension;
import sun.security.x509.Extension;
import sun.security.x509.KeyUsageExtension;
import sun.security.x509.X500Name;
import sun.security.x509.X509CertImpl;
import sun.security.x509.X509CertInfo;

/**
 * 证书生成工具类
 * @author fupf
 */
@SuppressWarnings("restriction")
public class X509CertGenerator {

    private static final Random RANDOM = new Random();

    public static X509Certificate createRootCert(String issuer, RSASignAlgorithm sigAlg,
        PrivateKey privateKey, PublicKey publicKey, Date notBefore, Date notAfter) {
        return createRootCert(null, issuer, sigAlg, privateKey, publicKey, notBefore, notAfter);
    }

    /**
     * 创建CA根证书（自签名）
     * @param sn
     * @param issuer
     * @param sigAlg
     * @param privateKey
     * @param publicKey
     * @param notBefore
     * @param notAfter
     * @return
     */
    public static X509Certificate createRootCert(Integer sn, String issuer, RSASignAlgorithm sigAlg,
        PrivateKey privateKey, PublicKey publicKey, Date notBefore, Date notAfter) {
        PKCS10 pkcs10 = createPkcs10(issuer, privateKey, publicKey, sigAlg);
        X509CertInfo certInfo = createCertInfo(sn, pkcs10, notBefore, notAfter, createExtensions(true));
        return signSelf(privateKey, sigAlg, certInfo);
    }

    public static X509Certificate createSubjectCert(X509Certificate caCert, PrivateKey caKey, String subject,
        RSASignAlgorithm sigAlg, PrivateKey privateKey, PublicKey publicKey, Date notBefore, Date notAfter) {
        return createSubjectCert(caCert, caKey, null, subject, sigAlg, privateKey, publicKey, notBefore, notAfter);
    }

    /**
     * 创建证书并用根证签发
     * @param caCert
     * @param caKey
     * @param sn
     * @param subject
     * @param sigAlg
     * @param privateKey
     * @param publicKey
     * @param notBefore
     * @param notAfter
     * @return
     */
    public static X509Certificate createSubjectCert(X509Certificate caCert, PrivateKey caKey, Integer sn,
        String subject, RSASignAlgorithm sigAlg, PrivateKey privateKey, PublicKey publicKey, Date notBefore, Date notAfter) {
        PKCS10 pkcs10 = createPkcs10(subject, privateKey, publicKey, sigAlg);
        X509CertInfo certInfo = createCertInfo(sn, pkcs10, notBefore, notAfter, createExtensions(false));
        return signCert(caCert, caKey, certInfo);
    }

    public static X509Certificate createSubjectCert(X509Certificate caCert,
        PrivateKey caKey, PKCS10 pkcs10, Date notBefore, Date notAfter) {
        return createSubjectCert(caCert, caKey, null, pkcs10, notBefore, notAfter);
    }

    /**
     * pkcs10请求CA签发证书
     * @param caCert
     * @param caKey
     * @param sn
     * @param pkcs10
     * @param notBefore
     * @param notAfter
     * @return
     */
    public static X509Certificate createSubjectCert(X509Certificate caCert,
        PrivateKey caKey, Integer sn, PKCS10 pkcs10, Date notBefore, Date notAfter) {
        X509CertInfo certInfo = createCertInfo(sn, pkcs10, notBefore, notAfter, createExtensions(false));
        return signCert(caCert, caKey, certInfo);
    }

    /**
     * 创建pkcs10
     * @param subject
     * @param privateKey
     * @param publicKey
     * @param sigAlg
     * @return
     */
    public static PKCS10 createPkcs10(String subject, PrivateKey privateKey, PublicKey publicKey, RSASignAlgorithm sigAlg) {
        try {
            PKCS10 pkcs10 = new PKCS10(publicKey);
            Signature signature = Signature.getInstance(sigAlg.name());
            signature.initSign(privateKey);
            pkcs10.encodeAndSign(new X500Name(subject), signature);
            return pkcs10;
        } catch (Exception e) {
            throw new SecurityException(e);
        }
    }

    /**
     * 创建默认的扩展信息
     * @return
     */
    public static CertificateExtensions createExtensions(boolean isCA) {
        try {
            CertificateExtensions extensions = new CertificateExtensions();
            byte[] userData = null;

            // 密钥用法
            KeyUsageExtension keyUsage = new KeyUsageExtension();
            keyUsage.set(KeyUsageExtension.DIGITAL_SIGNATURE, true);
            if (isCA) {
                userData = "Digital Signature, Certificate Signing, Off-line CRL Signing, CRL Signing (86)".getBytes();

                keyUsage.set(KeyUsageExtension.KEY_CERTSIGN, true);
                keyUsage.set(KeyUsageExtension.CRL_SIGN, true);
            } else {
                userData = "Digital Signature, Data Encipherment (90)".getBytes();

                //keyUsage.set(KeyUsageExtension.KEY_ENCIPHERMENT, true);
                keyUsage.set(KeyUsageExtension.DATA_ENCIPHERMENT, true);

                // 增强密钥用法
                Vector<ObjectIdentifier> extendedKeyUsage = new Vector<>();
                extendedKeyUsage.add(new ObjectIdentifier(new int[] { 1, 3, 6, 1, 5, 5, 7, 3, 3 })); // 代码签名
                extensions.set(ExtendedKeyUsageExtension.NAME, new ExtendedKeyUsageExtension(extendedKeyUsage));
            }
            extensions.set(KeyUsageExtension.NAME, keyUsage);

            // 版本号：v1、v2、v3，此扩展信息必须是v3版本，生成一个extension对象参数分别为oid，是否关键扩展，byte[]型的内容值
            ObjectIdentifier oid = new ObjectIdentifier(new int[] { 1, 22 }); // 扩展域:第1位最大为2，第2位最大为39，后续不明
            userData = ObjectUtils.concat(new byte[] { 0x04, (byte) userData.length }, userData); // flag,data length, data
            extensions.set("UserData", new Extension(oid, true, userData));

            return extensions;
        } catch (IOException e) {
            throw new SecurityException(e);
        }
    }

    /**
     * 根据pkcs10创建证书
     * @param sn
     * @param pkcs10
     * @param notBefore
     * @param notAfter
     * @param extensions
     * @return
     */
    private static X509CertInfo createCertInfo(Integer sn, PKCS10 pkcs10,
        Date notBefore, Date notAfter, CertificateExtensions extensions) {
        if (sn == null) sn = RANDOM.nextInt() & Integer.MAX_VALUE;
        try {
            // 验证pkcs10
            Security.addProvider(Providers.BC.get());
            PKCS10CertificationRequest req = new PKCS10CertificationRequest(pkcs10.getEncoded());
            if (!req.verify()) throw new SecurityException("invalid pkcs10 data");

            AlgorithmId signAlg = AlgorithmId.get(req.getSignatureAlgorithm().getAlgorithm().getId());
            X509CertInfo x509certinfo = new X509CertInfo();
            x509certinfo.set(X509CertInfo.VERSION, new CertificateVersion(2));
            x509certinfo.set(X509CertInfo.SERIAL_NUMBER, new CertificateSerialNumber(sn));
            x509certinfo.set(X509CertInfo.ALGORITHM_ID, new CertificateAlgorithmId(signAlg));
            x509certinfo.set(X509CertInfo.SUBJECT, pkcs10.getSubjectName());
            x509certinfo.set(X509CertInfo.KEY, new CertificateX509Key(pkcs10.getSubjectPublicKeyInfo()));
            x509certinfo.set(X509CertInfo.VALIDITY, new CertificateValidity(notBefore, notAfter));
            if (extensions != null) {
                x509certinfo.set(X509CertInfo.EXTENSIONS, extensions);
            }
            return x509certinfo;
        } catch (Exception e) {
            throw new SecurityException(e);
        }
    }

    /**
     * 自签名证书（根证书）
     * @param privateKey
     * @param sigAlg
     * @param certInfo
     * @return
     */
    private static X509Certificate signSelf(PrivateKey privateKey, RSASignAlgorithm sigAlg, X509CertInfo certInfo) {
        try {
            certInfo.set(X509CertInfo.ISSUER, certInfo.get(X509CertInfo.SUBJECT));
            X509CertImpl signedCert = new X509CertImpl(certInfo);
            signedCert.sign(privateKey, sigAlg.name()); // 签名
            return signedCert;
        } catch (Exception e) {
            throw new SecurityException(e);
        }
    }

    /**
     * 签名证书
     * @param caCert
     * @param caKey
     * @param certInfo
     * @return
     */
    private static X509Certificate signCert(X509Certificate caCert, PrivateKey caKey, X509CertInfo certInfo) {
        try {
            // 从CA的证书中提取签发者的信息
            X509CertImpl caCertImpl = new X509CertImpl(caCert.getEncoded()); // 用该编码创建X509CertImpl类型对象
            X509CertInfo caCertInfo = (X509CertInfo) caCertImpl.get(X509CertImpl.NAME + "." + X509CertImpl.INFO); // 获取X509CertInfo对象
            X500Name issuer = (X500Name) caCertInfo.get(X509CertInfo.SUBJECT + "." + CertificateIssuerName.DN_NAME); // 获取X509Name类型的签发者信息

            certInfo.set(X509CertInfo.ISSUER, issuer);
            X509CertImpl signedCert = new X509CertImpl(certInfo);
            signedCert.sign(caKey, caCert.getSigAlgName()); // 使用CA私钥对其签名
            return signedCert;
        } catch (Exception e) {
            throw new SecurityException(e);
        }
    }

    public static void main(String[] args) {
        String userData = "Digital Signature, Non-Repudiation, Key Encipherment, Data Encipherment (f0)";
        byte[] b1 = userData.getBytes();
        byte[] b2 = new byte[userData.length()];
        for (int i = 0; i < b2.length; i++) {
            b2[i] = (byte) userData.charAt(i);
        }
        System.out.println(ObjectUtils.equals(b1, b2));
        b2[15] = 1;
        System.out.println(ObjectUtils.equals(b1, b2));
    }

}
