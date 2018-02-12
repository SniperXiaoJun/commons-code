package code.ponfee.commons.jce;

import com.google.common.collect.ImmutableBiMap;

/**
 * Hamc算法
 * @author Ponfee
 */
public enum HmacAlgorithms {

    HmacMD5, //

    HmacRipeMD128, HmacRipeMD160, // 
    HmacRipeMD256, HmacRipeMD320, // 

    HmacSHA1, HmacSHA224, HmacSHA256, //
    HmacSHA384, HmacSHA512, // 

    HmacKECCAK224, HmacKECCAK288, HmacKECCAK256, //
    HmacKECCAK384, HmacKECCAK512, //

    ;

    public static final ImmutableBiMap<Integer, HmacAlgorithms> ALGORITHM_MAPPING =
        ImmutableBiMap.<Integer, HmacAlgorithms> builder()
            .put(1, HmacAlgorithms.HmacSHA256)
            .put(2, HmacAlgorithms.HmacSHA512)
            .put(3, HmacAlgorithms.HmacKECCAK256)
            .put(4, HmacAlgorithms.HmacKECCAK512)
            .build();
}
