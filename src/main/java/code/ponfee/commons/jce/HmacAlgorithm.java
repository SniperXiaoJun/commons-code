package code.ponfee.commons.jce;

import com.google.common.collect.ImmutableBiMap;

/**
 * Hamc算法
 * @author Ponfee
 */
public enum HmacAlgorithm {

    HmacMD5, HmacSHA1, HmacSHA224, HmacSHA256, HmacSHA384, HmacSHA512;

    public static final ImmutableBiMap<Integer, HmacAlgorithm> ALGORITHM_MAPPING = 
        ImmutableBiMap.<Integer, HmacAlgorithm>builder()
        .put(1, HmacAlgorithm.HmacSHA1)
        .put(2, HmacAlgorithm.HmacSHA224)
        .put(3, HmacAlgorithm.HmacSHA256)
        .put(4, HmacAlgorithm.HmacSHA384)
        .put(5, HmacAlgorithm.HmacSHA512)
        .build();
}
