package code.ponfee.commons.util;

import static com.google.common.base.CaseFormat.LOWER_CAMEL;
import static com.google.common.base.CaseFormat.LOWER_UNDERSCORE;
import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Date;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.commons.lang3.time.DateUtils;

import code.ponfee.commons.json.Jsons;
import code.ponfee.commons.math.Numbers;
import code.ponfee.commons.reflect.ClassUtils;
import code.ponfee.commons.reflect.Fields;

/**
 * 公用对象工具类
 * @author Ponfee
 */
public final class ObjectUtils {

    private static final char[] URL_SAFE_BASE64_CODES = 
        "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_".toCharArray();
    private static final String[] DATE_PATTERN = { 
        "yyyy-MM-dd", "yyyy-MM-dd HH:mm:ss", "yyyyMMdd", "yyyyMMddHHmmss", 
        "yyyyMMddHHmmssSSS", "yyyy-MM-dd HH:mm:ss.SSS" 
    };

    /**
     * Returns object toString
     * 
     * @param obj the target object
     * @return the string of object
     */
    public static String toString(Object obj) {
        return toString(obj, "null");
    }

    public static String toString(Object obj, String defaultStr) {
        return (obj == null) 
               ? defaultStr 
               : reflectionToString(obj, ToStringStyle.JSON_STYLE);
    }

