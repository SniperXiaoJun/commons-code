package code.ponfee.commons.jce;

/**
 * The Digest Algorithms
 * 
 * @author Ponfee
 */
public enum DigestAlgorithms {

    MD5(16), // 

    RipeMD128(16), RipeMD160(20), RipeMD256(32), RipeMD320(40), // 

    SHA1("SHA-1", 20), SHA224("SHA-224", 28), SHA256("SHA-256", 32), // 
    SHA384("SHA-384", 48), SHA512("SHA-512", 64), // 

    /**
     * @see org.bouncycastle.crypto.digests.KeccakDigest
     * @see org.bouncycastle.jcajce.provider.digest.Keccak
     */
    KECCAK224("KECCAK-224", 28), KECCAK256("KECCAK-256", 32), // 
    KECCAK288("KECCAK-288", 36), KECCAK384("KECCAK-384", 48), // 
    KECCAK512("KECCAK-512", 64), //

    /**
     * @see org.bouncycastle.crypto.digests.SHA3Digest
     * @see org.bouncycastle.jcajce.provider.digest.SHA3
     */
    SHA3_224("SHA3-224", 28), SHA3_256("SHA3-256", 32), // 
    SHA3_384("SHA3-384", 48), SHA3_512("SHA3-512", 64), //
    ;

    private final String algorithm;
    private final int byteSize;

    DigestAlgorithms(int byteSize) {
        this.algorithm = this.name();
        this.byteSize = byteSize;
    }

    DigestAlgorithms(String algorithm, int byteSize) {
        this.algorithm = algorithm;
        this.byteSize = byteSize;
    }

    public String algorithm() {
        return this.algorithm;
    }

    public int byteSize() {
        return byteSize;
    }

}
