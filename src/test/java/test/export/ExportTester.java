package test.export;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import code.ponfee.commons.export.AbstractExporter;
import code.ponfee.commons.export.CellStyleOptions;
import code.ponfee.commons.export.CsvExporter;
import code.ponfee.commons.export.ExcelExporter;
import code.ponfee.commons.export.HtmlExporter;
import code.ponfee.commons.export.Table;
import code.ponfee.commons.export.Thead;
import code.ponfee.commons.export.Tmeta;
import code.ponfee.commons.export.Tmeta.Align;
import code.ponfee.commons.export.Tmeta.Type;
import code.ponfee.commons.io.FileTransformer;
import code.ponfee.commons.io.Files;
import code.ponfee.commons.json.Jsons;
import code.ponfee.commons.util.Captchas;

public class ExportTester {

    private int multiple = 20;

    public @Test void testHtml1() throws IOException {
        AbstractExporter html = new HtmlExporter();
        AbstractExporter csv = new CsvExporter();
        List<Thead> list = new ArrayList<>();

        list.add(new Thead("区域", 1, 0));
        list.add(new Thead("分公司", 2, 0));
        list.add(new Thead("昨天", 3, 0));
        list.add(new Thead("项目数", 4, 3));
        list.add(new Thead("项目应收(元)", 5, 3));
        list.add(new Thead("成交套数", 6, 3));
        list.add(new Thead("套均收入(元)", 7, 3));
        list.add(new Thead("团购项目数", 8, 3));
        list.add(new Thead("导客项目数", 9, 3));
        list.add(new Thead("代收项目数", 10, 3));
        list.add(new Thead("线上项目数", 11, 3));
        list.add(new Thead("本月", 12, 0));
        list.add(new Thead("应收(万)", 13, 12));
        list.add(new Thead("实收(万)", 14, 12));
        list.add(new Thead("成交套数", 15, 12));
        list.add(new Thead("套均收入(元)", 16, 12));
        list.add(new Thead("团购项目应收(万)", 17, 12));
        list.add(new Thead("团购项目成交套数", 18, 12));
        list.add(new Thead("团购项目经服成交套数", 19, 12));
        list.add(new Thead("团购项目套均收入(元)", 20, 12));
        list.add(new Thead("团购项目经服成交应收(万)", 21, 12));
        list.add(new Thead("团购项目中介应付外佣(万)", 22, 12));
        list.add(new Thead("团购项目经服成交套数占比", 23, 12));
        list.add(new Thead("团购项目中介分佣比例", 24, 12));
        list.add(new Thead("导客项目应收(万)", 25, 12));
        list.add(new Thead("导客项目成交套数", 26, 12));
        list.add(new Thead("导客项目套均收入(元)", 27, 12));
        list.add(new Thead("导客项目中介应付外佣(万)", 28, 12));
        list.add(new Thead("导客项目中介分佣比例", 29, 12));
        list.add(new Thead("代收项目应收(万)", 30, 12));
        list.add(new Thead("代收项目成交套数", 31, 12));
        list.add(new Thead("线上项目应收(万)", 32, 12));
        list.add(new Thead("线上项目成交套数", 33, 12));
        list.add(new Thead("月指标(万)", 34, 12));
        list.add(new Thead("指标完成率", 35, 12));

        Table table = new Table(list);
        System.out.println(Jsons.toJson(table.getThead()));
        table.setCaption("abc");
        table.addRowsAndEnd(Lists.newArrayList(new Object[]{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
                                          new Object[]{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
                                          new Object[]{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
                                          new Object[]{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
                                          new Object[]{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
                                          new Object[]{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
                                          new Object[]{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
                                          new Object[]{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
                                          new Object[]{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
                                          new Object[]{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
                                          new Object[]{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
                                          new Object[]{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
                                          new Object[]{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
                                          new Object[]{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
                                          new Object[]{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
                                          new Object[]{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
                                          new Object[]{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
                                          new Object[]{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
                                          new Object[]{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
                                          new Object[]{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
                                          new Object[]{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
                                          new Object[]{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
                                          new Object[]{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
                                          new Object[]{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
                                          new Object[]{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
                                          new Object[]{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
                                          new Object[]{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1}
        ));
        table.setTfoot(new Object[]{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1});
        table.setComment("comment1;comment2;comment3;comment4;comment5;comment6;");
        html.build(table);
        csv.build(table);

        table = new Table(list);
        table.setCaption("123");
        table.end();
        html.build(table);
        
        table = new Table(list);
        table.end();
        table.setCaption("bnm");
        html.build(table);

        IOUtils.write((String) html.setName("报表").export(), new FileOutputStream("d://testHtml1.html"), "UTF-8");
        Files.addBOM("d:/testHtml1.html");
        IOUtils.write((String) csv.export(), new FileOutputStream("d://testHtml1.csv"), "UTF-8");
        Files.addBOM("d:/testHtml1.csv");

        html.close();
        csv.close();
    }

    @Test
    public void testHtml2() throws FileNotFoundException, IOException {
        AbstractExporter html = new HtmlExporter();
        html.build(new Table("a,b,c,d,e".split(",")).end());
        IOUtils.write((String) html.export(), new FileOutputStream("d://testHtml2.html"), "UTF-8");
        Files.addBOM("d:/testHtml2.html");
        html.close();
    }

    @Test
    public void testExcel() throws IOException {
        ExcelExporter excel = new ExcelExporter();
        List<Thead> list = new ArrayList<>();

        list.add(new Thead("区域", 1, 0, new Tmeta(Type.NUMERIC, "#,###.00%", Align.RIGHT, false, "#cccccc")));
        list.add(new Thead("分公司", 2, 0, new Tmeta(Type.DATETIME, "yyyy-MM-dd", Align.CENTER, true, "#fcdebc")));
        list.add(new Thead("昨天", 3, 0));
        list.add(new Thead("项目数", 4, 3, new Tmeta(Type.CHAR, null, Align.RIGHT, true, "#abc")));
        list.add(new Thead("项目应收(元)", 5, 3));
        list.add(new Thead("成交套数", 6, 3));
        list.add(new Thead("套均收入(元)", 7, 3));
        list.add(new Thead("团购项目数", 8, 3));
        list.add(new Thead("导客项目数", 9, 3));
        list.add(new Thead("代收项目数", 10, 3));
        list.add(new Thead("线上项目数", 11, 3));
        list.add(new Thead("本月", 12, 0));
        list.add(new Thead("应收(万)", 13, 12));
        list.add(new Thead("实收(万)", 14, 12));
        list.add(new Thead("成交套数", 15, 12));
        list.add(new Thead("套均收入(元)", 16, 12));
        list.add(new Thead("团购项目应收(万)", 17, 12));
        list.add(new Thead("团购项目成交套数", 18, 12));
        list.add(new Thead("团购项目经服成交套数", 19, 12));
        list.add(new Thead("团购项目套均收入(元)", 20, 12));
        list.add(new Thead("团购项目经服成交应收(万)", 21, 12));
        list.add(new Thead("团购项目中介应付外佣(万)", 22, 12));
        list.add(new Thead("团购项目经服成交套数占比", 23, 12));
        list.add(new Thead("团购项目中介分佣比例", 24, 12));
        list.add(new Thead("导客项目应收(万)", 25, 12));
        list.add(new Thead("导客项目成交套数", 26, 12));
        list.add(new Thead("导客项目套均收入(元)", 27, 12));
        list.add(new Thead("导客项目中介应付外佣(万)", 28, 12));
        list.add(new Thead("导客项目中介分佣比例", 29, 12));
        list.add(new Thead("代收项目应收(万)", 30, 12));
        list.add(new Thead("代收项目成交套数", 31, 12));
        list.add(new Thead("线上项目应收(万)", 32, 12));
        list.add(new Thead("线上项目成交套数", 33, 12));
        list.add(new Thead("月指标(万)", 34, 12));
        list.add(new Thead("指标完成率", 35, 12));
        
        List<Object[]> data1 = new ArrayList<>();
        for (int i = 0; i < 2*multiple; i++) {
            data1.add(new Object[] { "1234563.918%", "2017-02-03", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd",
                "abd", "abd",
                "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd" });
        }
        
        List<Object[]> data2 = new ArrayList<>();
        for (int i = 0; i < 5*multiple; i++) {
            data2.add(new Object[] { "1234563.918%", "2017-02-03", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd",
                "abd", "abd",
                "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd" });
        }
        
        List<Object[]> data3 = new ArrayList<>();
        for (int i = 0; i < 3*multiple; i++) {
            data3.add(new Object[] { "1234563.918%", "2017-02-03", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd",
                "abd", "abd",
                "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd" });
        }
        Object[] tfoot = new Object[] {"1", "2", "3", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd","abd", "abd",
            "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd" };
        Map<CellStyleOptions, Object> options = ImmutableMap.of(CellStyleOptions.HIGHLIGHT, ImmutableMap.of("cells", Lists.newArrayList(Lists.newArrayList(1,1),Lists.newArrayList(2,2)), "color", "#FF3030"));
        long start = System.currentTimeMillis();
        System.out.println("========================================start");

        Table table1 = new Table(list);
        table1.setCaption("test1");
        table1.addRowsAndEnd(data1);
        table1.setTfoot(tfoot);
        table1.setOptions(options);
        excel.setName("报表1").build(table1);

        // ------------------------------------------
        Table table2 = new Table(list);
        table2.setCaption("test2");
        table2.addRowsAndEnd(data2);
        table2.setTfoot(tfoot);
        table2.setOptions(options);
        excel.setName("报表2").build(table2);

        // ------------------------------------------
        Table table3 = new Table(list);
        table3.setCaption("test3");
        table3.addRowsAndEnd(data3);
        table3.setTfoot(tfoot);
        table3.setOptions(options);
        excel.setName("报表1").build(table3);

        // ------------------------------------------
        excel.setName("图表");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Captchas.generate(200, baos, RandomStringUtils.randomAlphanumeric(10));
        excel.insertImage(baos.toByteArray());
        baos = new ByteArrayOutputStream();
        Captchas.generate(200, baos, RandomStringUtils.randomAlphanumeric(10));
        excel.insertImage(baos.toByteArray());

        OutputStream out = new FileOutputStream("d:/abc1.xlsx");
        excel.write(out);
        out.close();
        excel.close();
        System.out.println("========================================excel: " + (System.currentTimeMillis() - start));

        start = System.currentTimeMillis();
        // -------------------------csv
        CsvExporter csv = new CsvExporter();
        table1 = new Table(list);
        table1.setCaption("test1");
        table1.addRowsAndEnd(data1);
        table1.setTfoot(tfoot);
        table1.setOptions(options);
        csv.build(table1);
        IOUtils.write(csv.export().toString(), new FileOutputStream("d://testExcel.csv"), "UTF-8");
        Files.addBOM(new File("d://testExcel.csv"));
        csv.close();
        System.out.println("========================================csv: " + (System.currentTimeMillis() - start));
        
        start = System.currentTimeMillis();
        HtmlExporter html = new HtmlExporter();
        table1 = new Table(list);
        table1.setCaption("test1");
        table1.addRowsAndEnd(data1);
        table1.setTfoot(tfoot);
        table1.setOptions(options);
        html.build(table1);
        html.setName("test");
        IOUtils.write((String) html.export(), new FileOutputStream("d://testExcel.html"), "UTF-8");
        Files.addBOM("d:/testExcel.html");
        html.close();
        System.out.println("========================================html: " + (System.currentTimeMillis() - start));
    }

    @Test
    public void testExcel2() throws IOException {
        AbstractExporter excel = new ExcelExporter();

        Table table = new Table("a,b,c,d,e".split(","));
        table.setCaption("title");
        List<Object[]> data = new ArrayList<>();
        data.add(new Object[] { "1", "2", "3", "4", "5" });
        table.addRowsAndEnd(data);
        excel.setName("21321");
        excel.build(table);
        IOUtils.write((byte[]) excel.export(), new FileOutputStream("d:/testExcel2.xlsx"));
        excel.close();
    }

    public static void main(String[] args) {
        System.out.println(FileTransformer.guessEncoding("d:/csv3.csv"));
    }
}
