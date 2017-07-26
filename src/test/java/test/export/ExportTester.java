package test.export;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import com.google.common.collect.Lists;

import code.ponfee.commons.export.AbstractExporter;
import code.ponfee.commons.export.CsvExporter;
import code.ponfee.commons.export.ExcelExporter;
import code.ponfee.commons.export.HtmlExporter;
import code.ponfee.commons.export.Table;
import code.ponfee.commons.export.Thead;
import code.ponfee.commons.export.Tmeta;
import code.ponfee.commons.export.Tmeta.Align;
import code.ponfee.commons.export.Tmeta.Type;
import code.ponfee.commons.util.FileTransformer;
import code.ponfee.commons.util.Streams;

public class ExportTester {

    @Test
    public void testHtml() throws IOException {
        AbstractExporter builder = new HtmlExporter();
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
        table.setCaption("abc");
        table.setTobdy(Lists.newArrayList(new Object[]{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
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
        table.setComment("comment1;comment2;comment3;comment4;comment5;comment6;");
        builder.build(table);
        csv.build(table);

        table = new Table(list);
        table.setCaption("123");
        builder.build(table);
        
        table = new Table(list);
        table.setCaption("bnm");
        builder.build(table);

        System.out.println(builder.setName("报表").export());
        Streams.string2file((String)csv.export(), "d:/csv.csv");
        Streams.addBOM("d:/csv.csv");
    }

    @Test
    public void testHtml2() {
        AbstractExporter builder = new HtmlExporter();

        Table table = new Table("a,b,c,d,e".split(","));
        builder.build(table);
        System.out.println(builder.export());
    }

    @Test
    public void testExcel() throws FileNotFoundException, IOException {
        AbstractExporter excel = new ExcelExporter();
        List<Thead> list = new ArrayList<>();

        list.add(new Thead("区域", 1, 0, new Tmeta(Type.NUMERIC, "#,###.00%", Align.RIGHT, false, "#cccccc")));
        list.add(new Thead("分公司", 2, 0, new Tmeta(Type.DATETIME, "yyyy-MM-dd", Align.CENTER, true, "#fcdebc")));
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
        System.out.println("========================================start");
        long start = System.currentTimeMillis();

        Table table = new Table(list);
        table.setCaption("test1");
        List<Object[]> data = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            data.add(new Object[] { "1234563.918%", "2017-02-03", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd",
                "abd", "abd",
                "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd" });
        }
        table.setTobdy(data);
        excel.setName("报表1").build(table);

        // ------------------------------------------
        table = new Table(list);
        table.setCaption("test2");
        data = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            data.add(new Object[] { "1234563.918%", "2017-02-03", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd",
                "abd", "abd",
                "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd" });
        }
        table.setTobdy(data);
        excel.setName("报表2").build(table);

        // ------------------------------------------
        table = new Table(list);
        table.setCaption("test3");
        data = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            data.add(new Object[] { "1234563.918%", "2017-02-03", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd",
                "abd", "abd",
                "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd" });
        }
        table.setTobdy(data);
        excel.setName("报表1").build(table);

        // ------------------------------------------
        excel.setName("图表");
        ((ExcelExporter)excel).nestedImage(Streams.file2bytes("d:/test/2.png"));
        ((ExcelExporter)excel).nestedImage(Streams.file2bytes("d:/test/2.png"));
        
        IOUtils.write((byte[]) excel.export(), new FileOutputStream("d:/abc.xlsx"));
        excel.close();
        System.out.println("========================================" + (System.currentTimeMillis() - start));
        
        CsvExporter csv = new CsvExporter();
        csv.build(table);
        Streams.string2file(csv.export().toString(), "d://csv.csv");
        Streams.addBOM(new File("d://csv.csv"));
    }

    @Test
    public void testExcel2() throws FileNotFoundException, IOException {
        AbstractExporter builder = new ExcelExporter();

        Table table = new Table("a,b,c,d,e".split(","));
        table.setCaption("title");
        List<Object[]> data = new ArrayList<>();
        data.add(new Object[] { "1", "2", "3", "4", "5" });
        table.setTobdy(data);
        builder.build(table);
        IOUtils.write((byte[]) builder.export(), new FileOutputStream("d:/123.xlsx"));
    }

    public static void main(String[] args) {
        System.out.println(FileTransformer.guessEncoding("d:/csv.csv"));
    }
}
