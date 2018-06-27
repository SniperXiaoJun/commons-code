package code.ponfee.commons.extract;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.input.BOMInputStream;

/**
 * Csv file data extractor
 * 
 * @author Ponfee
 */
public class CsvExtractor<T> extends DataExtractor<T> {

    public CsvExtractor(InputStream input, String[] headers, 
                        int firstDataRow, long maxFileSize) {
        super(input, headers, firstDataRow, maxFileSize);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void extract(RowProcessor<T> processor) throws IOException {
        try (BOMInputStream bom = new BOMInputStream(input); 
             Reader reader = new InputStreamReader(bom)
        ) {
            Iterable<CSVRecord> records = CSVFormat.DEFAULT.withHeader(headers).parse(reader);
            int i = 0;
            for (CSVRecord redord : records) {
                if (columnNumber == 1) {
                    processor.process(i++, (T) redord.get(0));
                } else {
                    String[] array = new String[columnNumber];
                    for (int j = 0; j < columnNumber; j++) {
                        array[j] = redord.get(j);
                    }
                    processor.process(i++, (T) array);
                }
            }
        }
    }

}
