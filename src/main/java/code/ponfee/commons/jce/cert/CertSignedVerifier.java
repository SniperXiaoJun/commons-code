package code.ponfee.commons.jce.cert;

import java.io.IOException;
import java.security.SignatureException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 证书验签（template method patterns）
 * @author fupf
 */
public abstract class CertSignedVerifier {
    protected final X509Certificate rootCert; // 根证书
    protected final X509CRL crl; // 吊销列表
    protected X509Certificate[] subjects; // 多人签名证书
    protected byte[] info; // 原文信息
    protected List<byte[]> signedInfos = new ArrayList<>(); // 签名数据

    private boolean verifySigned = true;

    protected CertSignedVerifier(X509Certificate rootCert, X509CRL crl) {
        this.rootCert = rootCert;
        this.crl = crl;
    }

    /**
     * 根据加载的根证进行证书验证
     * @throws CertVerifyException
     */
    public final void verify() {
        try {
            for (int i = 0; i < this.subjects.length; i++) {
                String subjectCN = X509CertUtils.getCertInfo(subjects[i], X509CertInfo.SUBJECT_CN);

                // 获取根证书
                if (rootCert == null) {
                    throw new SecurityException("[" + subjectCN + "]的根证未受信任");
                }

                // 校验
                verifyCertDate(this.subjects[i]);
                verifyIssuingSign(this.subjects[i], rootCert);
                if (crl != null) {
                    verifyCrlRevoke(this.subjects[i], crl);
                }
            }

            // 签名验证
            if (verifySigned) verifySigned();
        } catch (IOException e) {
            throw new SecurityException("获取证书主题异常", e);
        }
    }

    /**
     * 验证签名
     */
    public abstract void verifySigned();

    /**
     * 校验证书是否过期
     * @param subject
     * @throws CertVerifyException
     */
    public static void verifyCertDate(X509Certificate subject) {
        String subjectCN = null;
        try {
            subjectCN = X509CertUtils.getCertInfo(subject, X509CertInfo.SUBJECT_CN);
            subject.checkValidity(new Date());
        } catch (CertificateExpiredException e) {
            throw new SecurityException("[" + subjectCN + "]已过期");
        } catch (CertificateNotYetValidException e) {
            throw new SecurityException("[" + subjectCN + "]尚未生效");
        } catch (IOException e) {
            throw new SecurityException("获取证书主题异常", e);
        }
    }

    /**
     * 校验是否由指定根证签发
     * @param root
     * @param subject
     * @throws CertVerifyException
     */
    public static void verifyIssuingSign(X509Certificate subject, X509Certificate root) {
        String subjectCN = null;
        try {
            subjectCN = X509CertUtils.getCertInfo(subject, X509CertInfo.SUBJECT_CN);
            subject.verify(root.getPublicKey());
        } catch (SignatureException e) {
            throw new SecurityException("[" + subjectCN + "]的根证未受信任");
        } catch (IOException e) {
            throw new SecurityException("获取证书主题异常", e);
        } catch (Exception e) {
            throw new SecurityException("根证验签出错", e);
        }

    }

    /**
     * 校验是否已被吊销
     * @param crl
     * @param subject
     * @throws CertVerifyException
     */
    public static void verifyCrlRevoke(X509Certificate subject, X509CRL crl) {
        try {
            String subjectCN = X509CertUtils.getCertInfo(subject, X509CertInfo.SUBJECT_CN);
            if (crl.isRevoked(subject)) throw new SecurityException("[" + subjectCN + "]已被吊销");
        } catch (IOException e) {
            throw new SecurityException("获取证书主题异常", e);
        }
    }

    public X509Certificate[] getSubjects() {
        return this.subjects;
    }

    public byte[] getInfo() {
        return this.info;
    }

    public List<byte[]> getSignedInfo() {
        return this.signedInfos;
    }

    public void setVerifySigned(boolean verifySigned) {
        this.verifySigned = verifySigned;
    }

}
