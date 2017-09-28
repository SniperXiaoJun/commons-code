package code.ponfee.commons.jce.pkcs;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.cert.X509Certificate;

import sun.security.pkcs.ContentInfo;
import sun.security.pkcs.PKCS7;
import sun.security.pkcs.SignerInfo;
import sun.security.util.DerValue;
import sun.security.x509.AlgorithmId;
import sun.security.x509.X500Name;

/**
 * pkcs7工具类
 * @author fupf
 */
@SuppressWarnings("restriction")
public class PKCS7Signature {

    /*private static final Map<String, String> HASH_SIGN_ALG = new HashMap<>() {
        private static final long serialVersionUID = 8252202658190109593L;
        {
            put("1.2.840.113549.1.1.4", "MD5");
            put("1.2.840.113549.1.1.5", "SHA-1");
            put("1.2.840.113549.1.1.11", "SHA-256");
            put("1.2.840.113549.1.1.12", "SHA-384");
            put("1.2.840.113549.1.1.13", "SHA-512");
        }
    };*/

    /**
     * byte流数据签名（单人）
     * @param privKey
     * @param cert
     * @param data 是否附原文
     * @param attach
     * @return
     */
    public static byte[] sign(PrivateKey privKey, X509Certificate cert, byte[] data, boolean attach) {
        return sign(new PrivateKey[] { privKey }, new X509Certificate[] { cert }, data, attach);
    }

    /**
     * byte流数据签名（多人）
     * @param privKeys
     * @param certs
     * @param data
     * @param attach
     * @return
     */
    public static byte[] sign(PrivateKey[] privKeys, X509Certificate[] certs, byte[] data, boolean attach) {
        ContentInfo contentInfo = null;
        if (attach) contentInfo = new ContentInfo(data);
        else contentInfo = new ContentInfo(ContentInfo.DATA_OID, null);
        return sign(contentInfo, data, certs, privKeys);
    }

    /**
     * 文本签名（单人）
     * @param privKey
     * @param cert
     * @param data
     * @param attach 是否附原文
     * @return
     */
    public static byte[] sign(PrivateKey privKey, X509Certificate cert, String data, boolean attach) {
        return sign(new PrivateKey[] { privKey }, new X509Certificate[] { cert }, data, attach);
    }

    /**
     * 文本签名（多人）
     * @param privKeys
     * @param certs
     * @param data
     * @param attach
     * @return
     */
    public static byte[] sign(PrivateKey[] privKeys, X509Certificate[] certs, String data, boolean attach) {
        try {
            DerValue dv = null;
            if (attach) dv = new DerValue(data);
            ContentInfo contentInfo = new ContentInfo(ContentInfo.DATA_OID, dv);
            return sign(contentInfo, data.getBytes(), certs, privKeys);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 附原文的验签（P7方式验签，可验证CMS格式签名）
     * @param pkcs7
     * @return 返回原文
     */
    public static byte[] verify(byte[] pkcs7) {
        try {
            ContentInfo contentInfo = new PKCS7(pkcs7).getContentInfo();
            byte[] data = null;
            if (contentInfo.getContent() == null) {
                data = contentInfo.getData();
            } else {
                try {
                    data = contentInfo.getContent().getOctetString();
                } catch (Exception e) {
                    data = contentInfo.getContent().getDataBytes();
                }
            }
            verify(pkcs7, data);
            return data;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 不附原文的验签（P7方式验签，可验证CMS格式签名）
     * @param pkcs7Data
     * @param data
     * @return
     */
    public static void verify(byte[] pkcs7Data, byte[] data) {
        if (data == null || data.length == 0) {
            throw new SecurityException("待验签的原数据为空！");
        }

        try {
            PKCS7 pkcs7 = new PKCS7(pkcs7Data);
            for (SignerInfo signed : pkcs7.getSignerInfos()) {
                if (pkcs7.verify(signed, data) == null) {
                    String certSN = signed.getCertificateSerialNumber().toString(16);
                    String certDN = signed.getCertificate(pkcs7).getSubjectX500Principal().getName();
                    //new X509Principal(signed.getCertificate(pkcs7).getSubjectX500Principal().getEncoded()).getName()
                    throw new SecurityException("验签失败[certSN：" + certSN + "；CertDN：" + certDN + "]");
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 签名方法体
     * @param contentInfo
     * @param certs
     * @param keys
     * @return
     */
    private static byte[] sign(ContentInfo contentInfo, byte[] data, X509Certificate[] certs, PrivateKey[] keys) {
        SignerInfo[] signs = new SignerInfo[keys.length];
        AlgorithmId[] digestAlgorithmIds = new AlgorithmId[keys.length];
        for (int i = 0; i < keys.length; i++) {
            X509Certificate cert = certs[i];
            PrivateKey privKey = keys[i];
            try {
                /*AlgorithmId digAlg = AlgorithmId.get(HASH_SIGN_ALG.get(cert.getSigAlgOID()));
                AlgorithmId encAlg = new AlgorithmId(AlgorithmId.RSAEncryption_oid);*/
                AlgorithmId digAlg = AlgorithmId.get(AlgorithmId.getDigAlgFromSigAlg(cert.getSigAlgName()));
                AlgorithmId encAlg = AlgorithmId.get(AlgorithmId.getEncAlgFromSigAlg(cert.getSigAlgName()));
                digestAlgorithmIds[i] = digAlg;
                X500Name name = new X500Name(cert.getIssuerX500Principal().getEncoded());

                Signature signer = Signature.getInstance(cert.getSigAlgName());
                signer.initSign(privKey);
                signer.update(data); // signer.update(data, 0, data.length);
                signs[i] = new SignerInfo(name, cert.getSerialNumber(), digAlg, encAlg, signer.sign());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        // 构造PKCS7数据
        PKCS7 pkcs7 = new PKCS7(digestAlgorithmIds, contentInfo, certs, signs);
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            pkcs7.encodeSignedData(out);
            out.flush();
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
