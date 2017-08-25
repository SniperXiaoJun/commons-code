package code.ponfee.commons.log;

public interface FrequencyLimiter {

    boolean checkAndTrace(String key);
    
    boolean setLimitQtyInMinutes(String key, long qty);
    
    long getLimitQtyInMinutes(String key);
}
