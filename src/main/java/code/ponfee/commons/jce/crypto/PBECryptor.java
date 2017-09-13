package code.ponfee.commons.jce.crypto;

import java.security.GeneralSecurityException;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

import org.apache.commons.text.RandomStringGenerator;

import com.sun.crypto.provider.SunJCE;

import code.ponfee.commons.util.SecureRandoms;

/**
 * <pre>
 *  |---------------------------------------|-------------------|---------------------------|
 *  | Algorithm                             | secret key length | default secret key length |
 *  |---------------------------------------|-------------------|---------------------------|
 *  | PBEWithMD5AndDES(Bad Algorithm)       |        56         |            56             |
 *  |---------------------------------------|-------------------|---------------------------|
 *  | PBEWithMD5AndTripleDES(Bad Algorithm) |      112,168      |            168            |
 *  |---------------------------------------|-------------------|---------------------------|
 *  | PBEWithSHA1AndDESede                  |      112,168      |            168            |
 *  |---------------------------------------|-------------------|---------------------------|
 *  | PBEWithSHA1AndRC2_40                  |     40 to 1024    |            128            |
 *  |---------------------------------------|-------------------|---------------------------|
 * </pre>
 * 
 * PBE盐加密
 * @author fupf
 */
@SuppressWarnings("restriction")
public class PBECryptor extends SymmetricCryptor {

    /** 支持以下任意一种算法 */
    public static final String ALG_PBE_MD5_DES = "PBEWITHMD5andDES";
    public static final String ALG_PBE_SHA1_3DES = "PBEWithSHA1AndDESede";
    public static final String ALG_PBE_SHA1_RC2 = "PBEWithSHA1AndRC2_40";
    //public static final String ALG_PBE_MD5_3DES = "PBEWithMD5AndTripleDES"; // was wrong

    private static final RandomStringGenerator GENERATOR = new RandomStringGenerator.Builder()
                                                               .withinRange('!', '~').build();

    /**
     * default key 24 character
     * @param algName
     */
    public PBECryptor(String algName) {
        this(algName, GENERATOR.generate(24).toCharArray());
    }

    /**
     * default salt 24 bytes
     * @param algName
     * @param key
     */
    public PBECryptor(String algName, char[] key) {
        this(algName, key, SecureRandoms.nextBytes(24));
    }

    /**
     * default iterations 100
     * @param algName
     * @param pass
     * @param salt
     */
    public PBECryptor(String algName, char[] pass, byte[] salt) {
        this(algName, pass, salt, 100);
    }

    public PBECryptor(String algName, char[] pass, byte[] salt, int iterations) {
        super(generateSecret(algName, pass), null, null, 
              new PBEParameterSpec(salt, iterations), new SunJCE());
    }

    // --------------------------getter
    public char[] getPass() {
        return new String(getKey()).toCharArray();
    }

    @Override
    public byte[] getParameter() {
        return ((PBEParameterSpec) parameter).getSalt();
    }

    public int getIterations() {
        return ((PBEParameterSpec) parameter).getIterationCount();
    }

    private static SecretKey generateSecret(String algName, char[] pass) {
        //return new SecretKeySpec(new String(pass).getBytes(), algName); // 也可用此方法来构造具体的密钥
        try {
            return SecretKeyFactory.getInstance(algName).generateSecret(new PBEKeySpec(pass));
        } catch (GeneralSecurityException e) {
            throw new SecurityException(e);
        }
    }

    public static void main(String[] args) {
        String ag = PBECryptor.ALG_PBE_SHA1_3DES;

        // 加密
        PBECryptor p = new PBECryptor(ag, "fdsafasd".toCharArray(), "12343215678".getBytes(), 1000);
        byte[] a = p.encrypt("abc".getBytes());

        // 解密
        p = new PBECryptor(ag, p.getPass(), p.getParameter(), p.getIterations());
        byte[] b = p.decrypt(a);

        System.out.println(new String(b));
        
        System.out.println(GENERATOR.generate(100));
    }
}
