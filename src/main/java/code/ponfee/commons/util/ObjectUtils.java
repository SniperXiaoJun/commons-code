package code.ponfee.commons.util;

import static com.google.common.base.CaseFormat.LOWER_CAMEL;
import static com.google.common.base.CaseFormat.LOWER_UNDERSCORE;
import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Array;
import java.util.Base64;
import java.util.Collection;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringStyle;

import code.ponfee.commons.json.Jsons;
import code.ponfee.commons.math.Numbers;
import code.ponfee.commons.reflect.ClassUtils;

/**
 * 公用对象工具类
 * @author Ponfee
 */
public final class ObjectUtils {

    private static final char[] URL_SAFE_BASE64_CODES = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_".toCharArray();

    /**
     * 对象toString
     * @param obj
     * @return
     */
    public static String toString(Object obj) {
        return (obj == null) 
               ? "null" 
               : reflectionToString(obj, ToStringStyle.JSON_STYLE);
    }

    public static String toString(Object obj, String defaultStr) {
        return (obj == null) ? defaultStr : toString(obj);
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
    @SuppressWarnings("unchecked")
    public static <T, E extends Enum<E>> void map2bean(Map<String, ?> map, T bean) {
        String name;
        Object value;
        Class<?> type;
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(bean.getClass());
            for (PropertyDescriptor prop : beanInfo.getPropertyDescriptors()) {
                if ("class".equals(name = prop.getName())) {
                    continue;
                }

                String name0 = name;
                if (!map.containsKey(name)
                    && !map.containsKey(name = LOWER_CAMEL.to(LOWER_UNDERSCORE, name0))
                    && !map.containsKey(name = LOWER_UNDERSCORE.to(LOWER_CAMEL, name0))) {
                    continue;
                }

                value = map.get(name);
                type = prop.getPropertyType();
                if (type.isPrimitive()) {
                    // 原始类型
                    //type = org.apache.commons.lang3.ClassUtils.primitiveToWrapper(type);
                    //org.apache.commons.lang3.ClassUtils.isPrimitiveOrWrapper(type)
                    if (Strings.isEmpty(value)) {
                        continue; // value为null或为空字符串时跳过
                    } else if (byte.class == type) {
                        value = Numbers.toByte(value);
                    } else if (short.class == type) {
                        value = Numbers.toShort(value);
                    } else if (char.class == type) {
                        value = Numbers.toChar(value);
                    } else if (int.class == type) {
                        value = Numbers.toInt(value);
                    } else if (long.class == type) {
                        value = Numbers.toLong(value);
                    } else if (float.class == type) {
                        value = Numbers.toFloat(value);
                    } else if (double.class == type) {
                        value = Numbers.toDouble(value);
                    } else {
                        // cannot to run in this place
                    }
                } else if (value == null) {
                    // nothing to do: value = value
                } else if (!type.isInstance(value)) { // 类型不一致时（此时value!=null）
                    if (org.apache.commons.lang3.ClassUtils.isPrimitiveWrapper(type)
                        && Strings.isEmpty(value)) {
                        value = null; // 原始包装类型且value为空或为空字符串则设置为null
                    } else if (Byte.class == type) {
                        value = Numbers.toWrapByte(value);
                    } else if (Short.class == type) {
                        value = Numbers.toWrapShort(value);
                    } else if (Character.class == type) {
                        value = Numbers.toWrapChar(value);
                    } else if (Integer.class == type) {
                        value = Numbers.toWrapInt(value);
                    } else if (Long.class == type) {
                        value = Numbers.toWrapLong(value);
                    } else if (Float.class == type) {
                        value = Numbers.toWrapFloat(value);
                    } else if (Double.class == type) {
                        value = Numbers.toWrapDouble(value);
                    } else if (type.isEnum()) {
                        // enum class
                        if (value instanceof Number) {
                            value = type.getEnumConstants()[((Number) value).intValue()];
                        } else {
                            value = Enum.valueOf((Class<E>) type, value.toString());
                            /*String str = value.toString();
                            for (Object e : type.getEnumConstants()) {
                                if (((Enum<?>) e).name().equals(str)) {
                                    value = e;
                                    break;
                                }
                            }*/
                        }
                    } else if (CharSequence.class.isAssignableFrom(type)) {
                        // Construct a CharSequence
                        // new String(value.toString()), new StringBuilder(value.toString())
                        value = type.getConstructor(String.class).newInstance(value.toString());
                    } else {
                        throw new ClassCastException(ClassUtils.getClassName(value.getClass())
                                      + " cannot be cast to " + ClassUtils.getClassName(type));
                    }
                } else {
                    // nothing to do: type.isInstance(value)
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
        long most  = uuid.getMostSignificantBits(), 
             least = uuid.getLeastSignificantBits();
       return new byte[] {
           (byte) (most >>> 56), (byte) (most >>> 48),
           (byte) (most >>> 40), (byte) (most >>> 32),
           (byte) (most >>> 24), (byte) (most >>> 16),
           (byte) (most >>>  8), (byte) (most       ),

           (byte) (least >>> 56), (byte) (least >>> 48),
           (byte) (least >>> 40), (byte) (least >>> 32),
           (byte) (least >>> 24), (byte) (least >>> 16),
           (byte) (least >>>  8), (byte) (least       )
       };
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
        return new StringBuilder()
                      .append(trace.getLineNumber()).append(":")
                      .append(trace.getClassName())
                      .append("#").append(trace.getMethodName())
                      .toString();
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
