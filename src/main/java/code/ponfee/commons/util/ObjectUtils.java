package code.ponfee.commons.util;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import code.ponfee.commons.model.Page;
import code.ponfee.commons.model.Result;
import code.ponfee.commons.reflect.Fields;

/**
 * 公用对象工具类
 * @author fupf
 */
public final class ObjectUtils {

    // 不区分大小写，去掉了1,0,i,o几个容易混淆的字符
    private static final String[] CASE_SENSITIVE = {
        "a", "b", "c", "d", "e", "f", "g", "h", /*"i",*/"j", "k", "l",
        "m", "n", /*"o",*/ "p", "q", "r", "s", "t", "u", "v", "x", "w",
        "y", "z", /*"0", "1",*/ "2", "3", "4", "5", "6", "7", "8", "9",
        "A", "B", "C", "D", "E", "F", "G", "H", /*"I",*/"J", "K", "L",
        "M", "N", /*"O",*/ "P", "Q", "R", "S", "T", "U", "V", "X", "W",
        "Y", "Z" };

    // 纯大写字母加数字
    private static final String[] CASE_IGNORE = {
        "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E",
        "F", "G", "H", "J", "K", "L", "M", "N", "P", "Q", "R", "S", "T",
        "U", "V", "X", "W", "Y", "Z" };

    public static final String[] URL_SAFE_BASE64_CODES = {
        "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M",
        "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z",
        "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m",
        "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z",
        "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "-", "_", "."
    };

    /**
     * 对象toString
     * @param obj
     * @return
     */
    public static String toString(Object obj) {
        return ReflectionToStringBuilder.reflectionToString(obj, ToStringStyle.JSON_STYLE);
        /*if (obj == null) return null;
        StringBuilder builder = new StringBuilder();
        if (obj.getClass().isArray()) {
            int length = Array.getLength(obj);
            Object o = null;
            for (int i = 0; i < length; i++) {
                o = Array.get(obj, i);
                if (o == null) {
                    builder.append("null, ");
                } else if (o.getClass().isArray()) {
                    builder.append(toString(o) + ", ");
                } else {
                    builder.append(Array.get(obj, i) + ", ");
                }
            }
            if (builder.length() > 0) {
                builder.delete(builder.length() - 2, builder.length())
                       .insert(0, "[").append("]");
            }
        } else {
            builder.append(obj);
        }
        return builder.toString();*/
    }

    /**
     * <pre>
     *   递归地比较两个数组是否相同，支持多维数组。
     *   如果比较的对象不是数组，则此方法的结果同
     *   <code>Objects.equals</code>。
     * </pre>
     * 
     * @param obj1
     * @param obj2
     * @return 如果相等, 则返回<code>true</code>
     */
    public static boolean equals(Object obj1, Object obj2) {
        if (obj1 == obj2) return true;
        if (obj1 == null || obj2 == null) return false;

        Class<? extends Object> clazz = obj1.getClass();
        if (!clazz.equals(obj2.getClass())) return false;
        if (!clazz.isArray()) return obj1.equals(obj2);

        // obj1和obj2为同类型的数组
        if (obj1 instanceof long[]) {
            return Arrays.equals((long[]) obj1, (long[]) obj2);
        } else if (obj1 instanceof int[]) {
            return Arrays.equals((int[]) obj1, (int[]) obj2);
        } else if (obj1 instanceof short[]) {
            return Arrays.equals((short[]) obj1, (short[]) obj2);
        } else if (obj1 instanceof byte[]) {
            return Arrays.equals((byte[]) obj1, (byte[]) obj2);
        } else if (obj1 instanceof double[]) {
            return Arrays.equals((double[]) obj1, (double[]) obj2);
        } else if (obj1 instanceof float[]) {
            return Arrays.equals((float[]) obj1, (float[]) obj2);
        } else if (obj1 instanceof boolean[]) {
            return Arrays.equals((boolean[]) obj1, (boolean[]) obj2);
        } else if (obj1 instanceof char[]) {
            return Arrays.equals((char[]) obj1, (char[]) obj2);
        } else {
            return Arrays.equals((Object[]) obj1, (Object[]) obj2);
        }
    }

