package code.ponfee.commons.ws.adapter;

import java.util.List;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import code.ponfee.commons.model.Pagination;
import code.ponfee.commons.model.Result;
import code.ponfee.commons.reflect.ClassUtils;
import code.ponfee.commons.ws.adapter.model.TransitPagination;

/**
 * Result<Pagination<T>>转换器
 * @author fupf
 * @param <T>
 */
public abstract class ResultPaginationAdapter<T> extends XmlAdapter<Result<TransitPagination<T>>, Result<Pagination<T>>> {

    protected final Class<T> type;

    protected ResultPaginationAdapter() {
        type = ClassUtils.getClassGenricType(this.getClass());
    }

    @Override
    public Result<Pagination<T>> unmarshal(Result<TransitPagination<T>> v) throws Exception {
        if (v.getData() == null) {
            return new Result<>(v.getCode(), v.getMsg(), null);
        } else if (v.getData().getRows() == null || v.getData().getRows().getItem() == null) {
            return new Result<>(v.getCode(), v.getMsg(), new Pagination<T>());
        }

        return new Result<>(v.getCode(), v.getMsg(), TransitPagination.unmarshal(v.getData()));
    }

    public @Override Result<TransitPagination<T>> marshal(Result<Pagination<T>> v) throws Exception {
        if (v.getData() == null) {
            return new Result<>(v.getCode(), v.getMsg(), null);
        }

        List<T> data = v.getData().getRows();
        if (data == null) {
            return new Result<>(v.getCode(), v.getMsg(), new TransitPagination<T>());
        }
        return new Result<>(v.getCode(), v.getMsg(), TransitPagination.marshal(type, v.getData()));
    }

}
