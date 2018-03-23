package code.ponfee.commons.jce;

import com.google.common.collect.ImmutableBiMap;

/**
 * The Hamc Algorithms
 * @author Ponfee
 */
public enum HmacAlgorithms {

    HmacMD5(16), //

    HmacRipeMD128(16), HmacRipeMD160(20), // 
    HmacRipeMD256(32), HmacRipeMD320(40), // 

    HmacSHA1(20), HmacSHA224(28), // 
    HmacSHA256(32), HmacSHA384(48), // 
    HmacSHA512(64), // 

    /**
     * @see org.bouncycastle.crypto.digests.KeccakDigest
     * @see org.bouncycastle.jcajce.provider.digest.Keccak
     */
    HmacKECCAK224(28), HmacKECCAK288(36), // 
    HmacKECCAK256(32), HmacKECCAK384(48), // 
    HmacKECCAK512(64), //

    /**
     * @see org.bouncycastle.crypto.digests.SHA3Digest
     * @see org.bouncycastle.jcajce.provider.digest.SHA3
     */
    HmacSHA3_224("HmacSHA3-224", 28),HmacSHA3_256("HmacSHA3-256", 32), // 
    HmacSHA3_384("HmacSHA3-384", 48),HmacSHA3_512("HmacSHA3-512", 64), // 
    ;

    private final String algorithm;
    private final int byteSize;

    HmacAlgorithms(int byteSize) {
        this.algorithm = this.name();
        this.byteSize = byteSize;
    }

    HmacAlgorithms(String algorithm, int byteSize) {
        this.algorithm = algorithm;
        this.byteSize = byteSize;
    }

    public String algorithm() {
        return this.algorithm;
    }

    public int byteSize() {
        return this.byteSize;
    }

    public static final ImmutableBiMap<Integer, HmacAlgorithms> ALGORITHM_MAPPING =
        ImmutableBiMap.<Integer, HmacAlgorithms> builder()
            .put(1, HmacAlgorithms.HmacSHA256)
            .put(2, HmacAlgorithms.HmacSHA512)
            .put(3, HmacAlgorithms.HmacKECCAK256)
            .put(4, HmacAlgorithms.HmacKECCAK512)
            .put(5, HmacAlgorithms.HmacSHA3_256)
            .put(6, HmacAlgorithms.HmacSHA3_512)
            .build();
}
