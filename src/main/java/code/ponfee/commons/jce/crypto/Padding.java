package code.ponfee.commons.jce.crypto;

/**
 * encrypt padding
 * pkcs7Padding must be has BouncyCastleProvider support
 * PKCS7Padding：缺几个字节就补几个字节的0
 * PKCS5Padding：缺几个字节就补充几个字节的几，如缺6个字节就补充6个字节的6
 * @author fupf
 */
public enum Padding {
    NoPadding, PKCS5Padding, PKCS7Padding, PADDING_ISO10126;
}
