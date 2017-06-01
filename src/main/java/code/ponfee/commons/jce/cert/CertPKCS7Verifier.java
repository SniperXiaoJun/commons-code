package code.ponfee.commons.jce.cert;

import java.io.IOException;
import java.security.SignatureException;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;

import sun.security.pkcs.ContentInfo;
import sun.security.pkcs.PKCS7;
import sun.security.pkcs.SignerInfo;

/**
 * p7验签
 * @author fupf
 */
@SuppressWarnings("restriction")
public class CertPKCS7Verifier extends CertSignedVerifier {

    private PKCS7 pkcs7;

    /**
     * 不附原文的多人pkcs7签名
     * @param rootCert
     * @param crl
     * @param p7sBytes
     * @param info
     */
    public CertPKCS7Verifier(X509Certificate rootCert, X509CRL crl, byte[] p7sBytes, byte[] info) {
        this(rootCert, crl, p7sBytes);
        this.info = info;
    }

    /**
     * 附原文的多人pkcs7签名
     * @param rootCert
     * @param crl
     * @param pkcs7Data
     */
    public CertPKCS7Verifier(X509Certificate rootCert, X509CRL crl, byte[] pkcs7Data) {
        super(rootCert, crl);
        try {
            this.info = getPkcs7Content(pkcs7Data);
            this.pkcs7 = new PKCS7(pkcs7Data);
            SignerInfo[] signs = pkcs7.getSignerInfos();
            X509Certificate[] certs = pkcs7.getCertificates();
            subjects = new X509Certificate[signs.length];
            int i = 0;
            for (SignerInfo sign : signs) {
                for (X509Certificate cert : certs) {
                    if (cert.getSerialNumber().equals(sign.getCertificateSerialNumber())) {
                        subjects[i++] = cert;
                        signedInfos.add(sign.getEncryptedDigest());
                    }
                }
            }
        } catch (IOException e) {
            throw new SecurityException("pkcs7获取原文失败", e);
        }
    }

    @Override
    public void verifySigned() {
        String cn = null;
        try {
            for (SignerInfo signer : pkcs7.getSignerInfos()) {
                cn = X509CertUtils.getCertInfo(signer.getCertificate(pkcs7), X509CertInfo.SUBJECT_CN);
                if (pkcs7.verify(signer, this.info) == null) {
                    throw new SecurityException("[" + cn + "]验签不通过");
                }
            }
        } catch (SecurityException e) {
            throw e;
        } catch (SignatureException e) {
            throw new SecurityException("[" + cn + "]签名信息错误", e);
        } catch (IOException e) {
            throw new SecurityException("获取证书主题异常", e);
        } catch (Exception e) {
            throw new SecurityException("证书验签出错", e);
        }
    }

    /**
     * 获取pkcs7原文
     * @param pkcs7
     * @return
     */
    private static byte[] getPkcs7Content(byte[] pkcs7) {
        try {
            PKCS7 p7 = new PKCS7(pkcs7);
            ContentInfo contentInfo = p7.getContentInfo();
            byte[] data;
            if (contentInfo.getContent() == null) {
                data = contentInfo.getData();
            } else {
                try {
                    data = contentInfo.getContent().getOctetString();
                } catch (Exception e) {
                    data = contentInfo.getContent().getDataBytes();
                }
            }
            return data;
        } catch (IOException e) {
            throw new SecurityException(e);
        }
    }

}
