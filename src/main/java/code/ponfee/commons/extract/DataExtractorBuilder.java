package code.ponfee.commons.extract;

import java.io.IOException;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.io.FilenameUtils;

import com.google.common.collect.ImmutableList;

import code.ponfee.commons.extract.ExcelExtractor.ExcelType;

/**
 * The fiel data extractor builder
 * 
 * @author Ponfee
 */
public class DataExtractorBuilder {

    private static final List<String> EXCEL_EXTENSION = ImmutableList.of("xlsx", "xls");
    private static final List<String> CSV_EXTENSION = ImmutableList.of("csv", "txt");
    private static final String CONTENT_TYPE_TEXT = "text/plain";

    private final Object dataSource;
    private final String fileName;
    private final String contentType;

    private String[] headers;

    private int startRow = 0; // excel start row
    private int sheetIndex = 0; // excel work book sheet index

    private CSVFormat csvFormat; // csv format

    private DataExtractorBuilder(Object dataSource, String fileName, 
                                 String contentType) {
        this.dataSource = dataSource;
        this.fileName = fileName;
        this.contentType = contentType;
    }

    public static DataExtractorBuilder newBuilder(Object dataSource, String fileName, 
                                                  String contentType) {
        return new DataExtractorBuilder(dataSource, fileName, contentType);
    }

    public DataExtractorBuilder headers(String[] headers) {
        this.headers = headers;
        return this;
    }

    public DataExtractorBuilder startRow(int startRow) {
        this.startRow = startRow;
        return this;
    }

    public DataExtractorBuilder sheetIndex(int sheetIndex) {
        this.sheetIndex = sheetIndex;
        return this;
    }

    public DataExtractorBuilder csvFormat(CSVFormat csvFormat) {
        this.csvFormat = csvFormat;
        return this;
    }

    public <T> DataExtractor<T> build() throws IOException {
        String extension = FilenameUtils.getExtension(fileName).toLowerCase();
        if (CONTENT_TYPE_TEXT.equalsIgnoreCase(contentType)
            || CSV_EXTENSION.contains(extension)) {
            // csv, txt文本格式数据
            return new CsvExtractor<>(dataSource, headers, csvFormat);
        } else if (EXCEL_EXTENSION.contains(extension)) {
            // content-type
            // xlsx: application/vnd.openxmlformats-officedocument.wordprocessingml.document
            //       application/vnd.openxmlformats-officedocument.spreadsheetml.sheet
            //
            // xls: application/vnd.ms-excel
            //      application/msword application/x-xls
            return new ExcelExtractor<>(dataSource, headers, startRow,
                                        ExcelType.from(extension), sheetIndex);
        } else {
            throw new RuntimeException("File content type not supported: " + fileName);
        }
    }
}
