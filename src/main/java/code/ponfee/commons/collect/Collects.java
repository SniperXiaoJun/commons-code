package code.ponfee.commons.collect;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import code.ponfee.commons.model.Page;
import code.ponfee.commons.model.Result;
import code.ponfee.commons.reflect.Fields;
import code.ponfee.commons.util.ObjectUtils;

public class Collects {

    /**
     * map转数组
     * @param map
     * @param fields
     * @return
     */
    public static Object[] map2array(Map<String, Object> map, String... fields) {
        Object[] array = new Object[fields.length];
        for (int i = 0; i < fields.length; i++) {
            array[i] = ObjectUtils.nullValue(map.get(fields[i]), "");
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
            if (row != null && !row.isEmpty()) {
                results.add(map2array(row, fields));
            }
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
            results[i++] = ObjectUtils.nullValue(entry.getValue(), "");
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
            if (row != null && !row.isEmpty()) {
                results.add(map2array(row));
            }
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
        Result<Page<Object[]>> target = new Result<>(source.getCode(), source.getMsg(), null);
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
        Result<Page<Object[]>> target = new Result<>(source.getCode(), source.getMsg(), null);
        Fields.put(target, "data", page);
        return target;
    }

    /**
     * 求两list的交集
     * @param list1
     * @param list2
     * @return
     */
    public static String[] intersect(List<String> list1, List<String> list2) {
        list1 = Lists.newArrayList(list1);
        list1.retainAll(list2);
        return list1.toArray(new String[list1.size()]);
    }

    /**
     * 数组与list交集
     * @param array
     * @param list
     * @return
     */
    public static String[] intersect(String[] array, List<String> list) {
        return intersect(Lists.newArrayList(array), list);
    }

    /**
     * 两数组交集
     * @param array1
     * @param array2
     * @return
     */
    public static String[] intersect(String[] array1, String[] array2) {
        return intersect(array1, Lists.newArrayList(array2));
    }

    /**
     * 转map
     * @param kv
     * @return
     */
    public static Map<String, Object> ofMap(Object... kv) {
        if (kv.length % 2 != 0) {
            throw new IllegalArgumentException("args must be pair.");
        }
        Map<String, Object> map = Maps.newLinkedHashMap();
        for (int i = 0; i < kv.length; i = i + 2) {
            map.put((String) kv[i], kv[i + 1]);
        }
        return map;
    }

    /**
     * 转数组
     * @param args
     * @return
     */
    public static <T> T[] toArray(@SuppressWarnings("unchecked") T... args) {
        return args;
    }

    /**
     * 合并数组
     * @see org.apache.commons.lang3.ArrayUtils#addAll(T[] array1, T... array2);
     * @param arrays
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] concat(T[]... arrays) {
        if (arrays == null) return null;

        List<T> list = new ArrayList<>();
        Class<?> type = null;
        for (T[] array : arrays) {
            if (array != null) {
                if (type == null) {
                    type = array.getClass().getComponentType();
                }
                list.addAll(Arrays.asList(array));
            }
        }
        if (type == null) {
            return null;
        }

        return list.toArray((T[]) Array.newInstance(type, list.size()));
        //return list.toArray((T[]) new Object[list.size()]); // [Ljava.lang.Object; cannot be cast to [Ljava.lang.String;
    }
}
