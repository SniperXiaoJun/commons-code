package code.ponfee.commons.constrain;

import static code.ponfee.commons.model.ResultCode.ILLEGAL_ARGS;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
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
 * 方法参数校验：拦截参数中包含@ConstrainParam注解的方法
 * `@Component
 * `@Aspect
 * public class TestParamValidator extends ParamValidator {
 *    `@Around(value = "execution(public * code.ponfee.xxx.service.impl.*Impl.*(@code.ponfee.commons.constrain.ConstrainParam (*)))")
 *    public `@Override Object constrain(ProceedingJoinPoint joinPoint) throws Throwable {
 *      return super.constrain(joinPoint);
 *    }
 * }
 * </pre>
 * 
 * @author fupf
 */
public abstract class ParamValidator extends FieldValidator {

    private static Logger logger = LoggerFactory.getLogger(ParamValidator.class);

    /**
     * @param joinPoint
     * @param validator
     * @return
     * @throws Throwable
     */
    public Object constrain(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        if (args == null || args.length == 0) {
            return joinPoint.proceed();
        }

        // 参数校验
        StringBuilder builder = new StringBuilder();
        String[] argsName = null;
        Method method = null;
        String methodSign = null;
        try {
            // 缓存方法参数名
            MethodSignature mSign = (MethodSignature) joinPoint.getSignature();
            method = joinPoint.getTarget().getClass().getMethod(mSign.getName(), mSign.getParameterTypes());
            methodSign = ClassUtils.getMethodSignature(method);
            argsName = METHOD_SIGN_CACHE.get(methodSign);
            if (argsName == null) {
                argsName = ClassUtils.getMethodParamNames(method);
                METHOD_SIGN_CACHE.set(methodSign, argsName);
            }

            // 方法参数注解校验
            Annotation[][] anns = method.getParameterAnnotations();
            for (int i = 0; i < args.length; i++) {
                for (Annotation ann : anns[i]) {
                    if (ann instanceof ConstrainParam) {
                        try {
                            super.constrain(args[i]);
                        } catch (IllegalArgumentException e) {
                            builder.append("[").append(argsName[i]).append("]").append(e.getMessage());
                        }
                    }
                }
            }
        } catch (UnsupportedOperationException e) {
            builder.append(e.getMessage());
        } catch (Exception e) {
            logger.error("reflect exception", e);
            builder.append(ExceptionTracker.peekStackTrace(e));
        }

        if (builder.length() == 0) { // 校验成功
            return joinPoint.proceed();
        } else { // 校验失败，不调用方法，进入失败处理
            if (builder.length() > MAX_MSG_SIZE) {
                builder.setLength(MAX_MSG_SIZE - 3);
                builder.append("...");
            }
            String errMsg = builder.toString();
            if (logger.isInfoEnabled()) {
                logger.info("[参数校验失败]-[{}]-{}-[{}]", methodSign, ObjectUtils.toString(args), errMsg);
            }

            return returnFailed(method, errMsg);
        }
    }

    /**
     * 参数验证错误时返回数据处理<p>
     * 子类可覆盖此方法来自定义返回<p>
     * @param method  验证的目标方法
     * @param errMsg  错误信息
     * @return
     */
    protected Object returnFailed(Method method, String errMsg) {
        try {
            Constructor<?> c = method.getReturnType().getConstructor(int.class, String.class);
            return c.newInstance(ILLEGAL_ARGS.getCode(), ILLEGAL_ARGS.getMsg() + ": " + errMsg);
        } catch (Exception e) {
            throw new IllegalArgumentException(errMsg);
        }
    }
}
