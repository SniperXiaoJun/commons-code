package code.ponfee.commons.ws.adapter;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.apache.commons.lang3.StringUtils;

import code.ponfee.commons.json.Jsons;
import code.ponfee.commons.model.Result;
import code.ponfee.commons.reflect.GenericUtils;

/**
 * Result<data> -> Result<json>
 * @author fupf
 * @param <T>
 */
public abstract class ResultDataJsonAdapter<T> extends XmlAdapter<Result<String>, Result<T>> {

    protected final Class<T> type;

    protected ResultDataJsonAdapter() {
        type = GenericUtils.getActualTypeArgument(this.getClass());
    }

    @Override
    public Result<T> unmarshal(Result<String> v) {
        if (StringUtils.isEmpty(v.getData())) {
            return new Result<>(v.getCode(), v.getMsg(), null);
        }

        T data = Jsons.fromJson(v.getData(), type);
        return new Result<>(v.getCode(), v.getMsg(), data);
    }

    @Override
    public Result<String> marshal(Result<T> v) {
        if (v.getData() == null) {
            return new Result<>(v.getCode(), v.getMsg(), null);
        }

        String data = Jsons.toJson(v.getData());
        return new Result<>(v.getCode(), v.getMsg(), data);
    }

}