    /**
     * 合并数组
     * @param arrays
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] concat(T[]... arrays) {
        if (arrays == null) return null;

        List<T> list = new ArrayList<>();
        Class<?> type = null;
        for (T[] array : arrays) {
            if (array == null) continue;
            if (type == null) {
                type = array.getClass().getComponentType();
            }
            list.addAll(Arrays.asList(array));
        }
        if (type == null) return null;

        return list.toArray((T[]) Array.newInstance(type, list.size()));
        //return list.toArray((T[]) new Object[list.size()]); // [Ljava.lang.Object; cannot be cast to [Ljava.lang.String;
        //return list.toArray((T[]) Array.newInstance(list.get(0).getClass(), list.size())); // list.get(0) may be null
    }

    public static byte[] concat(byte[] first, byte[]... rest) {
        if (first == null) {
            throw new IllegalArgumentException("the first array must be non null");
        }
        int totalLength = first.length;
        for (byte[] array : rest) {
            if (array == null) continue;
            totalLength += array.length;
        }
        byte[] result = Arrays.copyOf(first, totalLength);
        int offset = first.length;
        for (byte[] array : rest) {
            if (array == null) continue;
            System.arraycopy(array, 0, result, offset, array.length);
            offset += array.length;
        }
        return result;
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
     * 判断是否为空字符串
     * @param value
     * @return
     */
    public static boolean isEmptyString(Object value) {
        return CharSequence.class.isInstance(value) 
               && StringUtils.isEmpty((CharSequence) value);
    }

