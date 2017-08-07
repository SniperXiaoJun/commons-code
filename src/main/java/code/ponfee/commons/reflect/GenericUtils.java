package code.ponfee.commons.reflect;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import code.ponfee.commons.util.ObjectUtils;

/**
 * 泛型工具类
 * @author fupf
 */
public final class GenericUtils {

    private GenericUtils() {}

    /**
     * map泛型协变
     * @param origin
     * @return
     */
    public static Map<String, String> covariant(Map<String, ?> origin) {
        if (origin == null) return null;
        Map<String, String> target = new HashMap<>();
        for (Entry<String, ?> entry : origin.entrySet()) {
            target.put(entry.getKey(), Objects.toString(entry.getValue(), null));
        }
        return target;
    }

    /**
     * 获取泛化参数类型
     * @param clazz
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> Class<T> getClassGenricType(final Class<?> clazz) {
        return getClassGenricType(clazz, 0);
    }

    /**
     * 获取泛化参数类型
     * @param clazz
     * @param index
     * @return
     */
    @SuppressWarnings("rawtypes")
    public static Class getClassGenricType(final Class<?> clazz, final int index) {
        Type genType = clazz.getGenericSuperclass();
        if (!(genType instanceof ParameterizedType)) {
            return Object.class;
        }
        Type[] params = ((ParameterizedType) genType).getActualTypeArguments();
        if (index >= params.length || index < 0) {
            return Object.class;
        } else if (params[index] instanceof ParameterizedType) {
            return (Class<?>) ((ParameterizedType) params[index]).getRawType();
        } else if (params[index] instanceof GenericArrayType) {
            Type type = ((GenericArrayType) params[index]).getGenericComponentType();
            return Array.newInstance((Class<?>) ((ParameterizedType) type).getRawType(), 0).getClass();
        } else if (params[index] instanceof Class<?>) {
            return (Class<?>) params[index];
        } else {
            return Object.class;
        }
    }

    /**
     * 获取泛型参数的实际类型
     * @param method
     * @param methodParamsIndex
     * @param genericParamsIndex
     * @return
     */
    public static Class<?> getActualTypeArgument(Method method, int methodParamsIndex, int genericParamsIndex) {
        Type type = method.getGenericParameterTypes()[methodParamsIndex];
        return getParameterType(((ParameterizedType) type).getActualTypeArguments()[genericParamsIndex]);
    }

    /**
     * private List<test.ClassA> data; -> test.ClassA
     * 获取泛型参数的实际类型
     * @param field
     * @param genericParamsIndex
     * @return
     */
    public static Class<?> getActualTypeArgument(Field field, int genericParamsIndex) {
        Type type = field.getGenericType();
        //type.getTypeName(); -> java.util.List<test.ClassA>
        //((ParameterizedType) type).getRawType(); // interface java.util.List
        //((ParameterizedType) type).getOwnerType(); // null
        return getParameterType(((ParameterizedType) type).getActualTypeArguments()[genericParamsIndex]);
    }

    private static Class<?> getParameterType(Type type) {
        if (type instanceof Class<?>) {
            return (Class<?>) type;
        } else if (type instanceof WildcardType) {
            WildcardType wType = (WildcardType) type;
            if (!ObjectUtils.isEmpty(wType.getLowerBounds())) { // List<? super A>
                return (Class<?>) wType.getLowerBounds()[0];
            } else if (!ObjectUtils.isEmpty(wType.getUpperBounds())) { // List<? extends A>
                return (Class<?>) wType.getUpperBounds()[0];
            } else { // List<?>
                return Object.class;
            }
        } else {
            return Object.class;
        }
    }

}
