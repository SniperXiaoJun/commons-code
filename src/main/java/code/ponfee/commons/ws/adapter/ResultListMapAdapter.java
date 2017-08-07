package code.ponfee.commons.ws.adapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import code.ponfee.commons.model.Result;
import code.ponfee.commons.reflect.GenericUtils;
import code.ponfee.commons.ws.adapter.model.MapEntry;
import code.ponfee.commons.ws.adapter.model.MapItem;
import code.ponfee.commons.ws.adapter.model.MapItemArray;

/**
 * Result<List<Map<K,V>>转换器
 * @author fupf
 * @param <K>
 * @param <V>
 */
@SuppressWarnings("unchecked")
public abstract class ResultListMapAdapter<K, V> extends XmlAdapter<Result<MapItemArray>, Result<List<Map<K, V>>>> {

    protected final Class<K> ktype;
    protected final Class<V> vtype;

    protected ResultListMapAdapter() {
        ktype = GenericUtils.getClassGenricType(this.getClass(), 0);
        vtype = GenericUtils.getClassGenricType(this.getClass(), 1);
    }

    @Override
    public Result<List<Map<K, V>>> unmarshal(Result<MapItemArray> v) throws Exception {
        if (v.getData() == null) {
            return new Result<>(v.getCode(), v.getMsg(), null);
        } else if (v.getData().getItems() == null) {
            return new Result<>(v.getCode(), v.getMsg(), Lists.newArrayList());
        }

        List<Map<K, V>> list = new ArrayList<>();
        for (MapItem items : v.getData().getItems()) {
            if (items == null) continue;
            Map<K, V> map = Maps.newLinkedHashMap();
            for (MapEntry<K, V> item : items.getItem()) {
                if (item == null) continue;
                map.put(item.getKey(), item.getValue());
            }
            list.add(map);
        }
        return new Result<>(v.getCode(), v.getMsg(), list);
    }

    @Override
    public Result<MapItemArray> marshal(Result<List<Map<K, V>>> v) throws Exception {
        if (v.getData() == null) {
            return new Result<>(v.getCode(), v.getMsg(), null);
        }

        MapItem[] items = new MapItem[v.getData().size()];
        int i = 0;
        for (Map<K, V> map : v.getData()) {
            if (map == null) continue;
            MapEntry<K, V>[] item = new MapEntry[map.size()];
            int j = 0;
            for (Entry<K, V> entry : map.entrySet()) {
                item[j++] = new MapEntry<K, V>(entry);
            }
            items[i++] = new MapItem(item);
        }
        return new Result<>(v.getCode(), v.getMsg(), new MapItemArray(items));
    }

}
