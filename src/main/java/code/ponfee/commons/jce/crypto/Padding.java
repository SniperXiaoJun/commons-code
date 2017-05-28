package code.ponfee.commons.jce.crypto;

/**
 * <p>
 *   encrypt padding
 *   pkcs7Padding must be BouncyCastleProvider supprot
 * </p>
 * @author fupf
 */
public enum Padding {
    NoPadding/*, PKCS1Padding*/, PKCS5Padding, PKCS7Padding, PADDING_ISO10126;
}
