package code.ponfee.commons.jce.cert;

import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;

/**
 * p1验签
 * @author fupf
 */
public class CertPKCS1Verifier extends CertSignedVerifier {

    public CertPKCS1Verifier(X509Certificate rootCert, X509CRL crl, 
                             X509Certificate subject, byte[] info, byte[] signedInfo) {
        super(rootCert, crl);
        try {
            this.subjects = new X509Certificate[] { subject };
            this.info = info;
            this.signedInfos.add(signedInfo);
        } catch (Exception e) {
            throw new SecurityException("证书数据格式错误", e);
        }
    }

    public @Override void verifySigned() {
        String subjectCN = null;
        try {
            subjectCN = X509CertUtils.getCertInfo(this.subjects[0], X509CertInfo.SUBJECT_CN);
            Signature sign = Signature.getInstance(this.subjects[0].getSigAlgName());
            sign.initVerify(this.subjects[0].getPublicKey());
            sign.update(this.info);

            if (!sign.verify(this.signedInfos.get(0))) {
                throw new SecurityException("[" + subjectCN + "]验签不通过");
            }
        } catch (SignatureException e) {
            throw new SecurityException("[" + subjectCN + "]证书签名信息错误", e);
        } catch (SecurityException e) {
            throw e;
        } catch (Exception e) {
            throw new SecurityException("证书验签出错", e);
        }
    }

}
