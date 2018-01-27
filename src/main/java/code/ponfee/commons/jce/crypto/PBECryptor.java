package code.ponfee.commons.jce.crypto;

import java.security.GeneralSecurityException;
import java.security.Provider;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

import org.apache.commons.text.RandomStringGenerator;

import code.ponfee.commons.jce.Providers;
import code.ponfee.commons.util.SecureRandoms;

/**
 * <pre>
 *  |---------------------------------------|-------------------|---------------------------|
 *  |               Algorithm               | secret key length | default secret key length |
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
 * String是常量（即创建之后就无法更改），会保存到常量池中，如果有其他进程
 * 可以dump这个进程的内存，那么密码就会随着常量池被dump出去从而泄露。
 * 而char[]可以写入其他的信息从而改变，即是被dump了也会减少泄露密码的风险。
 * <p>
 * 
 * PBE salt encryption
 * @author fupf
 */
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
        this(algName, pass, salt, 100, null);
    }

    public PBECryptor(String algName, char[] pass, byte[] salt, 
                      int iterations, Provider provider) {
        super(generateSecret(algName, pass), null, null, 
              new PBEParameterSpec(salt, iterations), 
              (provider == null) ? Providers.SunJCE : provider);
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
        PBECryptor p = new PBECryptor(ag, "fdsafasd".toCharArray(), "12343215678".getBytes(), 1000, Providers.BC);
        byte[] encrypted = p.encrypt("abc".getBytes());

        // 解密
        p = new PBECryptor(p.getAlgorithm(), p.getPass(), p.getParameter(), p.getIterations(), p.getProvider());
        byte[] decrypted = p.decrypt(encrypted);
        System.out.println(new String(decrypted));
    }
}
