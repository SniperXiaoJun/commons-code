package code.ponfee.commons.jce.crypto;

/**
 * 对称密钥算法bit(位)
 *     DES                  key size must be equal to 64
 *     DESede(TripleDES)    key size must be equal to 112 or 168
 *     AES                  key size must be equal to 128, 192 or 256,but 192 and 256 bits may not be available
 *     Blowfish             key size must be multiple of 8, and can only range from 32 to 448 (inclusive)
 *     RC2                  key size must be between 40 and 1024 bits
 *     RC4(ARCFOUR)         key size must be between 40 and 1024 bits
 * </pre>
 * @author fupf
 */
public enum Algorithm {
    AES, DES, DESede, Blowfish, RC2, RC4, IDEA;
}
