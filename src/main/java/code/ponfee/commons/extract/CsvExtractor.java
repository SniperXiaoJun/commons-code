package code.ponfee.commons.extract;

import code.ponfee.commons.json.Jsons;
import code.ponfee.commons.util.ObjectUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.input.BOMInputStream;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * Csv file data extractor
 * 
 * @author Ponfee
 */
public class CsvExtractor<T> extends DataExtractor<T> {

    private final CSVFormat csvFormat;
    private final boolean specHeaders;

    public CsvExtractor(InputStream input, String[] headers) {
        this(input, headers, null);
    }

    public CsvExtractor(InputStream input, String[] headers, CSVFormat csvFormat) {
        super(input, headers);
        this.specHeaders = ArrayUtils.isNotEmpty(headers);
        csvFormat = ObjectUtils.orElse(csvFormat, CSVFormat.DEFAULT);
        this.csvFormat = this.specHeaders
                         ? csvFormat.withHeader(headers)
                         : csvFormat.withFirstRecordAsHeader();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void extract(RowProcessor<T> processor) throws IOException {
        try (InputStream stream = input;
             BOMInputStream bom = new BOMInputStream(stream);
             Reader reader = new InputStreamReader(bom)
        ) {
            int columnSize = specHeaders ? this.headers.length : 0;
            Iterable<CSVRecord> records = csvFormat.parse(reader);
            int i = 0, j, n;
            T data;
            for (CSVRecord record : records) {
                if (!specHeaders && i == 0) {
                    columnSize = record.size(); // 不指定表头，则取第一行数据为表头
                }
                n = record.size();
                if (columnSize == 1) {
                    if (n == 0) {
                        data = (T) StringUtils.EMPTY;
                    } else {
                        data = (T) record.get(0);
                    }
                } else {
                    String[] array = new String[columnSize];
                    for (j = 0; j < n && j < columnSize; j++) {
                        array[j] = record.get(j);
                    }
                    for (; j < columnSize; j++) {
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
        //String[] headers = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18"};
        String[] headers = {"1", "2"};
        //String[] headers = null;
        String file = "D:\\testExcel1.csv";
        //String file = "D:\\国际组织10-单位模板20180718 - 副本.csv";
        CsvExtractor<String[]> csv = new CsvExtractor<>(new FileInputStream(file), headers);
        System.out.println(Jsons.toJson(csv.extract()));
    }
}
