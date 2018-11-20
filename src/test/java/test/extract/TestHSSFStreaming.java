package test.extract;


import java.util.concurrent.ThreadPoolExecutor;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import code.ponfee.commons.concurrent.ThreadPoolExecutors;
import code.ponfee.commons.extract.xls.HSSFStreamingFactory;

public class TestHSSFStreaming {

    static ThreadPoolExecutor  exec = ThreadPoolExecutors.create(1, 8, 60);
    public static void main(String[] args) {
        Sheet sheet = HSSFStreamingFactory.open("e:/advices_export.xls", exec).getSheetAt(1);
        for (Row row : sheet) {
            for (Cell cell : row) {
                System.out.print(cell.getStringCellValue()+", ");
            }
            System.out.println();
        }
        
        System.out.println("read end");
        exec.shutdown();
    }
}
