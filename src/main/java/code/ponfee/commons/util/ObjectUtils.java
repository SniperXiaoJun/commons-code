package code.ponfee.commons.util;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.Collection;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import code.ponfee.commons.json.Jsons;
import code.ponfee.commons.math.Numbers;

/**
 * 公用对象工具类
 * @author Ponfee
 */
public final class ObjectUtils {

    private static final char[] URL_SAFE_BASE64_CODES = {
        'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H',
        'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P',
        'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X',
        'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f',
        'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n',
        'o', 'p', 'q', 'r', 's', 't', 'u', 'v',
        'w', 'x', 'y', 'z', '0', '1', '2', '3',
        '4', '5', '6', '7', '8', '9', '-', '_'
    };

    /**
     * 对象toString
     * @param obj
     * @return
     */
    public static String toString(Object obj) {
        return ReflectionToStringBuilder.reflectionToString(obj, ToStringStyle.JSON_STYLE);
    }

    /**
     * 对象转json
     * @param obj
     * @return
     */
    public static String toJson(Object obj) {
        return Jsons.NORMAL.stringify(obj);
    }

    /**
     * 判断对象是否为空
     * @param o
     * @return
     */
    public static boolean isEmpty(Object o) {
        if (o == null) {
            return true;
        } else if (CharSequence.class.isInstance(o)) {
            return ((CharSequence) o).length() == 0;
        } else if (Collection.class.isInstance(o)) {
            return ((Collection<?>) o).isEmpty();
        } else if (o.getClass().isArray()) {
            return Array.getLength(o) == 0;
        } else if (Map.class.isInstance(o)) {
            return ((Map<?, ?>) o).isEmpty();
        } else if (Dictionary.class.isInstance(o)) {
            return ((Dictionary<?, ?>) o).isEmpty();
        } else {
            return false;
        }
    }

    public static Object nullValue(Object obj, Object nullDefault) {
        return obj == null ? nullDefault : obj;
    }

    /**
     * map to javabean
     * @param map
     * @param bean
     */
    public static <T> void map2bean(Map<String, ?> map, T bean) {
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(bean.getClass());
            String name;
            Object value;
            Class<?> type;
            for (PropertyDescriptor prop : beanInfo.getPropertyDescriptors()) {
                name = prop.getName();
                if ("class".equals(name) || 
                    (!map.containsKey(name) && !map.containsKey(name = Strings.underscoreName(name)))
                ) {
                    continue;
                }

                value = map.get(name);
                type = prop.getPropertyType();
                if (type.isPrimitive() && Strings.isEmpty(value)) {
                    continue; // 原始类型跳过
                }

                //type = ClassUtils.primitiveToWrapper(type);
                //ClassUtils.isPrimitiveOrWrapper(type)
                if (ClassUtils.isPrimitiveWrapper(type) && Strings.isEmpty(value)) {
                    value = null; // 原始包装类型则设置为null
                } else if (byte.class == type) {
                    value = Numbers.toByte(value);
                } else if (Byte.class == type) {
                    value = Numbers.toWrapByte(value);
                } else if (short.class == type) {
                    value = Numbers.toShort(value);
                } else if (Short.class == type) {
                    value = Numbers.toWrapShort(value);
                } else if (int.class == type) {
                    value = Numbers.toInt(value);
                } else if (Integer.class == type) {
                    value = Numbers.toWrapInt(value);
                } else if (long.class == type) {
                    value = Numbers.toLong(value);
                } else if (Long.class == type) {
                    value = Numbers.toWrapLong(value);
                } else if (float.class == type) {
                    value = Numbers.toFloat(value);
                } else if (Float.class == type) {
                    value = Numbers.toWrapFloat(value);
                } else if (double.class == type) {
                    value = Numbers.toDouble(value);
                } else if (Double.class == type) {
                    value = Numbers.toWrapDouble(value);
                } else if (CharSequence.class.isAssignableFrom(type) && !type.isInstance(value)) {
                    value = type.getConstructor(String.class).newInstance(value.toString());
                }

                // set value into bean field
                prop.getWriteMethod().invoke(bean, value);
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * map value set to bean property
     * @param map
     * @param type   bean type, must be has default constructor
     * @return
     */
    public static <T> T map2bean(Map<String, ?> map, Class<T> type) {
        try {
            T bean = type.getConstructor().newInstance();
            map2bean(map, bean);
            return bean;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * javabean to map
     * @param bean
     * @return
     */
    public static Map<String, Object> bean2map(Object bean) {
        try {
            Map<String, Object> map = new HashMap<>();
            BeanInfo beanInfo = Introspector.getBeanInfo(bean.getClass());
            String name;
            for (PropertyDescriptor prop : beanInfo.getPropertyDescriptors()) {
                name = prop.getName();
                if (!"class".equals(name)) {
                    map.put(name, prop.getReadMethod().invoke(bean));
                }
            }
            return map;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * uuid byte array
     * @return
     */
    public static byte[] uuid() {
        UUID uuid = UUID.randomUUID();
        return ByteBuffer.allocate(16) // wrap(new byte[16])
                         .putLong(uuid.getMostSignificantBits())
                         .putLong(uuid.getLeastSignificantBits())
                         .array();
    }

    /**
     * uuid 32 string
     * @return
     */
    public static String uuid32() {
        UUID uuid = UUID.randomUUID();
        return Long.toHexString(uuid.getMostSignificantBits())
             + Long.toHexString(uuid.getLeastSignificantBits());
    }

    /**
     * 22位uuid
     * @return
     */
    public static String uuid22() {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(uuid());
    }

    /**
     * short uuid
     * @param len
     * @return
     */
    public static String shortid(int len) {
        return shortid(len, URL_SAFE_BASE64_CODES);
    }

    /**
     * short uuid
     * should between 3 (inclusive) and 32 (exclusive)
     * @param len
     * @param chars
     * @return
     */
    public static String shortid(int len, char[] chars) {
        int size = chars.length;
        StringBuilder builder = new StringBuilder(len);
        for (String str : Strings.slice(uuid32(), len)) {
            if (StringUtils.isNotEmpty(str)) {
                builder.append(chars[(int) (Long.parseLong(str, 16) % size)]);
            }
        }
        return builder.toString();
    }

    /**
     * 获取堆栈信息
     * @param deep
     * @return
     */
    public static String getStackTrace(int deepPath) {
        StackTraceElement[] traces = Thread.currentThread().getStackTrace();
        if (traces.length <= deepPath) {
            return "warning: out of stack trace.";
        }

        StackTraceElement trace = traces[deepPath];
        return new StringBuilder(trace.getClassName()).append("#")
                      .append(trace.getMethodName()).append(":")
                      .append(trace.getLineNumber()).toString();
    }

    /**
     * if t is null the other
     * @param t
     * @param other
     * @return
     */
    public static <T> T ifNull(T t, T other) {
        return t != null ? t : other;
    }

}