    public static Object nullValue(Object obj) {
        return nullValue(obj, "");
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
        String name;
        try {
            for (PropertyDescriptor prop : Introspector.getBeanInfo(bean.getClass()).getPropertyDescriptors()) {
                name = prop.getName();
                if ("class".equals(name)) continue;

                if (map.containsKey(name) || map.containsKey(name = Strings.underscoreName(name))) {
                    Object value = map.get(name);
                    Class<?> type = prop.getPropertyType();

                    if ((value == null || isEmptyString(value)) && type.isPrimitive()) {
                        continue; // 原始类型跳过
                    } else if (isEmptyString(value) && ClassUtils.isPrimitiveWrapper(type)) {
                        value = null; // 包装类型则设置为null
                    }

                    // 字符序列转换
                    if (CharSequence.class.isAssignableFrom(type) && value != null && !type.isInstance(value)) {
                        value = type.getConstructor(String.class).newInstance(value.toString());
                    }

                    // 原始或包装类型
                    if (value != null && ClassUtils.isPrimitiveOrWrapper(type)) {
                        type = ClassUtils.primitiveToWrapper(type); // 转包装类型
                        if (!type.isAssignableFrom(value.getClass())) {
                            value = type.getConstructor(String.class).newInstance(value.toString()); // 字符串值转包装类型
                        }
                    }

                    // setter value into bean field
                    prop.getWriteMethod().invoke(bean, value);
                }
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
            String name;
            for (PropertyDescriptor prop : Introspector.getBeanInfo(bean.getClass()).getPropertyDescriptors()) {
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
     * map转数组
     * @param map
     * @param fields
     * @return
     */
    public static Object[] map2array(Map<String, Object> map, String... fields) {
        Object[] array = new Object[fields.length];
        for (int i = 0; i < fields.length; i++) {
            array[i] = nullValue(map.get(fields[i]));
        }
        return array;
    }

    /**
     * List<Map<String, Object>>转List<Object[]>
     * @param data
     * @param fields
     * @return
     */
    public static List<Object[]> map2array(List<Map<String, Object>> data, String... fields) {
        if (data == null) return null;

        List<Object[]> results = new ArrayList<>(data.size());
        for (Map<String, Object> row : data) {
            if (isEmpty(row)) continue;
            results.add(map2array(row, fields));
        }
        return results;
    }

    /**
     * LinkedHashMap<String, Object>转Object[]
     * @param data
     * @return
     */
    public static Object[] map2array(LinkedHashMap<String, Object> data) {
        if (data == null) return null;

        Object[] results = new Object[data.size()];
        int i = 0;
        for (Entry<String, Object> entry : data.entrySet()) {
            results[i++] = nullValue(entry.getValue());
        }
        return results;
    }

    /**
     * List<LinkedHashMap<String, Object>> -> List<Object[]>
     * @param data
     * @return
     */
    public static List<Object[]> map2array(List<LinkedHashMap<String, Object>> data) {
        if (data == null) return null;

        List<Object[]> results = new ArrayList<>(data.size());
        for (LinkedHashMap<String, Object> row : data) {
            if (row == null) continue;
            results.add(map2array(row));
        }
        return results;
    }

    /**
     * Result<Page<LinkedHashMap<String, Object>>>转Result<Page<Object[]>>
     * @param source
     * @return
     */
    public static Result<Page<Object[]>> map2array(Result<Page<LinkedHashMap<String, Object>>> source) {
        Page<LinkedHashMap<String, Object>> page = source.getData();
        List<Object[]> list = map2array(page.getRows());

        Fields.put(page, "rows", list);
        Result<Page<Object[]>> target = new Result<Page<Object[]>>(source.getCode(), source.getMsg(), null);
        Fields.put(target, "data", page);
        return target;
    }

    /**
     * Result<Page<Map<String, Object>>>转Result<Page<Object[]>>
     * @param source
     * @param fields
     * @return
     */
    public static Result<Page<Object[]>> map2array(Result<Page<Map<String, Object>>> source, String... fields) {
        Page<Map<String, Object>> page = source.getData();
        List<Object[]> list = Lists.newArrayList();
        for (Map<String, Object> map : page.getRows()) {
            list.add(map2array(map, fields));
        }

        Fields.put(page, "rows", list);
        Result<Page<Object[]>> target = new Result<Page<Object[]>>(source.getCode(), source.getMsg(), null);
        Fields.put(target, "data", page);
        return target;
    }

    public static String uuid(int len) {
        return uuid(len, false);
    }

    public static String uuid(int len, boolean caseSensitive) {
        return uuid(len, caseSensitive ? CASE_SENSITIVE : CASE_IGNORE);
    }

    /**
     * should between 3 (inclusive) and 32 (exclusive)
     * @param len
     * @param chars
     * @return
     */
    public static String uuid(int len, String[] chars) {
        int size = chars.length;
        StringBuilder builder = new StringBuilder(len);
        for (String str : Strings.slice(uuid32(), len)) {
            if (isEmpty(str)) continue;
            builder.append(chars[(int) (Long.parseLong(str, 16) % size)]);
        }
        return builder.toString();
    }

    public static String uuid32() {
        return uuid36().replace("-", "");
    }

    public static String uuid36() {
        return UUID.randomUUID().toString();
    }

    /**
     * 求交集
     * @param list1
     * @param list2
     * @return
     */
    public static String[] intersect(List<String> list1, List<String> list2) {
        list1 = Lists.newArrayList(list1);
        list1.retainAll(list2);
        return list1.toArray(new String[list1.size()]);
    }

    public static String[] intersect(String[] array, List<String> list) {
        return intersect(Lists.newArrayList(array), list);
    }

    public static String[] intersect(String[] array1, String[] array2) {
        return intersect(array1, Lists.newArrayList(array2));
    }

    /**
     * 获取堆栈信息
     * @param deep
     * @return
     */
    public static String getStackTrace(int deep) {
        StackTraceElement[] traces = Thread.currentThread().getStackTrace();
        if (traces.length <= deep) {
            return "warning: out of stack trace.";
        }

        StackTraceElement trace = traces[deep];
        return new StringBuilder(trace.getClassName()).append("#").append(trace.getMethodName()).append("(").append(trace.getLineNumber()).append(")").toString();
    }

    /**
     * 转map
     * @param kv
     * @return
     */
    public static Map<String, Object> ofMap(Object... kv) {
        if (kv.length % 2 != 0) {
            throw new IllegalArgumentException("arguments must be pair.");
        }
        Map<String, Object> map = Maps.newLinkedHashMap();
        for (int i = 0; i < kv.length; i = i + 2) {
            map.put((String) kv[i], kv[i + 1]);
        }
        return map;
    }

    public static void main(String[] args) throws IOException {
        int len = 8;
        Set<String> set = new HashSet<>();
        String uuid;
        for (int i = 0; i < 999; i++) {
            uuid = uuid(len);
            if (!set.add(uuid)) {
                System.err.println(uuid);
            }
        }

        System.out.println(toString(Strings.slice("6f2e8fb0df2a4cf29abd382a08ef329e", 33)));
        System.out.println(uuid(8, true));

        String[] s1 = { "1" };
        String[] s2 = { "2" };
        String[] s3 = concat(s1, s2);
        System.out.println(toString(s3));

        System.out.println(toString(new String[][] { { "1", "2", "3" }, { null, "b", "c" } }));

        List<String> list1 = Lists.newArrayList("1", "0", "3", "4", "9", "6");
        List<String> list2 = Lists.newArrayList("1", "2", "9", "3", "9", "8");
        System.out.println(toString(intersect(list1, list2)));
        System.out.println(list1);
        System.out.println(list2);

        System.out.println(Strings.crc32(uuid32()));

        System.out.println(bean2map(new TestBean(1, "zs")));
        System.out.println(map2bean(ImmutableMap.of("age", 1, "firstName", "lisi"), TestBean.class));
        System.out.println(map2bean(ImmutableMap.of("age", 1, "first_name", "lisi"), TestBean.class));

        System.out.println((Long.parseLong("ff", 16)));
        System.out.println(map2bean(ImmutableMap.of("height", "1.0", "first_name", "lisi", "weight", 123.4), TestBean.class));
        System.out.println(String.class.isInstance(null));

        int i = 1;
        Object o = i;
        System.out.println(o.getClass());
    }

    static class TestBean {
        private int age;
        private String weight;
        private double height;
        private String firstName;

        public TestBean() {}

        public TestBean(int age, String firstName) {
            super();
            this.age = age;
            this.firstName = firstName;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public double getHeight() {
            return height;
        }

        public void setHeight(double height) {
            this.height = height;
        }

        public String getWeight() {
            return weight;
        }

        public void setWeight(String weight) {
            this.weight = weight;
        }

        @Override
        public String toString() {
            return "TestBean [age=" + age + ", weight=" + weight + ", height=" + height + ", firstName=" + firstName + "]";
        }

    }
}
