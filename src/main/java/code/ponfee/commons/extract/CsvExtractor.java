package code.ponfee.commons.extract;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.input.BOMInputStream;
import org.apache.commons.lang3.StringUtils;

import code.ponfee.commons.json.Jsons;

/**
 * Csv file data extractor
 * 
 * @author Ponfee
 */
public class CsvExtractor<T> extends DataExtractor<T> {

    public CsvExtractor(InputStream input, String[] headers) {
        super(input, headers, 0, 0);
    }

    public CsvExtractor(InputStream input, String[] headers, 
                        int startRow, long maxFileSize) {
        super(input, headers, startRow, maxFileSize);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void extract(RowProcessor<T> processor) throws IOException {
        try (BOMInputStream bom = new BOMInputStream(input); 
             Reader reader = new InputStreamReader(bom)
        ) {
            Iterable<CSVRecord> records = CSVFormat.DEFAULT.withHeader(headers).parse(reader);
            int i = 0, j, n;
            T data;
            for (CSVRecord redord : records) {
                n = redord.size();
                if (columnNumber == 1) {
                    if (n == 0) {
                        data = (T) StringUtils.EMPTY;
                    } else {
                        data = (T) redord.get(0);
                    }
                } else {
                    String[] array = new String[columnNumber];
                    for (j = 0; j < n && j < columnNumber; j++) {
                        array[j] = redord.get(j);
                    }
                    for (; j < columnNumber; j++) {
                        array[j] = StringUtils.EMPTY;
                    }
                    data = (T) array;
                }
                if (isNotEmpty(data)) {
                    processor.process(i++, data);
                }
            }
        }
    }

    public static void main(String[] args) throws Exception {
        String[] headers = {"区域", "分公司", "项目数"};
        CsvExtractor<String[]> csv = new CsvExtractor<>(new FileInputStream("D:\\csv.csv"), headers);
        System.out.println(Jsons.toJson(csv.extract()));
    }
}
