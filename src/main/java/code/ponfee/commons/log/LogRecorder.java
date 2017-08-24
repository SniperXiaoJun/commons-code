package code.ponfee.commons.log;

import java.lang.reflect.Method;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import code.ponfee.commons.exception.ExceptionTracker;
import code.ponfee.commons.reflect.ClassUtils;
import code.ponfee.commons.util.ObjectUtils;

/**
 * <pre>
 *   1.开启spring切面特性：<aop:aspectj-autoproxy />
 *   2.编写子类：
 *     `@Component
 *     `@Aspect
 *     public class TestLogger extends LogRecorder {
 *         `@Around(value = "execution(public * cn.xxx.service.impl.*Impl.*(..)) && @annotation(log)", argNames = "pjp,log")
 *         `@Override
 *         public Object around(ProceedingJoinPoint pjp, LogAnnotation log) throws Throwable {
 *             return super.around(pjp, log);
 *         }
 *     }
 * </pre>
 * 
 * 日志管理切面处理
 * @author fupf
 */
public abstract class LogRecorder {

    private static final int DEFAULT_ALARM_THRESHOLD_MILLIS = 2000;
    private static Logger logger = LoggerFactory.getLogger(LogRecorder.class);

    private final int alarmThresholdMillis; // 告警阀值
    private final FrequencyLimiter freqLimiter; // 访问频率限制

    public LogRecorder() {
        this(DEFAULT_ALARM_THRESHOLD_MILLIS);
    }

    public LogRecorder(int alarmThresholdMillis) {
        this(alarmThresholdMillis, null);
    }

    public LogRecorder(FrequencyLimiter freqLimiter) {
        this(DEFAULT_ALARM_THRESHOLD_MILLIS, freqLimiter);
    }

    public LogRecorder(int alarmThresholdMillis, FrequencyLimiter freqLimiter) {
        this.alarmThresholdMillis = alarmThresholdMillis < 0 ? 1 : alarmThresholdMillis;
        this.freqLimiter = freqLimiter;
    }

    /**
     * 日志拦截
     * @param pjp
     * @return
     * @throws Throwable
     */
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        return this.around(pjp, null);
    }

    /**
     * 日志拦截
     * @param pjp
     * @param log
     * @return
     * @throws Throwable
     */
    public Object around(ProceedingJoinPoint pjp, LogAnnotation log) throws Throwable {
        MethodSignature m = (MethodSignature) pjp.getSignature();
        Method method = pjp.getTarget().getClass().getMethod(m.getName(), m.getParameterTypes());
        String methodName = ClassUtils.getMethodSignature(method);

        // request frequency limit
        if (log != null && log.limit() && freqLimiter != null
            && !freqLimiter.checkAndTrace(methodName)) {
            throw new IllegalStateException("request rejection");
        }

        LogInfo logInfo = new LogInfo(methodName);
        if (log != null) {
            logInfo.setType(log.type());
            logInfo.setDesc(log.desc());
        }

        String logs = getLogs(log);
        logInfo.setArgs(pjp.getArgs());
        if (logger.isInfoEnabled()) {
            logger.info("[exec-before]-[{}]{}-{}", methodName, logs, ObjectUtils.toString(logInfo.getArgs()));
        }
        int cost = 0;
        long start = System.currentTimeMillis();
        try {
            Object retVal = pjp.proceed();
            cost = (int) (System.currentTimeMillis() - start);
            logInfo.setRetVal(retVal);
            if (cost > alarmThresholdMillis && logger.isWarnEnabled()) {
                // 执行时间告警
                logger.warn("[exec-time]-[{}]{}-[cost {}]", methodName, logs, cost);
            }
            if (logger.isInfoEnabled()) {
                logger.info("[exec-after]-[{}]{}-[{}]", methodName, logs, ObjectUtils.toString(retVal));
            }
            return retVal;
        } catch (Throwable e) {
            cost = (int) (System.currentTimeMillis() - start);
            logger.error("[exec-throwing]-[{}]{}-{}", methodName, logs, ObjectUtils.toString(logInfo.getArgs()), e);
            logInfo.setException(ExceptionTracker.peekStackTrace(e));
            throw e; // 向外抛
        } finally {
            logInfo.setCostTime(cost);
            try {
                log(logInfo);
            } catch (Throwable ignored) {
                ignored.printStackTrace();
            }
        }
    }

    /**
     * 日志记录（可用于记录到日志表）
     * @param logInfo
     */
    protected void log(LogInfo logInfo) {
        // do no thing
    }

    private String getLogs(LogAnnotation log) {
        if (log == null) return "";

        return new StringBuilder("-[").append(log.type())
                                      .append(log.desc() != null ? "," + log.desc() : "")
                                      .append("]").toString();
    }

}
