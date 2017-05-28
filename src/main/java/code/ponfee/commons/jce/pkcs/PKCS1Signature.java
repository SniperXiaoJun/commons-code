package code.ponfee.commons.jce.pkcs;

import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.cert.X509Certificate;

/**
 * P1签名验签工具类
 * @author fupf
 */
public class PKCS1Signature {

    /**
     * 签名
     * @param data
     * @param privateKey
     * @param cert
     * @return
     */
    public static byte[] sign(byte[] data, PrivateKey privateKey, X509Certificate cert) {
        try {
            Signature signature = Signature.getInstance(cert.getSigAlgName());
            signature.initSign(privateKey);
            signature.update(data);
            return signature.sign();
        } catch (GeneralSecurityException e) {
            throw new SecurityException(e);
        }
    }

    /**
     * 验签
     * @param cert
     * @param data
     * @param signed
     * @return
     */
    public static boolean verify(X509Certificate cert, byte[] data, byte[] signed) {
        try {
            Signature sign = Signature.getInstance(cert.getSigAlgName());
            sign.initVerify(cert.getPublicKey());
            sign.update(data);
            return sign.verify(signed);
        } catch (GeneralSecurityException e) {
            throw new SecurityException(e);
        }
    }

}
