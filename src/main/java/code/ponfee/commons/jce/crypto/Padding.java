package code.ponfee.commons.jce.crypto;

/**
 * encrypt padding
 * pkcs7Padding must be BouncyCastleProvider supprot
 * @author fupf
 */
public enum Padding {
    NoPadding, PKCS5Padding, PKCS7Padding, PADDING_ISO10126;
}
