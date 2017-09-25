package code.ponfee.commons.constrain;

import static code.ponfee.commons.model.ResultCode.ILLEGAL_ARGS;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import code.ponfee.commons.reflect.ClassUtils;
import code.ponfee.commons.reflect.Fields;
import code.ponfee.commons.util.ObjectUtils;

/**
 * <pre>
 * 方法参数校验：拦截方法中包含@Constraints注解的方法
 * e.g.：
 *    1.开启spring切面特性：<aop:aspectj-autoproxy />
 *    2.编写子类：
 *        `@Component
 *        `@Aspect
 *        public class TestMethodValidator extends MethodValidator {
 *            `@Around(value = "execution(public * code.ponfee.xxx.service.impl.*Impl.*(..)) && `@annotation(cst)", argNames = "pjp,cst")
 *            public `@Override Object constrain(ProceedingJoinPoint pjp, Constraints cst) throws Throwable {
 *                return super.constrain(pjp, cst);
 *            }
 *        }
 * </pre>
 * 
 * 参数校验
 * @author fupf
 */
public abstract class MethodValidator extends FieldValidator {

    private static Logger logger = LoggerFactory.getLogger(MethodValidator.class);

    /**
     * @param joinPoint
     * @param validator
     * @return
     * @throws Throwable
     */
    public Object constrain(ProceedingJoinPoint joinPoint, Constraints validator) throws Throwable {
        Object[] args = joinPoint.getArgs();
        if (args == null || args.length == 0) {
            return joinPoint.proceed();
        }

        // Method method = mSign.getMethod();
        MethodSignature ms = (MethodSignature) joinPoint.getSignature();
        Method method = joinPoint.getTarget().getClass().getMethod(ms.getName(), ms.getParameterTypes());
        String methodSign = ClassUtils.getMethodSignature(method);
        String[] argsName = METHOD_SIGN_CACHE.get(methodSign);
        if (argsName == null) {
            // 要用到asm字节码操作，消耗性能，所以缓存
            argsName = ClassUtils.getMethodParamNames(method);
            METHOD_SIGN_CACHE.set(methodSign, argsName);
        }

        // 校验开始
        StringBuilder builder = new StringBuilder();
        Class<?>[] paramTypes = method.getParameterTypes();
        Constraint cst = null;
        String fieldName;
        Object fieldVal;
        Class<?> fieldType;
        Constraint[] csts = validator.value();
        try {
            boolean[] argsNullable = argsNullable(args, csts);
            for (int len = csts.length, i = 0; i < len; i++) {
                cst = csts[i];
                fieldVal = args[cst.index()]; // 参数对象校验
                fieldType = paramTypes[cst.index()];
                if (argsNullable[cst.index()] && fieldVal == null) {
                    continue; // 参数可为空，则跳过校验
                } else if (StringUtils.isEmpty(cst.field())) {
                    // 验证参数对象
                    fieldName = argsName[cst.index()];
                    builder.append(constrain(methodSign, fieldName, fieldVal, cst, fieldType));
                } else if (fieldVal == null) {
                    // 不可为空，则抛出异常
                    String msg;
                    if (args.length == 1) {
                        msg = "参数不能为空;";
                    } else {
                        msg = "参数{" + argsName[cst.index()] + "}不能为空;";
                    }
                    throw new IllegalArgumentException(msg);
                } else if (Map.class.isInstance(fieldVal) || Dictionary.class.isInstance(fieldVal)) {
                    // 验证map对象
                    Method get = fieldVal.getClass().getMethod("get", Object.class);
                    fieldVal = get.invoke(fieldVal, cst.field());
                    fieldType = fieldVal == null ? null : fieldVal.getClass();
                    fieldName = argsName[cst.index()] + "[" + cst.field() + "]";
                    builder.append(constrain(fieldName, fieldVal, cst, fieldType));
                } else {
                    // 验证java bean
                    String[] ognl = cst.field().split("\\.");
                    Field field = null;
                    for (String s : ognl) {
                        field = ClassUtils.getField(fieldType, s);
                        fieldType = field.getType();
                        if (fieldVal != null) {
                            fieldVal = Fields.get(fieldVal, field);
                        }
                    }
                    fieldName = argsName[cst.index()] + "." + cst.field();
                    builder.append(constrain(methodSign, fieldName, fieldVal, cst, fieldType));
                }
            }
        } catch (NoSuchFieldException e) {
            String msg = "配置错误：不存在的字段[" + argsName[cst.index()] + "." + cst.field() + "]";
            logger.error(msg, e);
            builder.append(msg);
        } catch (UnsupportedOperationException | IllegalArgumentException e) {
            builder.append(e.getMessage());
        } catch (Exception e) {
            logger.error("参数约束校验异常", e);
            builder.append("参数约束校验异常：" + e.getMessage());
        }

        if (builder.length() == 0) { // 校验成功
            return joinPoint.proceed(); // 调用方法
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
            throw new IllegalArgumentException(errMsg, e);
        }
    }

    // --------------------------------------private methods-----------------------------------
    private boolean[] argsNullable(Object[] args, Constraint[] csts) {
        Set<String> set = new HashSet<>();
        boolean[] isArgsNullable = new boolean[args.length];
        Arrays.fill(isArgsNullable, false);
        for (int i = 0; i < csts.length; i++) {
            String key = "index=" + csts[i].index() + ", field=\"" + csts[i].field() + "\"";
            if (!set.add(key)) {
                throw new RuntimeException("配置错误，重复校验[" + key + "]");
            }

            if (csts[i].index() > args.length - 1) {
                throw new RuntimeException("配置错误，下标超出[index=" + csts[i].index() + "]");
            }

            if (StringUtils.isEmpty(csts[i].field()) && !csts[i].notNull()) {
                isArgsNullable[csts[i].index()] = true; // 该参数可为空
            }
        }
        return isArgsNullable;
    }

}
