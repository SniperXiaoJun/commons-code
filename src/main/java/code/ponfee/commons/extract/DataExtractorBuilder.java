package code.ponfee.commons.extract;

import code.ponfee.commons.io.Files;
import com.google.common.collect.ImmutableList;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.io.FilenameUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * The fiel data extractor builder
 * 
 * @author Ponfee
 */
public class DataExtractorBuilder {

    private static final List<String> EXCEL_EXTENSION = ImmutableList.of("xlsx", "xls");
    private static final List<String> CSV_EXTENSION = ImmutableList.of("csv", "txt");
    private static final String CONTENT_TYPE_TEXT = "text/plain";

    private final InputStream input;
    private final String fileName;
    private final String contentType;
    private final String[] headers;

    private long maxFileSize = 0;

    private int startRow = 0; // excel start row
    private int sheetIndex = 0; // excel work book sheet index

    private CSVFormat csvFormat; // csv format

    private DataExtractorBuilder(InputStream input, String fileName, 
                                 String contentType,String[] headers) {
        this.input = input;
        this.fileName = fileName;
        this.contentType = contentType;
        this.headers = headers;
    }

    public static DataExtractorBuilder newBuilder(InputStream input, String fileName, 
                                                  String contentType) {
        return new DataExtractorBuilder(input, fileName, contentType, null);
    }

    public static DataExtractorBuilder newBuilder(InputStream input, String fileName,
                                                  String contentType,String[] headers) {
        return new DataExtractorBuilder(input, fileName, contentType, headers);
    }

    public DataExtractorBuilder maxFileSize(long maxFileSize) {
        this.maxFileSize = maxFileSize;
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

    public <T> DataExtractor<T> build() throws FileTooBigException, IOException {
        String extension = FilenameUtils.getExtension(fileName).toLowerCase();
        long fileSize = input.available();
        if (maxFileSize > 0 && fileSize > maxFileSize) {
            throw new FileTooBigException("文件大小：" + Files.human(fileSize) 
                                        + "，已经超过：" + Files.human(maxFileSize));
        }
        if (CONTENT_TYPE_TEXT.equalsIgnoreCase(contentType)
            || CSV_EXTENSION.contains(extension)) {
            // csv, txt文本格式数据
            return new CsvExtractor<>(input, headers, csvFormat);
        } else if (EXCEL_EXTENSION.contains(extension)) {
            // content-type
            // xlsx: application/vnd.openxmlformats-officedocument.wordprocessingml.document
            //       application/vnd.openxmlformats-officedocument.spreadsheetml.sheet
            //
            // xls: application/vnd.ms-excel
            //      application/msword application/x-xls
            return new ExcelExtractor<>(input, headers, startRow,
                                        ExcelType.from(extension), sheetIndex);
        } else {
            throw new RuntimeException("File content type not supported: " + fileName);
        }
    }
}
