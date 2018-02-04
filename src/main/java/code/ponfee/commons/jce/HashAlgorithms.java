package code.ponfee.commons.jce;

/**
 * Hash算法
 * @author Ponfee
 */
public enum HashAlgorithms {

    MD5("MD5"), SHA1("SHA-1"), SHA224("SHA-224"), // 
    SHA256("SHA-256"), SHA384("SHA-384"), SHA512("SHA-512"), // 
    RipeMD128("RipeMD128"), RipeMD160("RipeMD160"), // 
    RipeMD256("RipeMD256"), RipeMD320("RipeMD320"); // 

    private final String algorithm;

    private HashAlgorithms(String algorithm) {
        this.algorithm = algorithm;
    }

    public String algorithm() {
        return this.algorithm;
    }
}
