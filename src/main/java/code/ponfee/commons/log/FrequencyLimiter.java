package code.ponfee.commons.log;

/**
 * 访问频率控制
 * @author fupf
 */
public interface FrequencyLimiter {

    boolean checkAndTrace(String key);

    boolean setLimitQtyInMinutes(String key, long qty);

    long getLimitQtyInMinutes(String key);
}
