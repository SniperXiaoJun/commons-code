package test.extract;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;

import org.junit.Test;

import code.ponfee.commons.extract.DataExtractor;
import code.ponfee.commons.extract.DataExtractorBuilder;

/**
 * 性能：Path > File > Input
 * @author 01367825
 */
public class ExcelExtractorTest {

    @Test
    public void testPath() throws FileNotFoundException, IOException {
        DataExtractor<?> et = DataExtractorBuilder.newBuilder("D:\\test\\test_excel_14.xls")
            .headers(new String[] { "a", "b", "c", "d", "e" }).build();
        et.extract((n, d) -> {
            System.out.println(Arrays.toString((String[])d));
        });
    }
    
    @Test
    public void testFile() throws FileNotFoundException, IOException {
        DataExtractor<?> et = DataExtractorBuilder.newBuilder(new File("D:\\test\\test_excel_14.xlsx"))
            /*.headers(new String[] { "a", "b", "c", "d", "e" })*/.build();
        et.extract((n, d) -> {
            System.out.println(Arrays.toString((String[])d));
        });
    }
    
    @Test
    public void testInput() throws FileNotFoundException, IOException {
        DataExtractor<?> et = DataExtractorBuilder.newBuilder(new FileInputStream("D:\\test\\test_excel_14.xlsx"), "test_excel_16.xlsx", null)
            .headers(new String[] { "a", "b", "c", "d", "e" }).build();
        et.extract((n, d) -> {
            if (n == 0) {
                System.out.println(Arrays.toString((String[])d));
            }
            if (n == 1) {
                System.out.println(Arrays.toString((String[])d));
            }
            System.out.println(Arrays.toString((String[])d));
        });
    }
    
    @Test
    public void test1() throws FileNotFoundException, IOException {
        DataExtractor<?> et = DataExtractorBuilder.newBuilder("e:\\data_expert_temp.xls")
            .headers(new String[] { "a", "b", "c", "d", "e" }).build();
        et.extract((n, d) -> {
            System.out.println(Arrays.toString((String[])d));
        });
    }
    
    @Test
    public void test2() throws FileNotFoundException, IOException {
        DataExtractor<?> et = DataExtractorBuilder.newBuilder("E:\\test.xlsx")
            .headers(new String[] { "a", "b", "c", "d", "e" }).build();
        et.extract((n, d) -> {
            System.out.println(String.join("|",(String[])d));
        });
    }
    
    @Test
    public void test3() throws FileNotFoundException, IOException {
        DataExtractor<?> et = DataExtractorBuilder.newBuilder("E:\\advices_export.xls")
            .headers(new String[] { "a", "b", "c", "d"}).build();
        et.extract((n, d) -> {
            System.out.println(String.join("|",(String[])d));
        });
    }
    
    @Test
    public void testCsvPath() throws FileNotFoundException, IOException {
        DataExtractor<?> et = DataExtractorBuilder.newBuilder("E:\\test.csv")
            .headers(new String[] { "a", "b", "c", "d", "e" }).build();
        et.extract((n, d) -> {
            if (n < 10)
            System.out.println(Arrays.toString((String[])d));
        });
    }
}
