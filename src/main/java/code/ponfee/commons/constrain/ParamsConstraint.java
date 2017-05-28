package code.ponfee.commons.constrain;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import code.ponfee.commons.exception.ExceptionTracker;
import code.ponfee.commons.reflect.ClassUtils;

/**
 * <pre>
 * 方法参数校验：拦截参数中包含@ConstrainParam注解的方法
 *  `@Around(value = "execution(public * cn.xxx.service.impl.*Impl.*(@code.ponfee.commons.constrain.ConstrainParam (*)))")
 *  public Object constrain(ProceedingJoinPoint joinPoint) throws Throwable {
 *      return new ParameterConstraint().constrain(joinPoint);
 *  }
 * </pre>
 * 
 * @author fupf
 */
public class ParamsConstraint extends FieldConstraint {
    private static Logger logger = LoggerFactory.getLogger(ParamsConstraint.class);

    /**
     * @param joinPoint
     * @param validator
     * @return
     * @throws Throwable
     */
    public Object constrain(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        if (args == null || args.length == 0) return joinPoint.proceed();

        // 参数校验
        StringBuilder builder = new StringBuilder();
        String[] argsName = null;
        Method method = null;
        try {
            // 缓存方法参数名
            MethodSignature mSign = (MethodSignature) joinPoint.getSignature();
            method = joinPoint.getTarget().getClass().getMethod(mSign.getName(), mSign.getParameterTypes());

            String methodSign = ClassUtils.getMethodSignature(method);
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

        // verify result
        if (builder.length() > 0) {
            if (builder.length() > 3000) {
                builder.setLength(2997);
                builder.append("...");
            }
            String errMsg = builder.toString();
            try {
                return method.getReturnType().getConstructor(int.class, String.class).newInstance(-9999, errMsg);
            } catch (Exception e) {
                throw new IllegalArgumentException(errMsg);
            }
        } else {
            return joinPoint.proceed();
        }
    }
}
