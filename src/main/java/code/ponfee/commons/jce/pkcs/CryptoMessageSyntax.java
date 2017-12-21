package code.ponfee.commons.jce.pkcs;

import static code.ponfee.commons.jce.Providers.BC;

import java.io.IOException;
import java.security.PrivateKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.CMSEnvelopedData;
import org.bouncycastle.cms.CMSEnvelopedDataGenerator;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.CMSTypedData;
import org.bouncycastle.cms.RecipientInformation;
import org.bouncycastle.cms.RecipientInformationStore;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder;
import org.bouncycastle.cms.jcajce.JceCMSContentEncryptorBuilder;
import org.bouncycastle.cms.jcajce.JceKeyTransEnvelopedRecipient;
import org.bouncycastle.cms.jcajce.JceKeyTransRecipientInfoGenerator;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.DigestCalculatorProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.bouncycastle.util.Store;;

/**
 * 加密消息语法：Cryptography Message Syntax
 * @author fupf
 */
public final class CryptoMessageSyntax {

    // -----------------------------------------sign/verify----------------------------------
    /**
     * 附原文签名（单人）
     * @param data
     * @param key
     * @param certChain
     * @return
     */
    public static byte[] sign(byte[] data, PrivateKey key, X509Certificate[] certChain) {
        return sign(data, Arrays.asList(key), Arrays.asList(new X509Certificate[][] { certChain }));
    }

    /**
     * 附原文签名（多人）
     * @param data
     * @param keys
     * @param certs  证书链（多人list）
     * @return
     */
    public static byte[] sign(byte[] data, List<PrivateKey> keys, List<X509Certificate[]> certs) {
        try {
            CMSSignedDataGenerator gen = new CMSSignedDataGenerator();
            DigestCalculatorProvider dcp = new JcaDigestCalculatorProviderBuilder().setProvider(BC).build();
            for (int i = 0; i < keys.size(); i++) {
                gen.addCertificates(new JcaCertStore(Arrays.asList(certs.get(i))));

                ContentSigner signer = new JcaContentSignerBuilder(certs.get(i)[0].getSigAlgName())
                                                         .setProvider(BC).build(keys.get(i));
                JcaSignerInfoGeneratorBuilder jsBuilder = new JcaSignerInfoGeneratorBuilder(dcp);
                gen.addSignerInfoGenerator(jsBuilder.build(signer, certs.get(i)[0]));
            }
            return gen.generate(new CMSProcessableByteArray(data), true).getEncoded(); // true附原文
        } catch (OperatorCreationException | CertificateEncodingException | CMSException | IOException e) {
            throw new SecurityException(e);
        }
    }

    /**
     * 验签（附原文）
     * @param signed
     * @return
     */
    public static void verify(byte[] signed) {
        try {
            CMSSignedData sign = new CMSSignedData(signed); // 构建PKCS#7签名数据处理对象
            Store<?> store = sign.getCertificates();
            for (Iterator<?> iter = sign.getSignerInfos().getSigners().iterator(); iter.hasNext();) {
                SignerInformation signer = (SignerInformation) iter.next();
                @SuppressWarnings("unchecked") Collection<?> certChain = store.getMatches(signer.getSID()); // 证书链
                X509CertificateHolder cert = (X509CertificateHolder) certChain.iterator().next();
                if (!signer.verify(new JcaSimpleSignerInfoVerifierBuilder().setProvider(BC).build(cert))) {
                    String certSN = cert.getSerialNumber().toString(16);
                    String certDN = cert.getSubject().toString();
                    throw new SecurityException("signature verify fail[" + certSN + ", " + certDN + "]");
                }
            }
        } catch (OperatorCreationException | CertificateException | CMSException e) {
            throw new SecurityException(e);
        }
    }

    // -----------------------------------------envelop/unenvelop----------------------------------
    /**
     * 构造数字信封
     * PKCSObjectIdentifiers.des_EDE3_CBC
     * PKCSObjectIdentifiers.RC2_CBC
     * new ASN1ObjectIdentifier("1.2.840.113549.3.2"); // RSA_RC2
     * new ASN1ObjectIdentifier("1.2.840.113549.3.4"); // RSA_RC4
     * new ASN1ObjectIdentifier("1.3.14.3.2.7"); // DES_CBC
     * new ASN1ObjectIdentifier("1.2.840.113549.3.7"); // DESede_CBC
     * @param data
     * @param cert
     * @param alg
     * @return
     */
    public static byte[] envelop(byte[] data, X509Certificate cert, ASN1ObjectIdentifier alg) {
        try {
            //添加数字信封
            CMSTypedData msg = new CMSProcessableByteArray(data);
            CMSEnvelopedDataGenerator edGen = new CMSEnvelopedDataGenerator();
            edGen.addRecipientInfoGenerator(new JceKeyTransRecipientInfoGenerator(cert).setProvider(BC));
            CMSEnvelopedData ed = edGen.generate(msg, new JceCMSContentEncryptorBuilder(alg).setProvider(BC).build());
            return ed.getEncoded();
        } catch (CertificateEncodingException | CMSException | IOException e) {
            throw new SecurityException(e);
        }
    }

    /**
     * 解数字信封
     * @param enveloped
     * @param privateKey
     * @return
     */
    public static byte[] unenvelop(byte[] enveloped, PrivateKey privateKey) {
        try {
            // 获取密文
            RecipientInformationStore recipients = new CMSEnvelopedData(enveloped).getRecipientInfos();
            Iterator<?> iter = recipients.getRecipients().iterator();
            // 解密
            if (iter.hasNext()) {
                RecipientInformation recipient = (RecipientInformation) iter.next();
                return recipient.getContent(new JceKeyTransEnvelopedRecipient(privateKey).setProvider(BC));
            } else {
                return null;
            }
        } catch (CMSException e) {
            throw new SecurityException(e);
        }
    }

}
