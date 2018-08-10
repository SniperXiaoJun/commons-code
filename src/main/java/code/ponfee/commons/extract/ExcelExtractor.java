package code.ponfee.commons.extract;

import static org.apache.poi.ss.usermodel.Row.MissingCellPolicy.CREATE_NULL_AS_BLANK;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import org.apache.commons.lang3.ArrayUtils;
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
    private final int startRow;

    public ExcelExtractor(InputStream inputStream, String[] headers, 
                          int startRow, ExcelType type) {
        this(inputStream, headers, startRow, type, 0);
    }

    public ExcelExtractor(InputStream input, String[] headers, int startRow, 
                          ExcelType type, int sheetIndex) {
        super(input, headers);
        this.startRow = startRow;
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

            boolean specHeaders;
            int columnSize;
            if (ArrayUtils.isNotEmpty(headers)) {
                specHeaders = true;
                columnSize = this.headers.length;
            } else {
                specHeaders = false;
                columnSize = 0;
            }

            for (int n = sheet.getLastRowNum(), i = startRow, k = i, m, j; i <= n; i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }

                if (!specHeaders && k == startRow) {
                    columnSize = row.getLastCellNum(); // 不指定表头则以开始行为表头
                }

                String[] array = columnSize > 1 ? new String[columnSize] : null;
                String str = null;
                for (m = row.getLastCellNum(), j = 0; j <= m && j < columnSize; j++) {
                    str = getCellValueAsString(row.getCell(j, CREATE_NULL_AS_BLANK));
                    if (columnSize > 1) {
                        array[j] = str;
                    }
                }
                if (columnSize > 1) {
                    for (; j < columnSize; j++) {
                        array[j] = StringUtils.EMPTY;
                    }
                    data = (T) array;
                } else {
                    data = (T) str;
                }
                if (isNotEmpty(data)) {
                    k++;
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

}
