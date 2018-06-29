package code.ponfee.commons.extract;

import static org.apache.poi.ss.usermodel.Row.MissingCellPolicy.CREATE_NULL_AS_BLANK;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * Excel file data extractor
 * 
 * @author Ponfee
 */
public class ExcelExtractor<T> extends DataExtractor<T> {

    private final int sheetIndex;
    private final ExcelType type;

    public ExcelExtractor(InputStream inputStream, String[] headers, 
                          int startRow, long maxFileSize, ExcelType type) {
        this(inputStream, headers, startRow, maxFileSize, type, 0);
    }

    public ExcelExtractor(InputStream input, String[] headers, int startRow, 
                          long maxFileSize, ExcelType type, int sheetIndex) {
        super(input, headers, startRow, maxFileSize);
        this.type = type;
        this.sheetIndex = sheetIndex;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void extract(RowProcessor<T> processor) throws IOException {
        try (InputStream stream = input;
             Workbook workbook = readWorkbook(stream)
        ) {
            T data;
            Sheet sheet = workbook.getSheetAt(sheetIndex);
            for (int n = sheet.getLastRowNum(), i = startRow, m, j; i <= n; i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }

                String[] array = columnNumber > 1 ? new String[columnNumber] : null;
                String str = null;
                for (m = row.getLastCellNum(), j = 0; j <= m && j < columnNumber; j++) {
                    str = getCellValueAsString(row.getCell(j, CREATE_NULL_AS_BLANK));
                    if (columnNumber > 1) {
                        array[j] = str;
                    }
                }
                if (columnNumber > 1) {
                    for (; j < columnNumber; j++) {
                        array[j] = StringUtils.EMPTY;
                    }
                    data = (T) array;
                } else {
                    data = (T) str;
                }
                if (isNotEmpty(data)) {
                    processor.process(i, data);
                }
            }
        }
    }

    /**
     * 获取单元格的值
     * 
     * @param cell
     * @return
     */
    private static String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return StringUtils.EMPTY;
        }
        switch (cell.getCellTypeEnum()) {
            case NUMERIC: // 数字
                if (DateUtil.isCellDateFormatted(cell)) {
                    return String.valueOf(cell.getDateCellValue());
                } else {
                    cell.setCellType(CellType.STRING);
                    return cell.getStringCellValue().trim();
                }
            case STRING: // 字符串
                return Objects.toString(cell.getStringCellValue(), "");
            case BOOLEAN: // Boolean
                return Boolean.toString(cell.getBooleanCellValue());
            case FORMULA: // 公式
                //return Objects.toString(cell.getCellFormula(), "");
                cell.setCellType(CellType.STRING);
                return cell.getStringCellValue().trim();
            case BLANK: // 空值
            case ERROR: // 故障
            default:
                return StringUtils.EMPTY;
        }
    }

    private Workbook readWorkbook(InputStream input) throws IOException {
        switch (type) {
            case XLS:
                return new HSSFWorkbook(input);
            case XLSX:
                return new XSSFWorkbook(input);
            default:
                throw new RuntimeException("Unknown excel type: " + type);
        }
    }

    public static void main(String[] args) throws Exception {
        String[] headers = new String[] {"a", "b"};
        //String[] headers = new String[] {"a"};
        //List<String> list = extractData(new FileInputStream("d:/大屏批量配置-data-2.xlsx"), "xlsx", 0, 1);
        ExcelExtractor<String[]> ex = new ExcelExtractor<>(new FileInputStream("d:/abcd.xlsx"), 
                                                           headers, 1, 10000, ExcelType.XLSX, 0);
        for (String[] s : ex.extract()) {
            System.out.println(StringUtils.join(s, ", "));
        }
    }
}
