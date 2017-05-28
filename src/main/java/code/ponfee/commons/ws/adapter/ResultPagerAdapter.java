package code.ponfee.commons.ws.adapter;

import java.util.List;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import code.ponfee.commons.model.Pager;
import code.ponfee.commons.model.Result;
import code.ponfee.commons.reflect.ClassUtils;
import code.ponfee.commons.ws.adapter.model.TransitPager;

/**
 * Result<Pager<T>>转换器
 * @author fupf
 * @param <T>
 */
public abstract class ResultPagerAdapter<T> extends XmlAdapter<Result<TransitPager<T>>, Result<Pager<T>>> {

    protected final Class<T> type;

    protected ResultPagerAdapter() {
        type = ClassUtils.getClassGenricType(this.getClass());
    }

    @Override
    public Result<Pager<T>> unmarshal(Result<TransitPager<T>> v) throws Exception {
        if (v.getData() == null) {
            return new Result<>(v.getCode(), v.getMsg(), null);
        } else if (v.getData().getRows() == null || v.getData().getRows().getItem() == null) {
            return new Result<>(v.getCode(), v.getMsg(), new Pager<T>());
        }

        return new Result<>(v.getCode(), v.getMsg(), TransitPager.unmarshal(v.getData()));
    }

    public @Override Result<TransitPager<T>> marshal(Result<Pager<T>> v) throws Exception {
        if (v.getData() == null) {
            return new Result<>(v.getCode(), v.getMsg(), null);
        }

        List<T> data = v.getData().getRows();
        if (data == null) {
            return new Result<>(v.getCode(), v.getMsg(), new TransitPager<T>());
        }
        return new Result<>(v.getCode(), v.getMsg(), TransitPager.marshal(type, v.getData()));
    }

}
