package code.ponfee.commons.extract;

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
    protected final int columnNumber;
    protected int firstDataRow;
    protected long maxFileSize;

    protected DataExtractor(InputStream input, String[] headers, 
                            int firstDataRow, long maxFileSize) {
        this.input = input;
        this.headers = headers;
        this.columnNumber = headers.length;
        this.firstDataRow = firstDataRow;
        this.maxFileSize = maxFileSize;
    }

    protected abstract void extract(RowProcessor<T> processor) throws IOException;

    public final List<T> extract() throws IOException {
        List<T> list = new ArrayList<>();
        extract((rowNumber, data) -> list.add((T) data));
        return list;
    }

}
