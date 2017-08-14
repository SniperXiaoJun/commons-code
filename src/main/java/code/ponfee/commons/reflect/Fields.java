package code.ponfee.commons.reflect;

import java.lang.reflect.Field;

import code.ponfee.commons.model.Result;
import sun.misc.Unsafe;

/**
 * 高效的反射工具类（基于sun.misc.Unsafe）
 * @author fupf
 */
@SuppressWarnings("restriction")
public final class Fields {
    private static final Unsafe UNSAFE/* = Unsafe.getUnsafe()*/;
    static {
        try {
            Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            UNSAFE = (Unsafe) f.get(null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("failed to get unsafe instance", e);
        }
    }

    /**
     * put field to target object
     * @param target 目标对象
     * @param name 字段名
     * @param value 字段值
     */
    public static void put(Object target, String name, Object value) {
        try {
            Field field = ClassUtils.getField(target.getClass(), name);
            put(target, field, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * put field to target object if value is null
     * @param target 目标对象
     * @param name 字段名
     * @param value 字段值
     */
    public static void putIfNull(Object target, String name, Object value) {
        if (get(target, name) != null) {
            put(target, name, value);
        }
    }

    /**
     * put field to target object
     * @param target target object
     * @param field object field
     * @param value field value
     */
    public static void put(Object target, Field field, Object value) {
        field.setAccessible(true);
        long fieldOffset = UNSAFE.objectFieldOffset(field);

        Class<?> type = field.getType();
        if (Boolean.TYPE.equals(type)) {
            UNSAFE.putBoolean(target, fieldOffset, (boolean) value);
        } else if (Byte.TYPE.equals(type)) {
            UNSAFE.putByte(target, fieldOffset, (byte) value);
        } else if (Character.TYPE.equals(type)) {
            UNSAFE.putChar(target, fieldOffset, (char) value);
        } else if (Short.TYPE.equals(type)) {
            UNSAFE.putShort(target, fieldOffset, (short) value);
        } else if (Integer.TYPE.equals(type)) {
            UNSAFE.putInt(target, fieldOffset, (int) value);
        } else if (Long.TYPE.equals(type)) {
            UNSAFE.putLong(target, fieldOffset, (long) value);
        } else if (Double.TYPE.equals(type)) {
            UNSAFE.putDouble(target, fieldOffset, (double) value);
        } else if (Float.TYPE.equals(type)) {
            UNSAFE.putFloat(target, fieldOffset, (float) value);
        } else {
            UNSAFE.putObject(target, fieldOffset, value);
        }
    }

    /**
     * put field to target object if value is null
     * @param target
     * @param field
     * @param value
     */
    public static void putIfNull(Object target, Field field, Object value) {
        if (get(target, field) != null) {
            put(target, field, value);
        }
    }

    /**
     * get field of target object
     * @param target 目标对象
     * @param name field name
     * @return the field value
     */
    public static Object get(Object target, String name) {
        try {
            return get(target, ClassUtils.getField(target.getClass(), name));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * get field of target object
     * @param target 目标对象
     * @param field  字段
     * @return
     */
    public static Object get(Object target, Field field) {
        long fieldOffset = UNSAFE.objectFieldOffset(field);
        Class<?> type = field.getType();
        if (Boolean.TYPE.equals(type)) {
            return UNSAFE.getBoolean(target, fieldOffset);
        } else if (Byte.TYPE.equals(type)) {
            return UNSAFE.getByte(target, fieldOffset);
        } else if (Character.TYPE.equals(type)) {
            return UNSAFE.getChar(target, fieldOffset);
        } else if (Short.TYPE.equals(type)) {
            return UNSAFE.getShort(target, fieldOffset);
        } else if (Integer.TYPE.equals(type)) {
            return UNSAFE.getInt(target, fieldOffset);
        } else if (Long.TYPE.equals(type)) {
            return UNSAFE.getLong(target, fieldOffset);
        } else if (Double.TYPE.equals(type)) {
            return UNSAFE.getDouble(target, fieldOffset);
        } else if (Float.TYPE.equals(type)) {
            return UNSAFE.getFloat(target, fieldOffset);
        } else {
            return UNSAFE.getObject(target, fieldOffset);
        }
    }

    /**
     * 支持volatile语义
     * @param target
     * @param name
     * @return
     */
    public static Object getVolatile(Object target, String name) {
        try {
            return getVolatile(target, ClassUtils.getField(target.getClass(), name));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 支持volatile语义
     * @param target
     * @param field
     * @return
     */
    public static Object getVolatile(Object target, Field field) {
        long fieldOffset = UNSAFE.objectFieldOffset(field);
        Class<?> type = field.getType();
        if (Boolean.TYPE.equals(type)) {
            return UNSAFE.getBooleanVolatile(target, fieldOffset);
        } else if (Byte.TYPE.equals(type)) {
            return UNSAFE.getByteVolatile(target, fieldOffset);
        } else if (Character.TYPE.equals(type)) {
            return UNSAFE.getCharVolatile(target, fieldOffset);
        } else if (Short.TYPE.equals(type)) {
            return UNSAFE.getShortVolatile(target, fieldOffset);
        } else if (Integer.TYPE.equals(type)) {
            return UNSAFE.getIntVolatile(target, fieldOffset);
        } else if (Long.TYPE.equals(type)) {
            return UNSAFE.getLongVolatile(target, fieldOffset);
        } else if (Double.TYPE.equals(type)) {
            return UNSAFE.getDoubleVolatile(target, fieldOffset);
        } else if (Float.TYPE.equals(type)) {
            return UNSAFE.getFloatVolatile(target, fieldOffset);
        } else {
            return UNSAFE.getObjectVolatile(target, fieldOffset);
        }
    }

    public static void main(String[] args) {
        Result<?> result = Result.SUCCESS;
        System.out.println(get(result, "code"));

        Test a = new Test();
        System.out.println(get(a, "i"));
        System.out.println(get(a, "_i"));
        System.out.println(get(a, "c"));
        System.out.println(get(a, "_c"));
        System.out.println(get(a, "b"));
        System.out.println(get(a, "_b"));

        put(a, "i", 12);
        put(a, "_i", 14);

        System.out.println(get(a, "i"));
        System.out.println(get(a, "_i"));
    }

    @SuppressWarnings("unused")
    private static class Test {
        private int _i;
        private Integer i = 0;
        private char _c;
        private Character c;
        private boolean _b;
        private Boolean b;
    }
}
