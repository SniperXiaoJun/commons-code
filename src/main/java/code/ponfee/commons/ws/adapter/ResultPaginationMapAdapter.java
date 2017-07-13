package code.ponfee.commons.ws.adapter;

import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import code.ponfee.commons.model.Pagination;
import code.ponfee.commons.model.Result;
import code.ponfee.commons.reflect.ClassUtils;
import code.ponfee.commons.reflect.Fields;
import code.ponfee.commons.ws.adapter.model.MapEntry;
import code.ponfee.commons.ws.adapter.model.MapItem;
import code.ponfee.commons.ws.adapter.model.TransitPagination;

/**
 * Result<Pagination<Map<K, V>>>转换器
 * @author fupf
 * @param <K>
 * @param <V>
 */
@SuppressWarnings("unchecked")
public abstract class ResultPaginationMapAdapter<K, V> extends XmlAdapter<Result<TransitPagination<MapItem>>, Result<Pagination<Map<K, V>>>> {

    protected final Class<K> ktype;
    protected final Class<V> vtype;

    protected ResultPaginationMapAdapter() {
        ktype = ClassUtils.getClassGenricType(this.getClass(), 0);
        vtype = ClassUtils.getClassGenricType(this.getClass(), 1);
    }

    @Override
    public Result<Pagination<Map<K, V>>> unmarshal(Result<TransitPagination<MapItem>> v) throws Exception {
        if (v.getData() == null) {
            return new Result<>(v.getCode(), v.getMsg(), null);
        } else if (v.getData().getRows() == null || v.getData().getRows().getItem() == null) {
            return new Result<>(v.getCode(), v.getMsg(), new Pagination<>());
        }

        List<Map<K, V>> list = Lists.newArrayList();
        Pagination<MapItem> pagination = TransitPagination.unmarshal(v.getData());
        for (MapItem items : pagination.getRows()) {
            if (items == null) continue;
            Map<K, V> map = Maps.newLinkedHashMap();
            for (MapEntry<K, V> item : items.getItem()) {
                map.put(item.getKey(), item.getValue());
            }
            list.add(map);
        }

        Fields.put(pagination, "rows", list);
        Result<Pagination<Map<K, V>>> result = new Result<>(v.getCode(), v.getMsg(), null);
        Fields.put(result, "data", pagination);
        return result;
    }

    @Override
    public Result<TransitPagination<MapItem>> marshal(Result<Pagination<Map<K, V>>> v) throws Exception {
        if (v.getData() == null || v.getData().getRows() == null) {
            return new Result<>(v.getCode(), v.getMsg(), null);
        }

        List<MapItem> list = Lists.newArrayList();
        Pagination<Map<K, V>> pagination = v.getData();
        for (Map<K, V> map : pagination.getRows()) {
            if (map == null) continue;
            MapEntry<K, V>[] item = new MapEntry[map.size()];
            int index = 0;
            for (Map.Entry<K, V> entry : map.entrySet()) {
                item[index++] = new MapEntry<>(entry);
            }
            list.add(new MapItem(item));
        }

        TransitPagination<MapItem> pageData = TransitPagination.marshal(pagination, list.toArray(new MapItem[list.size()]));
        return new Result<TransitPagination<MapItem>>(v.getCode(), v.getMsg(), pageData);
    }

}
