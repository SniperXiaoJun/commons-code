package code.ponfee.commons.extract;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * The fiel data extractor
 * 
 * @author Ponfee
 */
public abstract class DataExtractor<T> {

    protected final InputStream input;
    protected final String[] headers;
    protected long maxFileSize;

    protected DataExtractor(InputStream input, String[] headers, long maxFileSize) {
        this.input = input;
        this.headers = headers;
        this.maxFileSize = maxFileSize;
    }

    public abstract void extract(RowProcessor<T> processor) throws IOException;

    public final List<T> extract() throws IOException {
        List<T> list = new ArrayList<>();
        this.extract((rowNumber, data) -> list.add(data));
        return list;
    }

    /**
     * 验证
     * 
     * @param validator
     * @return
     * @throws IOException
     */
    public final ProcessResult<T> filter(RowValidator<T> validator) throws IOException {
        ProcessResult<T> result = new ProcessResult<>();
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
