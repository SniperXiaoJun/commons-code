package code.ponfee.commons.extract;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.ImmutableList;

import code.ponfee.commons.io.Files;

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

    private int startRow = 0;
    private long maxFileSize = 0;
    private int sheetIndex = 0; // excel work book sheet index

    private DataExtractorBuilder(InputStream input, String fileName, 
                                 String contentType,String[] headers) {
        this.input = input;
        this.fileName = fileName;
        this.contentType = contentType;
        this.headers = headers;
    }

    public static DataExtractorBuilder newBuilder(InputStream input, String fileName, 
                                                  String contentType,String[] headers) {
        return new DataExtractorBuilder(input, fileName, contentType, headers);
    }

    public DataExtractorBuilder startRow(int startRow) {
        this.startRow = startRow;
        return this;
    }

    public DataExtractorBuilder maxFileSize(long maxFileSize) {
        this.maxFileSize = maxFileSize;
        return this;
    }

    public DataExtractorBuilder sheetIndex(int sheetIndex) {
        this.sheetIndex = sheetIndex;
        return this;
    }

    public <T> DataExtractor<T> build() throws FileTooBigException, IOException {
        String extension = FilenameUtils.getExtension(fileName).toLowerCase();
        //try (InputStream inputStream = input) {
            long fileSize = input.available();
            if (maxFileSize > 0 && fileSize > maxFileSize) {
                throw new FileTooBigException("文件大小：" + Files.human(fileSize) 
                                            + "，已经超过：" + Files.human(maxFileSize));
            }
            if (StringUtils.isBlank(contentType)
                || CONTENT_TYPE_TEXT.equalsIgnoreCase(contentType)
                || CSV_EXTENSION.contains(extension)) {
                // csv, txt文本格式数据
                return new CsvExtractor<>(input, headers, startRow, maxFileSize);
            } else if (EXCEL_EXTENSION.contains(extension)) {
                // content-type
                // xlsx: application/vnd.openxmlformats-officedocument.wordprocessingml.document
                //       application/vnd.openxmlformats-officedocument.spreadsheetml.sheet
                //
                // xls: application/vnd.ms-excel
                //      application/msword application/x-xls
                return new ExcelExtractor<>(input, headers, startRow, maxFileSize, 
                                            ExcelType.from(extension), sheetIndex);
            } else {
                throw new RuntimeException("File content type not supported: " + fileName);
            }
        //} catch (IOException e) {
        //    throw new RuntimeException("文件数据提取出现异常", e);
        //}
    }
}
