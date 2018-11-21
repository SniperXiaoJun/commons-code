package test.extract;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.junit.Test;

import code.ponfee.commons.concurrent.ThreadPoolExecutors;
import code.ponfee.commons.extract.streaming.xls.HSSFStreamingReader;
import code.ponfee.commons.extract.streaming.xls.HSSFStreamingSheet;
import code.ponfee.commons.extract.streaming.xls.HSSFStreamingWorkbook;

public class TestHSSFStreaming {

    static ThreadPoolExecutor exec = ThreadPoolExecutors.create(1, 8, 60);

    @Test
    public void test1() throws InterruptedException {
        HSSFStreamingWorkbook wb = HSSFStreamingReader.create(/*40, 0*/).open("e:/writeTest.xls", exec);
        HSSFStreamingSheet sheet = (HSSFStreamingSheet) wb.getSheetAt(0);
        int count = 0;
        for (Row row : sheet) {
            for (Cell cell : row) {
                System.out.print(cell.getStringCellValue() + ", ");
            }
            System.out.println();
            //Thread.sleep(1);
            count++;
        }

        System.out.println(((HSSFStreamingSheet) wb.getSheetAt(0)).getCacheRowCount());
        System.out.println(((HSSFStreamingSheet) wb.getSheetAt(0)).getSheetIndex());
        System.out.println(((HSSFStreamingSheet) wb.getSheetAt(0)).getSheetName());
        System.out.println("***");
        System.out.println(((HSSFStreamingSheet) wb.getSheetAt(1)).getCacheRowCount());
        System.out.println(((HSSFStreamingSheet) wb.getSheetAt(1)).getSheetIndex());
        System.out.println(((HSSFStreamingSheet) wb.getSheetAt(1)).getSheetName());
        System.out.println("====read end==" + count);
        exec.shutdown();
    }
    
    @Test
    public void test2() throws InterruptedException {
        HSSFStreamingWorkbook wb = HSSFStreamingReader.create(/*40, 0*/).open("e:/abc.xls", exec);
        HSSFStreamingSheet sheet = (HSSFStreamingSheet) wb.getSheetAt(0);
        int count = 0;
        for (Row row : sheet) {
            for (Cell cell : row) {
                System.out.print(cell == null ? "null," : cell.getStringCellValue() + ", ");
            }
            System.out.println();
            //Thread.sleep(1);
            count++;
        }
        System.out.println("====read end==" + count);
        exec.shutdown();
    }

    public static void add(List<String> list, int index, String s) {
        int size;
        if (index == (size = list.size())) {
            list.add(s);
        } else if (index < size) {
            list.set(index, s);
        } else {
            for (int i = size; i < index; i++) {
                list.add(null);
            }
            list.add(s  );
        }
    }
    
    public static void main(String[] args) {
        List<String> list = new ArrayList<>();
        add(list, 0, "a");
        System.out.println(list);
        
        add(list, 5, "b");
        System.out.println(list);
        
        add(list, 4, "c");
        System.out.println(list);
        
        add(list, 4, "c");
        System.out.println(list);
    }
}
