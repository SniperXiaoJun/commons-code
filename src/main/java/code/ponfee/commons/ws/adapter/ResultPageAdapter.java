package code.ponfee.commons.ws.adapter;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import code.ponfee.commons.model.Page;
import code.ponfee.commons.model.Result;
import code.ponfee.commons.reflect.GenericUtils;
import code.ponfee.commons.ws.adapter.model.TransitPage;

/**
 * Result<Page<T>>转换器
 * @param <T>
 * 
 * @see java.util.Collections.PageableAdapter
 * 
 * @author fupf
 */
public abstract class ResultPageAdapter<T> extends XmlAdapter<Result<TransitPage<T>>, Result<Page<T>>> {

    protected final Class<T> type;

    protected ResultPageAdapter() {
        type = GenericUtils.getActualTypeArgument(this.getClass());
    }

    @Override
    public Result<Page<T>> unmarshal(Result<TransitPage<T>> v) {
        if (v.getData() == null) {
            return new Result<>(v.getCode(), v.getMsg(), null);
        } else if (v.getData().getRows() == null || v.getData().getRows().getItem() == null) {
            return new Result<>(v.getCode(), v.getMsg(), new Page<>());
        }

        return new Result<>(v.getCode(), v.getMsg(), TransitPage.recover(v.getData()));
    }

    public @Override Result<TransitPage<T>> marshal(Result<Page<T>> v) {
        if (v.getData() == null) {
            return new Result<>(v.getCode(), v.getMsg(), null);
        } else if (v.getData().getRows() == null) {
            return new Result<>(v.getCode(), v.getMsg(), new TransitPage<>());
        }

        return new Result<>(v.getCode(), v.getMsg(), TransitPage.transform(v.getData(), type));
    }

}
