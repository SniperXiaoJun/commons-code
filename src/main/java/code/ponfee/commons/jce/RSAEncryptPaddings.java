package code.ponfee.commons.jce;

/**
 * RSA加密填充
 * @author fupf
 */
public enum RSAEncryptPaddings {

    NO_PADDING, // 无填充
    PKCS1_PADDING, // 原文必须 比RSA钥模长(modulus)短至少11个字节
    PKCS1_OAEP_PADDING, // RSA_size(rsa) – 41
    SSLV23_PADDING;

}
