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

    private static final List<String> EXCEL_EXT = ImmutableList.of("xlsx", "xls");
    private static final List<String> CSV_EXT = ImmutableList.of("csv", "txt");
    private static final String CONTENT_TYPE_TEXT = "text/plain";

    private final InputStream input;
    private final String fileName;
    private final String contentType;
    private final String[] headers;
    private int firstDataRow;
    private long maxFileSize;

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

    public DataExtractorBuilder firstDataRow(int firstDataRow) {
        this.firstDataRow = firstDataRow;
        return this;
    }

    public DataExtractorBuilder maxFileSize(long maxFileSize) {
        this.maxFileSize = maxFileSize;
        return this;
    }

    public <T> DataExtractor<T> build() {
        String extension = FilenameUtils.getExtension(fileName).toLowerCase();
        try (InputStream inputStream = input) {
            long fileSize = inputStream.available();
            if (maxFileSize > 0 && fileSize > maxFileSize) {
                throw new FileTooBigException("文件过大：" + Files.human(fileSize) 
                                            + "，不能超过：" + Files.human(maxFileSize));
            }
            if (StringUtils.isBlank(contentType)
                || CONTENT_TYPE_TEXT.equalsIgnoreCase(contentType)
                || CSV_EXT.contains(extension)) {
                // csv, txt文本格式数据
                return new CsvExtractor<>(inputStream, headers, firstDataRow, maxFileSize);
            } else if (EXCEL_EXT.contains(extension)) {
                // content-type
                // xlsx: application/vnd.openxmlformats-officedocument.wordprocessingml.document
                //       application/vnd.openxmlformats-officedocument.spreadsheetml.sheet
                //
                // xls: application/vnd.ms-excel
                //      application/msword application/x-xls
                return new ExcelExtractor<>(inputStream, headers, firstDataRow, maxFileSize, 
                                            ExcelType.from(extension), 0);
            } else {
                throw new RuntimeException("File content type not supported: " + fileName);
            }
        } catch (IOException e) {
            throw new RuntimeException("文件数据提取出现异常", e);
        }
    }
}
