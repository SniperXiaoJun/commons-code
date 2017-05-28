package code.ponfee.commons.util;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.google.common.collect.Lists;

import code.ponfee.commons.model.Pager;
import code.ponfee.commons.model.Result;
import code.ponfee.commons.reflect.Fields;

/**
 * 公用对象工具类
 * @author fupf
 */
public final class ObjectUtils {

    private static final String FOLDER_SEPARATOR = "/";
    private static final String WINDOWS_FOLDER_SEPARATOR = "\\";
    private static final String TOP_PATH = "..";
    private static final String CURRENT_PATH = ".";

    // 不区分大小写，去掉了1,0,i,o几个容易混淆的字符
    private static final String[] CASE_SENSITIVE = { "a", "b", "c", "d", "e", "f", "g", "h", /*"i",*/"j", "k",
        "l", "m", "n", /*"o",*/ "p", "q", "r", "s", "t", "u", "v", "x", "w", "y", "z", /*"0", "1",*/ "2", "3",
        "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F", "G", "H", /*"I",*/"J", "K", "L", "M", "N",
        /*"O",*/ "P", "Q", "R", "S", "T", "U", "V", "X", "W", "Y", "Z" };

    // 纯大写字母加数字
    private static final String[] CASE_IGNORE = { "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D",
        "E", "F", "G", "H", "J", "K", "L", "M", "N", "P", "Q", "R", "S", "T", "U", "V", "X", "W", "Y", "Z" };

    /**
     * 对象toString
     * @param t
     * @return
     */
    public static <T> String toString(T t) {
        return ReflectionToStringBuilder.reflectionToString(t, ToStringStyle.DEFAULT_STYLE);
    }

    /**
     * <pre>
     *   递归地比较两个数组是否相同，支持多维数组。
     *   如果比较的对象不是数组，则此方法的结果同<code>Objects.equals</code>。
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
            if (array == null) {
                continue;
            } else if (type == null) {
                type = array.getClass().getComponentType();
            }
            list.addAll(Arrays.asList(array));
        }
        return list.toArray((T[]) Array.newInstance(type, list.size()));
        //return list.toArray((T[]) new Object[list.size()]); // [Ljava.lang.Object; cannot be cast to [Ljava.lang.String;
        //return list.toArray((T[]) Array.newInstance(list.get(0).getClass(), list.size())); // list.get(0) may null
    }

    public static byte[] concat(byte[] first, byte[]... rest) {
        if (first == null) {
            throw new IllegalArgumentException("the first array must be not null");
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

    public static Object nullValue(Object obj) {
        return nullValue(obj, "");
    }

    public static Object nullValue(Object obj, Object nullDefault) {
        return obj == null ? nullDefault : obj;
    }

    public static String mask(String text, String regex, String replacement) {
        if (text == null) return null;
        return text.replaceAll(regex, replacement);
    }

    /**
     * 遮掩
     * @param text
     * @param start
     * @param num
     * @return
     */
    public static String mask(String text, int start, int num) {
        if (num < 1 || StringUtils.isEmpty(text) || text.length() < start) {
            return text;
        }
        if (start < 0) {
            start = 0;
        }
        if (text.length() < start + num) {
            num = text.length() - start;
        }
        int end = text.length() - start - num;
        String regex = "(\\w{" + start + "})\\w{" + num + "}(\\w{" + end + "})";
        return mask(text, regex, "$1" + StringUtils.repeat("*", num) + "$2");
    }

