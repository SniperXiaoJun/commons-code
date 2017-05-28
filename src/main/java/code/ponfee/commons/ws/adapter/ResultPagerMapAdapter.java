package code.ponfee.commons.ws.adapter;

import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import code.ponfee.commons.model.Pager;
import code.ponfee.commons.model.Result;
import code.ponfee.commons.reflect.ClassUtils;
import code.ponfee.commons.reflect.Fields;
import code.ponfee.commons.ws.adapter.model.MapEntry;
import code.ponfee.commons.ws.adapter.model.MapItem;
import code.ponfee.commons.ws.adapter.model.TransitPager;

/**
 * Result<Pager<Map<K, V>>>转换器
 * @author fupf
 * @param <K>
 * @param <V>
 */
@SuppressWarnings("unchecked")
public abstract class ResultPagerMapAdapter<K, V> extends XmlAdapter<Result<TransitPager<MapItem>>, Result<Pager<Map<K, V>>>> {

    protected final Class<K> ktype;
    protected final Class<V> vtype;

    protected ResultPagerMapAdapter() {
        ktype = ClassUtils.getClassGenricType(this.getClass(), 0);
        vtype = ClassUtils.getClassGenricType(this.getClass(), 1);
    }

    @Override
    public Result<Pager<Map<K, V>>> unmarshal(Result<TransitPager<MapItem>> v) throws Exception {
        if (v.getData() == null) {
            return new Result<>(v.getCode(), v.getMsg(), null);
        } else if (v.getData().getRows() == null || v.getData().getRows().getItem() == null) {
            return new Result<>(v.getCode(), v.getMsg(), new Pager<>());
        }

        List<Map<K, V>> list = Lists.newArrayList();
        Pager<MapItem> pager = TransitPager.unmarshal(v.getData());
        for (MapItem items : pager.getRows()) {
            if (items == null) continue;
            Map<K, V> map = Maps.newLinkedHashMap();
            for (MapEntry<K, V> item : items.getItem()) {
                map.put(item.getKey(), item.getValue());
            }
            list.add(map);
        }

        Fields.put(pager, "rows", list);
        Result<Pager<Map<K, V>>> result = new Result<>(v.getCode(), v.getMsg(), null);
        Fields.put(result, "data", pager);
        return result;
    }

    @Override
    public Result<TransitPager<MapItem>> marshal(Result<Pager<Map<K, V>>> v) throws Exception {
        if (v.getData() == null || v.getData().getRows() == null) {
            return new Result<>(v.getCode(), v.getMsg(), null);
        }

        List<MapItem> list = Lists.newArrayList();
        Pager<Map<K, V>> pager = v.getData();
        for (Map<K, V> map : pager.getRows()) {
            if (map == null) continue;
            MapEntry<K, V>[] item = new MapEntry[map.size()];
            int index = 0;
            for (Map.Entry<K, V> entry : map.entrySet()) {
                item[index++] = new MapEntry<>(entry);
            }
            list.add(new MapItem(item));
        }

        TransitPager<MapItem> pageData = TransitPager.marshal(pager, list.toArray(new MapItem[list.size()]));
        return new Result<TransitPager<MapItem>>(v.getCode(), v.getMsg(), pageData);
    }

}
