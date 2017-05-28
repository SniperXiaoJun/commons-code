package code.ponfee.commons.log;

import java.lang.reflect.Method;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import code.ponfee.commons.exception.ExceptionTracker;
import code.ponfee.commons.json.Jsons;
import code.ponfee.commons.reflect.ClassUtils;

/**
 * <pre>
 *   1.开启spring切面特性：<aop:aspectj-autoproxy />
 *   2.编写子类：
 *     `@Component
 *     `@Aspect
 *     public class TestLogger extends LoggerAspect {
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
public abstract class LoggerAspect {
    private static Logger logger = LoggerFactory.getLogger(LoggerAspect.class);
    private static final int ALARM_THRESHOLD_MILLIS = 2000; // 告警阀值2000ms

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
        LogInfo logInfo = new LogInfo(methodName);
        if (log != null) {
            logInfo.setType(log.type());
            logInfo.setDesc(log.desc());
        }

        String logs = getLogs(log);
        logInfo.setArgs(pjp.getArgs());
        if (logger.isInfoEnabled()) {
            logger.info("[exec-before]-[{}]{}-{}", methodName, logs, toJson(logInfo.getArgs()));
        }
        try {
            long start = System.currentTimeMillis();
            Object retVal = pjp.proceed();
            int cost = (int) (System.currentTimeMillis() - start);
            logInfo.setRetVal(retVal);
            logInfo.setCostTime(cost);
            if (cost > ALARM_THRESHOLD_MILLIS && logger.isWarnEnabled()) {
                logger.warn("[exec-time]-[{}]{}-[cost {}]", methodName, logs, cost);
            }
            if (logger.isInfoEnabled()) {
                logger.info("[exec-after]-[{}]{}-[{}]", methodName, logs, toJson(retVal));
            }
            return retVal;
        } catch (Throwable e) {
            logger.error("[exec-throwing]-[{}]{}-{}", methodName, logs, toJson(logInfo.getArgs()), e);
            logInfo.setException(ExceptionTracker.peekStackTrace(e));
            throw e;
            /*try {
                return method.getReturnType().getConstructor(int.class, String.class).newInstance(-1, e.getMessage());
            } catch(Exception ex) {
                throw e;
            }*/
        } finally {
            log(logInfo);
        }
    }

    protected void log(LogInfo logInfo) {
        // do no thing
    }

    private String getLogs(LogAnnotation log) {
        if (log == null) return "";
        return "-[" + log.type() + (log.desc() != null ? "," + log.desc() : "") + "]";
    }

    private String toJson(Object obj) {
        return Jsons.NON_NULL.stringify(obj);
    }

}
