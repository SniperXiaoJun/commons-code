package code.ponfee.commons.jce;

/**
 * Hash算法
 * @author Ponfee
 */
public enum HashAlgorithms {

    MD5("MD5"), // 

    RipeMD128("RipeMD128"), RipeMD160("RipeMD160"), // 
    RipeMD256("RipeMD256"), RipeMD320("RipeMD320"), // 

    SHA1("SHA-1"), SHA224("SHA-224"), SHA256("SHA-256"), // 
    SHA384("SHA-384"), SHA512("SHA-512"), // 

    KECCAK224("KECCAK-224"), KECCAK256("KECCAK-256"), // 
    KECCAK288("KECCAK-288"), KECCAK384("KECCAK-384"), 
    KECCAK512("KECCAK-512"), //

    ;

    private final String algorithm;

    private HashAlgorithms(String algorithm) {
        this.algorithm = algorithm;
    }

    public String algorithm() {
        return this.algorithm;
    }
}
