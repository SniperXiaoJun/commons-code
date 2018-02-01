package code.ponfee.commons.jce;

import com.google.common.collect.ImmutableBiMap;

/**
 * Hamc算法
 * @author Ponfee
 */
public enum HmacAlgorithms {

    HmacMD5, HmacSHA1, HmacSHA224, // 
    HmacSHA256, HmacSHA384, HmacSHA512, // 
    HmacRipeMD128, HmacRipeMD160, // 
    HmacRipeMD256, HmacRipeMD320, // 
    ;

    public static final ImmutableBiMap<Integer, HmacAlgorithms> ALGORITHM_MAPPING = 
        ImmutableBiMap.<Integer, HmacAlgorithms>builder()
        .put(1, HmacAlgorithms.HmacSHA1)
        .put(2, HmacAlgorithms.HmacSHA224)
        .put(3, HmacAlgorithms.HmacSHA256)
        .put(4, HmacAlgorithms.HmacSHA384)
        .put(5, HmacAlgorithms.HmacSHA512)
        .build();
}