    /**
     * 对象转json
     * @param obj
     * @return
     */
    public static String toJson(Object obj) {
        return Jsons.toJson(obj);
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

    /**
     * Returns {@code true} if the provided reference 
     * is not {@code null} otherwise {@code false}
     * 
     * user in method reference
     * {@link java.util.Objects#isNull(Object)}
     * 
     * @param o the object
     * @return {@code true} the object is not null
     */
    public static boolean isNotNull(Object o) {
        return o != null;
    }

    /**
     * Map object converts to java bean object
     * 
     * @param map the map object
     * @param bean the java bean object
     */
    public static <T> void map2bean(Map<String, ?> map, T bean) {
        String name;
        Object value;
        Class<?> type;
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(bean.getClass());
            for (PropertyDescriptor prop : beanInfo.getPropertyDescriptors()) {
                if ("class".equals(name = prop.getName())) { // getClass()
                    continue;
                }

                String name0 = name;
                if (   !map.containsKey(name)
                    && !map.containsKey(name = LOWER_UNDERSCORE.to(LOWER_CAMEL, name0))
                    && !map.containsKey(name = LOWER_CAMEL.to(LOWER_UNDERSCORE, name0))
                ) {
                    continue;
                }

                value = map.get(name);
                if ((type = prop.getPropertyType()).isPrimitive() && Strings.isEmpty(value)) {
                    continue; // 原始类型时：value为null或为空字符串时跳过
                }

                // set value into bean field
                prop.getWriteMethod().invoke(bean, convert(value, type));
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Returns a map object which is copy of java bean
     * 
     * @param map   the map object
     * @param type  the java bean type, must has a default constructor
     * @return a java bean object
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
     * Returns a map object which is copy of java bean
     * 
     * @param bean the java bean object
     * @return a HashMap object
     */
    public static Map<String, Object> bean2map(Object bean) {
        try {
            Map<String, Object> map = new HashMap<>();
            BeanInfo beanInfo = Introspector.getBeanInfo(bean.getClass());
            String name;
            for (PropertyDescriptor prop : beanInfo.getPropertyDescriptors()) {
                if (!"class".equals((name = prop.getName()))) { // getClass()
                    map.put(name, prop.getReadMethod().invoke(bean));
                }
            }
            return map;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Returns a object that convert spec value to type
     * 
     * @param value source object
     * @param type  target object type
     * @return a object of type 
     * @throws Exception if occur error
     */
    @SuppressWarnings("unchecked")
    public static <T, E extends Enum<E>> Object convert(Object value, Class<?> type) 
        throws Exception {
        if (type.isPrimitive()) {
            // 原始类型
            //type = org.apache.commons.lang3.ClassUtils.primitiveToWrapper(type);
            //org.apache.commons.lang3.ClassUtils.isPrimitiveOrWrapper(type)
            if (boolean.class == type) {
                value = Numbers.toBoolean(value);
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
                // cannot happened
                throw new UnsupportedOperationException("unknow primitive class: " + type.toString());
            }
        } else if (value != null && !type.isInstance(value)) { // 类型不一致时
            if (   org.apache.commons.lang3.ClassUtils.isPrimitiveWrapper(type)
                && Strings.isEmpty(value)
            ) {
                value = null; // 原始包装类型且value为空或为空字符串则设置为null
            } else if (Boolean.class == type) {
                value = Numbers.toWrapBoolean(value);
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
                        if (((Enum<?>) e).name().equalsIgnoreCase(str)) {
                            value = e;
                            break;
                        }
                    }*/
                }
            } else if (Date.class == type) {
                if (value instanceof Number) {
                    value = new Date(((Number) value).longValue());
                } else if (value instanceof CharSequence) {
                    String str = value.toString();
                    if (StringUtils.isNumeric(str) && !RegexUtils.isDatePattern(str)) {
                        value = new Date(Numbers.toLong(str));
                    } else {
                        value = DateUtils.parseDate(str, DATE_PATTERN);
                    }
                } else {
                    throw new IllegalArgumentException("Illegal date time: " + value);
                }
            } else if (CharSequence.class.isAssignableFrom(type)) {
                // Construct a CharSequence
                // e.g. new String(value.toString()) and new StringBuilder(value.toString())
                value = type.getConstructor(String.class).newInstance(value.toString());
            } else {
                throw new ClassCastException(ClassUtils.getClassName(value.getClass())
                              + " cannot be cast to " + ClassUtils.getClassName(type));
            }
        } //else { /*nothing to do: value is null or type.isInstance(value)*/ }
        return value;
    }

    /**
     * Returns the serialize byte array data for value
     * 
     * @param value the value
     * @param type  the type of value
     * @return a byte array
     */
    public static byte[] serialize(Object value) {
        if (value == null) {
            return null;
        } else if (value instanceof byte[]) {
            return (byte[]) value;
        } else if (value instanceof Byte[]) {
            return ArrayUtils.toPrimitive((Byte[]) value);
        } else if (value instanceof InputStream) {
            try (InputStream input = (InputStream) value) {
                return IOUtils.toByteArray(input);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else if (value instanceof Boolean) {
            return new byte[] { (Boolean) value ? (byte) 0x01 : (byte) 0x00 };
        } else if (value instanceof Byte) {
            return new byte[] { Numbers.toByte(value) };
        } else if (value instanceof Short) {
            return Bytes.fromShort(Numbers.toShort(value));
        } else if (value instanceof Character) {
            return Bytes.fromChar(Numbers.toChar(value));
        } else if (value instanceof Integer) {
            return Bytes.fromInt(Numbers.toInt(value));
        } else if (value instanceof Long) {
            return Bytes.fromLong(Numbers.toLong(value));
        } else if (value instanceof Float) {
            return Bytes.fromFloat(Numbers.toFloat(value));
        } else if (value instanceof Double) {
            return Bytes.fromDouble(Numbers.toDouble(value));
        } else if (value instanceof String) {
            return ((String) value).getBytes(StandardCharsets.UTF_8);
        } else if (value instanceof Date) {
            return Bytes.fromLong(((Date) value).getTime());
        } else if (value instanceof Enum) {
            return Bytes.fromInt(((Enum<?>) value).ordinal());
        } else if (value instanceof Serializable) {
            return SerializationUtils.serialize((Serializable) value);
        } else {
            throw new UnsupportedOperationException(
                ClassUtils.getClassName(value.getClass()) + " is not Serializable."
            );
        }
    }

    /**
     * Returns a object or primitive value from 
     * deserialize the byte array
     * 
     * @param value the byte array
     * @param type the obj type
     * @return a object
     */
    @SuppressWarnings("unchecked")
    public static <T> T deserialize(byte[] value, Class<T> type) {
        if (value == null || value.length == 0) {
            // type.isPrimitive()
            if (boolean.class == type) {
                return (T) Boolean.FALSE;
            } else if (byte.class == type) {
                //return (T) new Byte((byte) 0);
                return (T) (Byte) (byte) 0;
            } else if (short.class == type) {
                return (T) (Short) (short) 0;
            } else if (char.class == type) {
                return (T) (Character) Numbers.CHAR_ZERO;
            } else if (int.class == type) {
                return (T) (Integer) 0;
            } else if (long.class == type) {
                return (T) (Long) 0L;
            } else if (float.class == type) {
                return (T) (Float) 0F;
            } else if (double.class == type) {
                return (T) (Double) 0D;
            } else {
                return null;
            }
        } else if (type == byte[].class) {
            return (T) value;
        } else if (type == Byte[].class) {
            return (T) ArrayUtils.toObject(value);
        } else if (type == InputStream.class) {
            return (T) new ByteArrayInputStream(value);
        }  else if (boolean.class == type || Boolean.class == type) {
            return (T) (value[0] == 0x00 ? Boolean.FALSE : Boolean.TRUE);
        } else if (byte.class == type || Byte.class == type) {
            return (T) (Byte) value[0];
        } else if (short.class == type || Short.class == type) {
            return (T) (Short) Bytes.toShort(value);
        } else if (char.class == type || Character.class == type) {
            return (T) (Character) Bytes.toChar(value);
        } else if (int.class == type || Integer.class == type) {
            return (T) (Integer) Bytes.toInt(value);
        } else if (long.class == type || Long.class == type) {
            return (T) (Long) Bytes.toLong(value);
        } else if (float.class == type || Float.class == type) {
            return (T) (Float) Bytes.toFloat(value);
        } else if (double.class == type || Double.class == type) {
            return (T) (Double) Bytes.toDouble(value);
        } else if (String.class == type) {
            return (T) new String(value, StandardCharsets.UTF_8);
        } else if (Date.class == type) {
            return (T) new Date(Bytes.toLong(value));
        } else if (type.isEnum()) {
            return type.getEnumConstants()[Bytes.toInt(value)];
        } else if (Serializable.class.isAssignableFrom(type)) {
            return SerializationUtils.deserialize(value);
        } else {
            throw new UnsupportedOperationException(
                ClassUtils.getClassName(value.getClass()) + " is not Serializable."
            );
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
           (byte) (most  >>> 56), (byte) (most  >>> 48),
           (byte) (most  >>> 40), (byte) (most  >>> 32),
           (byte) (most  >>> 24), (byte) (most  >>> 16),
           (byte) (most  >>>  8), (byte) (most        ),

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
        return Base64UrlSafe.encode(uuid());
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
     * @param deepPath
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
     * Returns if t is null then other, else t
     *
     * @param obj   the obj
     * @param other the other
     * @return if obj is not null then obj else other
     */
    public static <T> T orElse(T obj, T defaultValue) {
        return orElse(obj, defaultValue, ObjectUtils::isNotNull);
    }

    public static <T> T orElse(T obj, T defaultValue, Predicate<T> predicate) {
        return predicate.test(obj) ? obj : defaultValue;
    }

    public static <T> void copy(T source, T target, String... fields) {
        for (String field : fields) {
            Fields.put(target, field, Fields.get(source, field));
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T copy(T source, String... fields) {
        T target;
        try {
            target = (T) source.getClass().newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        copy(source, target, fields);
        return target;
    }
}
