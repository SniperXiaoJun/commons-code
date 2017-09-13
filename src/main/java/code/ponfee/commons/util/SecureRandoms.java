package code.ponfee.commons.util;

import java.security.SecureRandom;

/**
 * 安全随机数生成工具类
 * @author Ponfee
 */
public final class SecureRandoms {

    /** 随机数 */
    private static final SecureRandom RANDOM = new SecureRandom();
    static {
        RANDOM.setSeed(new SecureRandom(ObjectUtils.uuid()).generateSeed(20));
    }

    /**
     * random byte[] array by SecureRandom
     * @param numOfByte
     * @return
     */
    public static byte[] nextBytes(int numOfByte) {
        byte[] bytes = new byte[numOfByte];
        RANDOM.nextBytes(bytes);
        return bytes;
    }

    /**
     * returns a pseudo random int
     * @param bound
     * @return
     */
    public static int nextInt(int bound) {
        return RANDOM.nextInt(bound);
    }
}
