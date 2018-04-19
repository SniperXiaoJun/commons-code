package code.ponfee.commons.ws.adapter;

import java.lang.reflect.Array;
import java.util.Set;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import com.google.common.collect.Sets;

import code.ponfee.commons.model.Result;
import code.ponfee.commons.reflect.GenericUtils;
import code.ponfee.commons.ws.adapter.model.ArrayItem;

/**
 * Result<Set<T>>转换器
 * @author fupf
 * @param <T>
 */
public abstract class ResultSetAdapter<T> extends XmlAdapter<Result<ArrayItem<T>>, Result<Set<T>>> {

    protected final Class<T> type;

    protected ResultSetAdapter() {
        type = GenericUtils.getActualTypeArgument(this.getClass());
    }

    @Override
    public Result<Set<T>> unmarshal(Result<ArrayItem<T>> v) {
        if (v.getData() == null) {
            return new Result<>(v.getCode(), v.getMsg(), null);
        } else if (v.getData().getItem() == null) {
            return new Result<>(v.getCode(), v.getMsg(), Sets.newHashSet());
        }

        Set<T> set = Sets.newHashSet(v.getData().getItem());
        return new Result<>(v.getCode(), v.getMsg(), set);
    }

    @SuppressWarnings("unchecked")
    public @Override Result<ArrayItem<T>> marshal(Result<Set<T>> v) {
        if (v.getData() == null) {
            return new Result<>(v.getCode(), v.getMsg(), null);
        }

        T[] array = v.getData().toArray((T[]) Array.newInstance(type, v.getData().size()));
        return new Result<>(v.getCode(), v.getMsg(), new ArrayItem<>(array));
    }

}
