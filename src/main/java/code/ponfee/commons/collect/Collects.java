package code.ponfee.commons.collect;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.poi.ss.formula.functions.T;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import code.ponfee.commons.model.Page;
import code.ponfee.commons.model.Result;
import code.ponfee.commons.reflect.Fields;
import code.ponfee.commons.util.ObjectUtils;

/**
 * 集合工具类
 * @author Ponfee
 */
public final class Collects {
    private Collects() {}

    /**
     * map转数组
     * @param map
     * @param fields
     * @return
     */
    public static Object[] map2array(Map<String, Object> map, String... fields) {
        Object[] array = new Object[fields.length];
        for (int i = 0; i < fields.length; i++) {
            array[i] = ObjectUtils.orElse(map.get(fields[i]), "");
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
        if (data == null) {
            return null;
        }

        List<Object[]> result = new ArrayList<>(data.size());
        for (Map<String, Object> row : data) {
            if (row != null && !row.isEmpty()) {
                result.add(map2array(row, fields));
            }
        }
        return result;
    }

    /**
     * LinkedHashMap<String, Object>转Object[]
     * @param data
     * @return
     */
    public static Object[] map2array(LinkedHashMap<String, Object> data) {
        if (data == null) {
            return null;
        }

        Object[] result = new Object[data.size()];
        int i = 0;
        for (Entry<String, Object> entry : data.entrySet()) {
            result[i++] = ObjectUtils.orElse(entry.getValue(), "");
        }
        return result;
    }

    /**
     * List<LinkedHashMap<String, Object>> -> List<Object[]>
     * @param data
     * @return
     */
    public static List<Object[]> map2array(List<LinkedHashMap<String, Object>> data) {
        if (data == null) {
            return null;
        }

        List<Object[]> result = new ArrayList<>(data.size());
        for (LinkedHashMap<String, Object> row : data) {
            if (row != null && !row.isEmpty()) {
                result.add(map2array(row));
            }
        }
        return result;
    }

    /**
     * Result<Page<LinkedHashMap<String, Object>>>转Result<Page<Object[]>>
     * @param s
     * @return
     */
    public static Result<Page<Object[]>> map2array(Result<Page<LinkedHashMap<String, Object>>> s) {
        Page<LinkedHashMap<String, Object>> page = s.getData();
        List<Object[]> list = map2array(page.getRows());

        Fields.put(page, "rows", list);
        Result<Page<Object[]>> target = s.copy(null);
        Fields.put(target, "data", page);
        return target;
    }

    /**
     * Result<Page<Map<String, Object>>>转Result<Page<Object[]>>
     * @param source
     * @param fields
     * @return
     */
    public static Result<Page<Object[]>> map2array(Result<Page<Map<String, Object>>> source, 
                                                   String... fields) {
        Page<Map<String, Object>> page = source.getData();
        List<Object[]> list = Lists.newArrayListWithCapacity(page.getRows().size());
        for (Map<String, Object> map : page.getRows()) {
            list.add(map2array(map, fields));
        }

        Fields.put(page, "rows", list);
        Result<Page<Object[]>> target = source.copy(null);
        Fields.put(target, "data", page);
        return target;
    }

    /**
     * 指定对象字段fieldA的值作为key，字段fieldB的值作为value
     * 
     * @param bean
     * @param keyField
     * @param valueField
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <K, V, E> Map<K, V> flatMap(E bean, String keyField, String valueField) {
        if (bean == null) {
            return null;
        }
        return ImmutableMap.of((K) Fields.get(bean, keyField), (V) Fields.get(bean, valueField));
    }

    @SuppressWarnings("unchecked")
    public static <K, V, E> Map<K, V> flatMap(List<E> beans, String keyField, String valueField) {
        if (beans == null) {
            return null;
        }
        Map<K, V> map = new HashMap<>();
        for (E bean : beans) {
            map.put((K) Fields.get(bean, keyField), (V) Fields.get(bean, valueField));
        }
        return map;
    }

    /**
     * 获取对象指定字段的值
     * 
     * @param beans
     * @param field
     * @return
     */
    @SuppressWarnings({ "unchecked", "hiding" })
    public static <T, E> List<T> flatList(List<E> beans, String field) {
        if (beans == null) {
            return null;
        }
        List<T> list = new ArrayList<>(beans.size());
        for (E bean : beans) {
            list.add((T) Fields.get(bean, field));
        }
        return list;
    }

    public static <E> List<Object[]> flatList(List<E> beans, String... fields) {
        if (beans == null || fields == null || fields.length == 0) {
            return null;
        }

        List<Object[]> list = new ArrayList<>(beans.size());
        for (E bean : beans) {
            Object[] array = new Object[fields.length];
            for (int i = 0; i < fields.length; i++) {
                array[i] = Fields.get(bean, fields[i]);
            }
            list.add(array);
        }
        return list;
    }

    /**
     * 将对象指定字段转为map
     * 
     * @param bean
     * @param fields
     * @return
     */
    public static <E> Map<String, Object> ofMap(E bean, String... fields) {
        if (bean == null || fields == null) {
            return null;
        }
        Map<String, Object> result = new HashMap<>(fields.length);
        for (String field : fields) {
            result.put(field, Fields.get(bean, field));
        }
        return result;
    }

    // -----------------------------the collection of intersect, union and different operations
    /**
     * 求两list的交集
     * intersect([1,2,3], [2,3,4]) = [2,3]
     * @param list1
     * @param list2
     * @return
     */
    @SuppressWarnings({ "unchecked", "hiding" })
    public static <T> T[] intersect(List<T> list1, List<T> list2) {
        //list1.retainAll(list2);
        return (T[]) list1.stream().filter(list2::contains).toArray();
    }

    /**
     * 数组与list交集
     * @param array
     * @param list
     * @return
     */
    @SuppressWarnings({ "unchecked", "hiding" })
    public static <T> T[] intersect(T[] array, List<T> list) {
        return (T[]) Stream.of(array).filter(list::contains).toArray();
    }

    /**
     * two list union result
     * @param list1
     * @param list2
     * @return
     */
    @SuppressWarnings({ "unchecked", "hiding" })
    public static <T> T[] union(List<T> list1, List<T> list2) {
        list1 = Lists.newArrayList(list1);
        list1.addAll(list2);
        return (T[]) list1.stream().distinct().toArray();
    }

    /**
     * list差集
     * different([1,2,3], [2,3,4]) = [1,4]
     *
     * @param list1
     * @param list2
     * @return
     */
    @SuppressWarnings({ "hiding" })
    public static <T> List<T> different(List<T> list1, List<T> list2) {
        List<T> list = list1.stream().filter(t -> !list2.contains(t))
                                     .collect(Collectors.toList());

        list.addAll(list2.stream().filter(t -> !list1.contains(t))
                                  .collect(Collectors.toList()));

        return list;
    }

    /**
     * The two set different elements
     * 
     * @param set1
     * @param set2
     * @return
     */
    @SuppressWarnings("hiding")
    public static <T> Set<T> different(Set<T> set1, Set<T> set2) {
        Set<T> diffSet = Sets.newHashSet(Sets.difference(set1, set2));
        diffSet.addAll(Sets.difference(set2, set1));
        return diffSet;
    }

    /**
     * map差集
     * @param map1
     * @param map2
     * @return
     */
    public static <K, V> Map<K, V> different(Map<K, V> map1, Map<K, V> map2) {
        Set<K> set1 = map1.keySet();
        Set<K> set2 = map2.keySet();
        Set<K> diffSet = Sets.newHashSet(Sets.difference(set1, set2));
        diffSet.addAll(Sets.difference(set2, set1));
        Map<K, V> result = Maps.newHashMapWithExpectedSize(diffSet.size());
        for (K key : diffSet) {
            if (map1.containsKey(key)) {
                result.put(key, map1.get(key));
            } else {
                result.put(key, map2.get(key));
            }
        }
        return result;
    }

    /**
     * 转map
     * @param kv
     * @return
     */
    public static Map<String, Object> toMap(Object... kv) {
        if (kv == null) {
            return null;
        }

        int length = kv.length;
        if ((length & 0x01) != 0) { // length % 2
            throw new IllegalArgumentException("args must be pair.");
        }

        Map<String, Object> map = new LinkedHashMap<>(length / 2);
        for (int i = 0; i < length; i += 2) {
            map.put((String) kv[i], kv[i + 1]);
        }
        return map;
    }

    /**
     * 转数组
     * @param args
     * @return
     */
    public static T[] toArray(T... args) {
        return args;
    }

    /**
     * object to list
     * @param obj of elements
     * @return list with the same elements
     */
    public static List<Object> toList(Object obj) {
        if (obj == null) {
            return null;
        } else if (!obj.getClass().isArray()) {
            return Collections.singletonList(obj);
        } else {
            int length = Array.getLength(obj);
            List<Object> result = new ArrayList<>(length);
            for (int i = 0; i < length; i++) {
                result.add(Array.get(obj, i));
            }
            return result;
        }
    }

    /**
     * 合并数组
     * @see org.apache.commons.lang3.ArrayUtils#addAll(T[] array1, T... array2)
     * @param arrays
     * @return The new array of merged
     */
    @SuppressWarnings({ "unchecked", "hiding" })
    public static <T> T[] concat(T[]... arrays) {
        if (arrays == null) {
            return null;
        }

        List<T> list = new ArrayList<>();
        Class<?> type = null;
        for (T[] array : arrays) {
            if (array == null) continue;

            if (type == null) {
                type = array.getClass().getComponentType();
            }
            list.addAll(Arrays.asList(array));
        }
        if (type == null) {
            return null;
        }

        return list.toArray((T[]) Array.newInstance(type, list.size()));

        // [Ljava.lang.Object; cannot be cast to [Ljava.lang.String;
        //return list.toArray((T[]) new Object[list.size()]);
    }

    /**
     * Returns the ImmutableList of merged collection and object
     * 
     * @param coll
     * @param obj
     * @return
     */
    public static <E> List<E> add(Collection<E> coll, E obj) {
        ImmutableList.Builder<E> builder = ImmutableList.builder();
        if (coll != null) {
            builder.addAll(coll);
        }
        builder.add(obj);
        return builder.build();
    }

}
