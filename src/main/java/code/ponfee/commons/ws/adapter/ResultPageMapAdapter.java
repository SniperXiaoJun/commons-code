package code.ponfee.commons.ws.adapter;

import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import code.ponfee.commons.model.Page;
import code.ponfee.commons.model.Result;
import code.ponfee.commons.reflect.Fields;
import code.ponfee.commons.reflect.GenericUtils;
import code.ponfee.commons.ws.adapter.model.MapEntry;
import code.ponfee.commons.ws.adapter.model.MapItem;
import code.ponfee.commons.ws.adapter.model.TransitPage;

/**
 * Result<Page<Map<K, V>>>转换器
 * @param <K>
 * @param <V>
 * 
 * @see java.util.Collections.PageableAdapter
 * 
 * @author fupf
 */
@SuppressWarnings("unchecked")
public abstract class ResultPageMapAdapter<K, V> extends XmlAdapter<Result<TransitPage<MapItem>>, Result<Page<Map<K, V>>>> {

    protected final Class<K> ktype;
    protected final Class<V> vtype;

    protected ResultPageMapAdapter() {
        ktype = GenericUtils.getActualTypeArgument(this.getClass(), 0);
        vtype = GenericUtils.getActualTypeArgument(this.getClass(), 1);
    }

    @Override
    public Result<Page<Map<K, V>>> unmarshal(Result<TransitPage<MapItem>> v) {
        if (v.getData() == null) {
            return new Result<>(v.getCode(), v.getMsg(), null);
        } else if (v.getData().getRows() == null || v.getData().getRows().getItem() == null) {
            return new Result<>(v.getCode(), v.getMsg(), new Page<>());
        }

        List<Map<K, V>> list = Lists.newArrayList();
        Page<MapItem> page = TransitPage.recover(v.getData());
        for (MapItem items : page.getRows()) {
            if (items == null) {
                continue;
            }
            Map<K, V> map = Maps.newLinkedHashMap();
            for (MapEntry<K, V> item : items.getItem()) {
                map.put(item.getKey(), item.getValue());
            }
            list.add(map);
        }

        Fields.put(page, "rows", list);
        Result<Page<Map<K, V>>> result = new Result<>(v.getCode(), v.getMsg(), null);
        Fields.put(result, "data", page);
        return result;
    }

    @Override
    public Result<TransitPage<MapItem>> marshal(Result<Page<Map<K, V>>> v) {
        if (v.getData() == null || v.getData().getRows() == null) {
            return new Result<>(v.getCode(), v.getMsg(), null);
        }

        List<MapItem> list = Lists.newArrayList();
        Page<Map<K, V>> page = v.getData();
        for (Map<K, V> map : page.getRows()) {
            if (map == null) {
                continue;
            }
            MapEntry<K, V>[] item = new MapEntry[map.size()];
            int index = 0;
            for (Map.Entry<K, V> entry : map.entrySet()) {
                item[index++] = new MapEntry<>(entry);
            }
            list.add(new MapItem(item));
        }

        TransitPage<MapItem> pageData = TransitPage.transform(page, list.toArray(new MapItem[list.size()]));
        return new Result<>(v.getCode(), v.getMsg(), pageData);
    }

}
