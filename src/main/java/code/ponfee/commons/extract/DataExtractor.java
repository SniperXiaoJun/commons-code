package code.ponfee.commons.extract;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import code.ponfee.commons.util.Holder;

/**
 * The file data extractor
 * 
 * @author Ponfee
 */
public abstract class DataExtractor<T> {

    protected final InputStream input;
    protected final String[] headers;

    protected DataExtractor(InputStream input, String[] headers) {
        this.input = input;
        this.headers = headers;
    }

    public abstract void extract(RowProcessor<T> processor) throws IOException;

    public final List<T> extract() throws IOException {
        List<T> list = new ArrayList<>();
        this.extract((rowNumber, data) -> list.add(data));
        return list;
    }

    public final void extract(int batchSize, Consumer<List<T>> action) throws IOException {
        Holder<List<T>> holder = Holder.of(new ArrayList<>(batchSize));
        this.extract((rowNumber, data) -> {
            List<T> list = holder.get();
            list.add(data);
            if (list.size() == batchSize) {
                action.accept(list);
                holder.set(new ArrayList<>(batchSize));
            }
        });
        if (CollectionUtils.isNotEmpty(holder.get())) {
            action.accept(holder.get());
        }
    }

    /**
     * 验证
     * 
     * @param validator
     * @return
     * @throws IOException
     */
    public final ValidateResult<T> verify(RowValidator<T> validator) throws IOException {
        ValidateResult<T> result = new ValidateResult<>();
        this.extract((rowNumber, data) -> {
            String error = validator.verify(rowNumber, data);
            if (StringUtils.isBlank(error)) {
                result.addData(data);
            } else {
                result.addError("第" + rowNumber + "行错误：" + error);
            }
        });
        return result;
    }

    public boolean isNotEmpty(T data) {
        if (data instanceof String[]) {
            for (String str : (String[]) data) {
                if (StringUtils.isNotBlank(str)) {
                    return true;
                }
            }
            return false;
        } else if (data instanceof String) {
            return StringUtils.isNotBlank((String) data);
        } else {
            return data != null;
        }
    }
}
