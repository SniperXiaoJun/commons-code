package code.ponfee.commons.extract;

import static org.apache.poi.ss.usermodel.Row.MissingCellPolicy.CREATE_NULL_AS_BLANK;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
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

import com.monitorjbl.xlsx.StreamingReader;

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

    public ExcelExtractor(Object dataSource, String[] headers, 
                          int startRow, ExcelType type, int sheetIndex) {
        super(dataSource, headers);
        this.startRow = startRow;
        this.type = type;
        this.sheetIndex = sheetIndex;
    }

    @Override @SuppressWarnings("unchecked")
    public void extract(RowProcessor<T> processor) throws IOException {
        InputStream input = null;
        Workbook workbook = null;
        try {
            switch (type) {
                case XLS:
                    workbook = new HSSFWorkbook(input = super.asInputStream());
                    break;
                /*case XLSX:
                    OPCPackage opcPkg;
                    if (dataSource instanceof CharSequence) {
                        opcPkg = OPCPackage.open(dataSource.toString());
                    } else if (dataSource instanceof File) {
                        opcPkg = OPCPackage.open((File) dataSource);
                    } else {
                        opcPkg = OPCPackage.open(input = (InputStream) dataSource);
                    }
                    workbook = new XSSFWorkbook(opcPkg);
                    //new SXSSFWorkbook(new XSSFWorkbook(OPCPackage.open(input)), 200);
                    break;*/
                case XLSX:
                    StreamingReader.Builder builder = StreamingReader.builder()
                            .rowCacheSize(10) // 缓存到内存中的行数，默认是10
                            .bufferSize(8192); // 读取资源时，缓存到内存的字节大小，默认是1024
                    // 可以是InputStream或者是File，注意：只能打开XLSX格式的文件
                    if (dataSource instanceof CharSequence) {
                        workbook = builder.open(new File(dataSource.toString()));
                    } else if (dataSource instanceof File) {
                        workbook = builder.open((File) dataSource);
                    } else {
                        workbook = builder.open(input = (InputStream) dataSource);
                    }
                    break;
                default:
                    throw new RuntimeException("Unknown excel type: " + type);
            }

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

            Iterator<Row> iter = sheet.iterator();
            Row row;
            for (int i = startRow, k = i, m, j; iter.hasNext(); i++) {
                row = iter.next(); // row = sheet.getRow(i);
                if (row == null && i < startRow) {
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
        } finally {
            if (workbook != null) try {
                workbook.close();
            } catch (Exception ignored) {
                ignored.printStackTrace();
            }
            if (input != null) try {
                input.close();
            } catch (Exception ignored) {
                ignored.printStackTrace();
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
        switch (cell.getCellType()) {
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return String.valueOf(cell.getDateCellValue());
                } else {
                    cell.setCellType(CellType.STRING);
                    return cell.getStringCellValue();
                }
            case STRING:
                return Objects.toString(cell.getStringCellValue(), "");
            case BOOLEAN:
                return Boolean.toString(cell.getBooleanCellValue());
            case FORMULA:
                cell.setCellType(CellType.STRING);
                return cell.getStringCellValue();
            case BLANK: // 空值
            case ERROR: // 错误
            default:
                return StringUtils.EMPTY;
        }
    }

    public enum ExcelType {
        XLS, XLSX;
    }
}
