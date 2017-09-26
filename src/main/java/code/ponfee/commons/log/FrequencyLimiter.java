package code.ponfee.commons.log;

import java.util.Date;

/**
 * 访问频率控制
 * @author fupf
 */
public interface FrequencyLimiter {

    /**
     * 校验并追踪
     * @param key
     * @return
     */
    boolean checkAndTrace(String key);

    /**
     * 按区间统计
     * @param key
     * @param from
     * @param to
     * @return
     */
    long countByRange(String key, Date from, Date to);

    /**
     * 设置一分钟的访问限制量
     * @param key
     * @param qty
     * @return
     */
    boolean setLimitsInMinute(String key, long qty);

    /**
     * 获取配置的访问量
     * @param key
     * @return
     */
    long getLimitsInMinute(String key);
}
