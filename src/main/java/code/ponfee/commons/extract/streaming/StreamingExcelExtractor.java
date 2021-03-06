package code.ponfee.commons.extract.streaming;

import java.io.File;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Workbook;

import com.monitorjbl.xlsx.StreamingReader;

import code.ponfee.commons.concurrent.ThreadPoolExecutors;
import code.ponfee.commons.extract.ExcelExtractor;
import code.ponfee.commons.extract.streaming.xls.HSSFStreamingReader;

/**
 * Excel file data extractor based streaming
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
 * {@linkplain https://github.com/monitorjbl/excel-streaming-reader}
 * 
 * @author Ponfee
 */
public class StreamingExcelExtractor<T> extends ExcelExtractor<T> {

    private static volatile ExecutorService executor;

    public StreamingExcelExtractor(InputStream inputStream, String[] headers, 
                                   int startRow, ExcelType type) {
        this(inputStream, headers, startRow, type, 0);
    }

    public StreamingExcelExtractor(Object dataSource, String[] headers, 
                                   int startRow, ExcelType type, int sheetIndex) {
        super(dataSource, headers, startRow, type, sheetIndex);
    }

    @Override
    protected Workbook createWorkbook() {
        switch (type) {
            case XLS:
                HSSFStreamingReader reader = HSSFStreamingReader.create(200, sheetIndex);
                if (executor == null) {
                    executor = ThreadPoolExecutors.INFINITY_QUEUE_EXECUTOR;
                }
                if (dataSource instanceof File) {
                    return reader.open((File) dataSource, executor);
                } else {
                    return reader.open((InputStream) dataSource, executor);
                }
            case XLSX:
                // only support xlsx
                StreamingReader.Builder builder = StreamingReader.builder()
                    .rowCacheSize(100) // 缓存到内存中的行数，默认是10
                    .bufferSize(4096); // 读取资源时，缓存到内存的字节大小，默认是1024
                if (dataSource instanceof File) {
                    return builder.open((File) dataSource);
                } else {
                    return builder.open((InputStream) dataSource);
                }
            default:
                throw new RuntimeException("Unknown excel type: " + type);
        }
    }

    /**
     * 获取单元格的值
     * 
     * @param cell
     * @return
     */
    @Override
    protected String getStringCellValue(Cell cell) {
        if (cell == null) {
            return StringUtils.EMPTY;
        }
        return cell.getStringCellValue();
    }

}