    /**
     * map to javabean
     * @param map
     * @param bean
     */
    public static <T> void map2bean(Map<String, ?> map, T bean) {
        // 获取类属性
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(bean.getClass());
            // 给 JavaBean 对象的属性赋值  
            for (PropertyDescriptor d : beanInfo.getPropertyDescriptors()) {
                String name = d.getName();
                if (map.containsKey(name)) {
                    try {
                        d.getWriteMethod().invoke(bean, new Object[] { map.get(name) });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (IntrospectionException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T map2bean(Map<String, ?> map, Class<T> type) {
        try {
            T bean = type.getConstructor().newInstance();
            map2bean(map, bean);
            return bean;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * javabean to map
     * @param bean
     * @return
     */
    public static Map<String, Object> bean2map(Object bean) {
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(bean.getClass());
            Map<String, Object> map = new HashMap<>();
            for (PropertyDescriptor d : beanInfo.getPropertyDescriptors()) {
                String name = d.getName();
                if (!name.equals("class")) {
                    try {
                        map.put(name, d.getReadMethod().invoke(bean, new Object[0]));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            return map;
        } catch (IntrospectionException e) {
            throw new RuntimeException(e);
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
    public static List<Object[]> map2array(List<Map<String, Object>> data, String[] fields) {
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
     * Result<Pager<LinkedHashMap<String, Object>>>转Result<Pager<Object[]>>
     * @param source
     * @return
     */
    public static Result<Pager<Object[]>> map2array(Result<Pager<LinkedHashMap<String, Object>>> source) {
        Pager<LinkedHashMap<String, Object>> pager = source.getData();
        List<Object[]> list = map2array(pager.getRows());

        Fields.put(pager, "rows", list);
        Result<Pager<Object[]>> target = new Result<Pager<Object[]>>(source.getCode(), source.getMsg(), null);
        Fields.put(target, "data", pager);
        return target;
    }

    /**
     * Result<Pager<Map<String, Object>>>转Result<Pager<Object[]>>
     * @param source
     * @param fields
     * @return
     */
    public static Result<Pager<Object[]>> map2array(Result<Pager<Map<String, Object>>> source, String... fields) {
        Pager<Map<String, Object>> pager = source.getData();
        List<Object[]> list = Lists.newArrayList();
        for (Map<String, Object> map : pager.getRows()) {
            list.add(map2array(map, fields));
        }

        Fields.put(pager, "rows", list);
        Result<Pager<Object[]>> target = new Result<Pager<Object[]>>(source.getCode(), source.getMsg(), null);
        Fields.put(target, "data", pager);
        return target;
    }

    /**
     * 3≤len≤32
     * @param len
     * @return
     */
    public static String uuid(int len) {
        return uuid(len, false);
    }

    public static String uuid(int len, boolean caseSensitive) {
        String[] chars = caseSensitive ? CASE_SENSITIVE : CASE_IGNORE;
        int size = chars.length;
        StringBuilder builder = new StringBuilder();
        for (String str : slice(uuid32(), len)) {
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
     * 字符串分片
     * @param str
     * @param segment
     * @return
     */
    public static String[] slice(String str, int segment) {
        int[] array = Numbers.sharding(str.length(), segment);
        String[] result = new String[array.length];
        for (int j = 0, i = 0; i < array.length; i++) {
            result[i] = str.substring(j, (j += array[i]));
        }
        return result;
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
     * 获取IEME校验码
     * @param code
     * @return
     */
    public static int generateIEMECode(String code) {
        int checkSum = 0;
        char[] chars = code.toCharArray();
        for (int i = 0, num; i < chars.length; i++) {
            num = chars[i] - '0'; // ascii to num  
            if (i % 2 == 0) {
                checkSum += num; // 1、将奇数位数字相加（从1开始计数）
            } else {
                num *= 2; // 2、将偶数位数字分别乘以2，分别计算个位数和十位数之和（从1开始计数）
                if (num < 10) checkSum += num;
                else checkSum += num - 9;
            }
        }
        return (10 - checkSum % 10) % 10;
    }

    /**
     * Normalize the path by suppressing sequences like "path/.." and inner simple dots.
     * <p>
     * The result is convenient for path comparison. For other uses, notice that Windows separators ("\") are replaced by simple slashes.
     * @param path the original path
     * @return the normalized path
     */
    public static String cleanPath(String path) {
        if (path == null) {
            return null;
        }
        String pathToUse = replace(path, WINDOWS_FOLDER_SEPARATOR, FOLDER_SEPARATOR);

        // Strip prefix from path to analyze, to not treat it as part of the
        // first path element. This is necessary to correctly parse paths like
        // "file:core/../core/io/Resource.class", where the ".." should just
        // strip the first "core" directory while keeping the "file:" prefix.
        int prefixIndex = pathToUse.indexOf(":");
        String prefix = "";
        if (prefixIndex != -1) {
            prefix = pathToUse.substring(0, prefixIndex + 1);
            if (prefix.contains("/")) {
                prefix = "";
            } else {
                pathToUse = pathToUse.substring(prefixIndex + 1);
            }
        }
        if (pathToUse.startsWith(FOLDER_SEPARATOR)) {
            prefix = prefix + FOLDER_SEPARATOR;
            pathToUse = pathToUse.substring(1);
        }

        String[] pathArray = delimitedListToStringArray(pathToUse, FOLDER_SEPARATOR);
        List<String> pathElements = new LinkedList<String>();
        int tops = 0;

        for (int i = pathArray.length - 1; i >= 0; i--) {
            String element = pathArray[i];
            if (CURRENT_PATH.equals(element)) {
                // Points to current directory - drop it.
            } else if (TOP_PATH.equals(element)) {
                // Registering top path found.
                tops++;
            } else {
                if (tops > 0) {
                    // Merging path element with element corresponding to top path.
                    tops--;
                } else {
                    // Normal path element found.
                    pathElements.add(0, element);
                }
            }
        }

        // Remaining top paths need to be retained.
        for (int i = 0; i < tops; i++) {
            pathElements.add(0, TOP_PATH);
        }

        return prefix + collectionToDelimitedString(pathElements, FOLDER_SEPARATOR);
    }

    /**
     * Replace all occurrences of a substring within a string with another string.
     * @param inString String to examine
     * @param oldPattern String to replace
     * @param newPattern String to insert
     * @return a String with the replacements
     */
    public static String replace(String inString, String oldPattern, String newPattern) {
        if (!hasLength(inString) || !hasLength(oldPattern) || newPattern == null) {
            return inString;
        }
        StringBuilder sb = new StringBuilder();
        int pos = 0; // our position in the old string
        int index = inString.indexOf(oldPattern);
        // the index of an occurrence we've found, or -1
        int patLen = oldPattern.length();
        while (index >= 0) {
            sb.append(inString.substring(pos, index));
            sb.append(newPattern);
            pos = index + patLen;
            index = inString.indexOf(oldPattern, pos);
        }
        sb.append(inString.substring(pos));
        // remember to append any characters to the right of a match
        return sb.toString();
    }

    /**
     * Check that the given CharSequence is neither {@code null} nor of length 0. Note: Will return {@code true} for a CharSequence that purely consists of
     * whitespace.
     * <p>
     * 
     * <pre class="code">
     * hasLength(null) = false
     * hasLength("") = false
     * hasLength(" ") = true
     * hasLength("Hello") = true
     * </pre>
     * 
     * @param str the CharSequence to check (may be {@code null})
     * @return {@code true} if the CharSequence is not null and has length
     * @see #hasText(String)
     */
    public static boolean hasLength(CharSequence str) {
        return (str != null && str.length() > 0);
    }

    /**
     * Check that the given String is neither {@code null} nor of length 0. Note: Will return {@code true} for a String that purely consists of whitespace.
     * @param str the String to check (may be {@code null})
     * @return {@code true} if the String is not null and has length
     * @see #hasLength(CharSequence)
     */
    public static boolean hasLength(String str) {
        return hasLength((CharSequence) str);
    }

    /**
     * Convenience method to return a Collection as a delimited (e.g. CSV) String. E.g. useful for {@code toString()} implementations.
     * @param coll the Collection to display
     * @param delim the delimiter to use (probably a ",")
     * @param prefix the String to start each element with
     * @param suffix the String to end each element with
     * @return the delimited String
     */
    public static String collectionToDelimitedString(Collection<?> coll, String delim, String prefix, String suffix) {
        if (coll == null || coll.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        Iterator<?> it = coll.iterator();
        while (it.hasNext()) {
            sb.append(prefix).append(it.next()).append(suffix);
            if (it.hasNext()) {
                sb.append(delim);
            }
        }
        return sb.toString();
    }

    /**
     * Convenience method to return a Collection as a delimited (e.g. CSV) String. E.g. useful for {@code toString()} implementations.
     * @param coll the Collection to display
     * @param delim the delimiter to use (probably a ",")
     * @return the delimited String
     */
    public static String collectionToDelimitedString(Collection<?> coll, String delim) {
        return collectionToDelimitedString(coll, delim, "", "");
    }

    /**
     * Take a String which is a delimited list and convert it to a String array.
     * <p>
     * A single delimiter can consists of more than one character: It will still be considered as single delimiter string, rather than as bunch of potential
     * delimiter characters - in contrast to {@code tokenizeToStringArray}.
     * @param str the input String
     * @param delimiter the delimiter between elements (this is a single delimiter, rather than a bunch individual delimiter characters)
     * @return an array of the tokens in the list
     * @see #tokenizeToStringArray
     */
    public static String[] delimitedListToStringArray(String str, String delimiter) {
        return delimitedListToStringArray(str, delimiter, null);
    }

    /**
     * Take a String which is a delimited list and convert it to a String array.
     * <p>
     * A single delimiter can consists of more than one character: It will still be considered as single delimiter string, rather than as bunch of potential
     * delimiter characters - in contrast to {@code tokenizeToStringArray}.
     * @param str the input String
     * @param delimiter the delimiter between elements (this is a single delimiter, rather than a bunch individual delimiter characters)
     * @param charsToDelete a set of characters to delete. Useful for deleting unwanted line breaks: e.g. "\r\n\f" will delete all new lines and line feeds in a
     *        String.
     * @return an array of the tokens in the list
     * @see #tokenizeToStringArray
     */
    public static String[] delimitedListToStringArray(String str, String delimiter, String charsToDelete) {
        if (str == null) {
            return new String[0];
        }
        if (delimiter == null) {
            return new String[] { str };
        }
        List<String> result = new ArrayList<String>();
        if ("".equals(delimiter)) {
            for (int i = 0; i < str.length(); i++) {
                result.add(deleteAny(str.substring(i, i + 1), charsToDelete));
            }
        } else {
            int pos = 0;
            int delPos;
            while ((delPos = str.indexOf(delimiter, pos)) != -1) {
                result.add(deleteAny(str.substring(pos, delPos), charsToDelete));
                pos = delPos + delimiter.length();
            }
            if (str.length() > 0 && pos <= str.length()) {
                // Add rest of String, but not in case of empty input.
                result.add(deleteAny(str.substring(pos), charsToDelete));
            }
        }
        return toStringArray(result);
    }

    /**
     * Delete any character in a given String.
     * @param inString the original String
     * @param charsToDelete a set of characters to delete. E.g. "az\n" will delete 'a's, 'z's and new lines.
     * @return the resulting String
     */
    public static String deleteAny(String inString, String charsToDelete) {
        if (!hasLength(inString) || !hasLength(charsToDelete)) {
            return inString;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < inString.length(); i++) {
            char c = inString.charAt(i);
            if (charsToDelete.indexOf(c) == -1) {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * Copy the given Collection into a String array. The Collection must contain String elements only.
     * @param collection the Collection to copy
     * @return the String array ({@code null} if the passed-in Collection was {@code null})
     */
    public static String[] toStringArray(Collection<String> collection) {
        if (collection == null) {
            return null;
        }
        return collection.toArray(new String[collection.size()]);
    }

    /**
     * Copy the given Enumeration into a String array. The Enumeration must contain String elements only.
     * @param enumeration the Enumeration to copy
     * @return the String array ({@code null} if the passed-in Enumeration was {@code null})
     */
    public static String[] toStringArray(Enumeration<String> enumeration) {
        if (enumeration == null) {
            return null;
        }
        List<String> list = Collections.list(enumeration);
        return list.toArray(new String[list.size()]);
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

        System.out.println(toString(slice("6f2e8fb0df2a4cf29abd382a08ef329e", 33)));
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
    }
}
