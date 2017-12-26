package test.excel;

import org.apache.poi.xssf.streaming.SXSSFCell;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

/**
 * 单元格处理
 * @author Ponfee
 * @param <T>
 */
@FunctionalInterface
public interface ExcelCellProcessor<T> {

    void process(SXSSFWorkbook workbook, SXSSFCell cell, T t, int row, int col);
}
