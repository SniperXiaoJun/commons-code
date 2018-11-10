package code.ponfee.commons.extract;

import static org.apache.poi.ss.usermodel.Row.MissingCellPolicy.CREATE_NULL_AS_BLANK;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Objects;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import com.monitorjbl.xlsx.StreamingReader;

/**
 * Excel file data extractor
 * 
 * 在打开一本工作簿时，不管是一个.xls HSSFWorkbook，还是一个.xlsx XSSFWorkbook，
 * 工作簿都可以从文件或InputStream中加载。使用File对象可以降低内存消耗，
 * 而InputStream则需要更多的内存，因为它必须缓冲整个文件。
 * 
 *  // Use a file
 *  Workbook wb = WorkbookFactory.create(new File("MyExcel.xls"));
 *  
 *  // Use an InputStream, needs more memory
 *  Workbook wb = WorkbookFactory.create(new FileInputStream("MyExcel.xlsx"));
 *  
 *  
 *  =======================================================================
 *  // HSSFWorkbook, File
 *  NPOIFSFileSystem fs = new NPOIFSFileSystem(new File("file.xls"));
 *  HSSFWorkbook wb = new HSSFWorkbook(fs.getRoot(), true);
 *  ....
 *  fs.close();
 *  
 *  // HSSFWorkbook, InputStream, needs more memory
 *  NPOIFSFileSystem fs = new NPOIFSFileSystem(myInputStream);
 *  HSSFWorkbook wb = new HSSFWorkbook(fs.getRoot(), true);
 *  
 *  
 *  =======================================================================
 *  // XSSFWorkbook, File
 *  OPCPackage pkg = OPCPackage.open(new File("file.xlsx"));
 *  XSSFWorkbook wb = new XSSFWorkbook(pkg);
 *  ....
 *  pkg.close();
 *  
 *  // XSSFWorkbook, InputStream, needs more memory
 *  OPCPackage pkg = OPCPackage.open(myInputStream);
 *  XSSFWorkbook wb = new XSSFWorkbook(pkg);
 *  ....
 *  pkg.close();
 * 
 * 
 * {@linkplain https://blog.csdn.net/zl_momomo/article/details/80703533}
 * {@linkplain http://poi.apache.org/components/spreadsheet/how-to.html}
 * 
 * @author Ponfee
 */
public class ExcelExtractor<T> extends DataExtractor<T> {

    private final ExcelType type;
    private final int sheetIndex; // start with 0
    private final int startRow; // start with 0

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
                case XLS: // sheet.getPhysicalNumberOfRows()
                    if (dataSource instanceof File) {
                        workbook = WorkbookFactory.create((File) dataSource);
                    } else {
                        workbook = WorkbookFactory.create(input = (InputStream) dataSource);
                    }
                    break;
                case XLSX:
                    // only support xlsx
                    StreamingReader.Builder builder = StreamingReader.builder()
                        .rowCacheSize(100) // 缓存到内存中的行数，默认是10
                        .bufferSize(4096); // 读取资源时，缓存到内存的字节大小，默认是1024
                    if (dataSource instanceof File) {
                        workbook = builder.open((File) dataSource);
                    } else {
                        workbook = builder.open(input = (InputStream) dataSource);
                    }
                    break;
                default:
                    throw new RuntimeException("Unknown excel type: " + type);
            }

            boolean specHeaders; int columnSize;
            if (ArrayUtils.isNotEmpty(headers)) {
                specHeaders = true;
                columnSize = this.headers.length;
            } else {
                specHeaders = false;
                columnSize = 0;
            }

            Row row; T data; String[] array; String str;
            Iterator<Row> iter = workbook.getSheetAt(sheetIndex).iterator();
            for (int i = 0, k = 0, m, j; iter.hasNext(); i++) {
                row = iter.next(); // row = sheet.getRow(i);
                if (row == null || i < startRow) {
                    continue;
                }

                if (!specHeaders && i == startRow) {
                    columnSize = row.getLastCellNum(); // 不指定表头则以开始行为表头
                }

                array = columnSize > 1 ? new String[columnSize] : null;
                str = null;
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
                    processor.process(k++, data);
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
