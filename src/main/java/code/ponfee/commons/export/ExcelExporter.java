package code.ponfee.commons.export;

import java.awt.Color;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Consumer;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.ClientAnchor.AnchorType;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFCell;
import org.apache.poi.xssf.streaming.SXSSFDrawing;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFDataFormat;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;

import code.ponfee.commons.export.Tmeta.Type;
import code.ponfee.commons.math.Numbers;
import code.ponfee.commons.util.Colors;
import code.ponfee.commons.util.Dates;
import code.ponfee.commons.util.ImageUtils;
import code.ponfee.commons.util.ObjectUtils;
import code.ponfee.commons.util.Strings;

/**
 * excel导出
 *
 * SXSSFWorkbook（size>65536），XSSFWorkbook（.xlsx），HSSFWorkbook（.xls）
 *
 * @author fupf
 */
public class ExcelExporter extends AbstractExporter {

    /** row and cell config，默认的列宽和行高大小 */
    private static final int DEFAULT_WIDTH = 3200;
    private static final short DEFAULT_HEIGHT = 350;

    /** nested image config，office 2013版本 */
    private static final double RATE_WIDTH = 70.5; // 图片真实宽度与excel列宽的比例
    private static final double RATE_HEIGHT = 18; // 图片真实高度与excel行高的比例

    /** 作为分隔符（类似html的<hr />）的合并列数目 */
    private static final int MARGIN_ROW_CELL_SIZE = 26;

    private SXSSFWorkbook workbook; // excel
    private final XSSFCellStyle titleStyle; // 标题样式
    private final XSSFCellStyle headStyle; // 表头样式
    private final XSSFCellStyle dataStyle; // 数据样式
    private final XSSFCellStyle tfootMergeStyle; // 合计行样式
    private final XSSFCellStyle noneStyle; // 无样式
    private final XSSFCellStyle tipStyle; // 无样式
    private final XSSFDataFormat dataFormat; // 数据格式

    private final Map<String, SXSSFSheet> sheets = new HashMap<>();
    private final Map<String, Integer>    images = new HashMap<>();
    private final Map<String, Freeze>    freezes = new HashMap<>();

    public ExcelExporter() {
        workbook = new SXSSFWorkbook(200);
        dataFormat = (XSSFDataFormat) workbook.createDataFormat();

        XSSFFont titleFont = (XSSFFont) workbook.createFont();
        titleFont.setBold(true);
        titleFont.setFontName("黑体");

        XSSFFont headFont = (XSSFFont) workbook.createFont();
        headFont.setBold(true);
        headFont.setFontName("宋体");

        XSSFFont redFont = (XSSFFont) workbook.createFont();
        redFont.setColor(new XSSFColor(new Color(255, 0, 0)));
        redFont.setFontName("宋体");

        XSSFCellStyle baseStyle = (XSSFCellStyle) workbook.createCellStyle();
        baseStyle.setBorderLeft(BorderStyle.THIN);
        baseStyle.setBorderTop(BorderStyle.THIN);
        baseStyle.setBorderRight(BorderStyle.THIN);
        baseStyle.setBorderBottom(BorderStyle.THIN);
        baseStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        baseStyle.setWrapText(true);
        baseStyle.setDataFormat(dataFormat.getFormat("@"));

        titleStyle = (XSSFCellStyle) workbook.createCellStyle();
        titleStyle.cloneStyleFrom(baseStyle);
        titleStyle.setAlignment(HorizontalAlignment.CENTER);
        titleStyle.setFont(titleFont);
        titleStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND); // 填充样式
        titleStyle.setFillForegroundColor(new XSSFColor(new Color(255, 255, 224))); // 填充颜色

        headStyle = (XSSFCellStyle) workbook.createCellStyle();
        headStyle.cloneStyleFrom(baseStyle);
        headStyle.setAlignment(HorizontalAlignment.CENTER);
        headStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headStyle.setFillForegroundColor(new XSSFColor(new Color(192, 192, 192)));
        headStyle.setFont(headFont);
        headStyle.setWrapText(false);

        dataStyle = (XSSFCellStyle) workbook.createCellStyle();
        dataStyle.cloneStyleFrom(baseStyle);

        tfootMergeStyle = (XSSFCellStyle) workbook.createCellStyle();
        tfootMergeStyle.cloneStyleFrom(dataStyle);
        tfootMergeStyle.setFont(headFont);
        tfootMergeStyle.setWrapText(false);
        tfootMergeStyle.setAlignment(HorizontalAlignment.RIGHT);
        tfootMergeStyle.setBorderLeft(BorderStyle.THIN);
        tfootMergeStyle.setBorderTop(BorderStyle.THIN);
        tfootMergeStyle.setBorderRight(BorderStyle.THIN);
        tfootMergeStyle.setBorderBottom(BorderStyle.THIN);

        tipStyle = (XSSFCellStyle) workbook.createCellStyle();
        tipStyle.cloneStyleFrom(baseStyle);
        tipStyle.setFont(redFont);

        noneStyle = (XSSFCellStyle) workbook.createCellStyle();
        noneStyle.setVerticalAlignment(VerticalAlignment.CENTER);
    }

    /**
     * 构建excel
     */
    @Override
    public void build(Table table) {
        // 1、校验表头是否为空
        if (table.getThead() == null || table.getThead().isEmpty()) {
            throw new IllegalArgumentException("thead can't be null");
        }

        String name = this.getName();
        // 2、获取工作簿
        SXSSFSheet sheet = getSheet(name);

        // 3、判断工作簿是否已创建过行数据
        CursorRow cursorRow = new CursorRow(sheet.getLastRowNum());
        if (cursorRow.get() > 0) {
            // 创建两行空白行
            cursorRow.increment();
            int i = cursorRow.getAndIncrement(), j = cursorRow.getAndIncrement();
            SXSSFRow row1 = sheet.createRow(i);
            row1.setHeight(DEFAULT_HEIGHT);
            SXSSFRow row2 = sheet.createRow(j);
            row2.setHeight(DEFAULT_HEIGHT);
            for (int k = 0; k < MARGIN_ROW_CELL_SIZE; k++) {
                createCell(row1, k, noneStyle, null);
                createCell(row2, k, noneStyle, null);
            }
            sheet.addMergedRegion(new CellRangeAddress(i, j, 0, MARGIN_ROW_CELL_SIZE - 1));
        }

        // 4、构建复合表头
        buildComplexThead(table, sheet, cursorRow);

        // 5、冻结窗口配置
        if (freezes.get(name) != null) {
            freezes.get(name).disable();
        } else {
            freezes.put(name, new Freeze(1, cursorRow.get())); // 叶子节点只占一列，故colSplit=1
        }

        // 6、判断是否有数据
        if (ObjectUtils.isEmpty(table.getTobdy()) && ObjectUtils.isEmpty(table.getTfoot())) {
            createBlankRow(NO_RESULT_TIP, sheet, tipStyle, cursorRow, table.getTotalLeafCount());
            return;
        }

        super.nonEmpty();
        List<XSSFCellStyle> styles = createStyles(table.getThead());
        List<Thead> theads = table.getThead();
        SXSSFRow row;

        // 7、处理tbody数据
        List<Object[]> tbody = table.getTobdy();
        if (tbody != null && !tbody.isEmpty()) {
            Map<CellStyleOptions, Object> options = table.getOptions();
            Object[] data;
            for (int i = 0, n = tbody.size(), j, m; i < n; i++) {
                row = sheet.createRow(cursorRow.getAndIncrement());
                row.setHeight(DEFAULT_HEIGHT);
                data = tbody.get(i);
                for (m = data.length, j = 0; j < m; j++) {
                    createCell(row, j, styles.get(j), theads.get(j).getTmeta(), data[j], i, j, options);
                }
            }
        }

        // 8、处理tfoot数据
        Object[] tfoots = table.getTfoot();
        if (tfoots != null && tfoots.length > 0) {
            int rowNum = cursorRow.getAndIncrement();
            row = sheet.createRow(rowNum);
            row.setHeight(DEFAULT_HEIGHT);

            if (table.getTfoot().length > table.getTotalLeafCount()) {
                throw new IllegalStateException("tfoot data length cannot more than total leaf count.");
            }

            // 合计单元格
            int mergeNum = table.getTotalLeafCount() - table.getTfoot().length;
            for (int i = 0; i < mergeNum; i++) {
                createCell(row, i, tfootMergeStyle, (i == 0) ? "合计" : null);
            }
            if (mergeNum > 1) {
                sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 0, mergeNum - 1));
            }

            // 合计数据
            for (int i = 0; i < tfoots.length; i++) {
                createCell(row, i + mergeNum, styles.get(mergeNum + i),
                           theads.get(mergeNum + i).getTmeta(), tfoots[i]);
            }
        }

        // 9、文字注释
        if (StringUtils.isNotBlank(table.getComment())) {
            createBlankRow(table.getComment(), sheet, tipStyle, cursorRow, table.getTotalLeafCount());
        }
    }

    public void insertImage(byte[] imageBytes) {
        int[] size = ImageUtils.getImageSize(new ByteArrayInputStream(imageBytes));
        insertImage(imageBytes, size[0], size[1]);
    }

    /**
     * excel中嵌入图片
     * excel.nestedImage(byte[] image, width, cheight);
     */
    public void insertImage(byte[] imageBytes, int width, int height) {
        if (imageBytes == null || imageBytes.length == 0) {
            return;
        }

        super.nonEmpty();

        SXSSFSheet sheet = getSheet(getName());
        int startRow = images.get(getName()) == null ? 1 : images.get(getName()), startCol = 1;
        int endCol = startCol + (int) Math.round(((double) width) / RATE_WIDTH);
        int endRow = startRow + (int) Math.round(((double) height) / RATE_HEIGHT);
        images.put(getName(), endRow + 2);

        SXSSFDrawing drawing = sheet.createDrawingPatriarch();
        XSSFClientAnchor anchor = new XSSFClientAnchor(0, 0, width, height, startCol,
                                                       startRow, (short) endCol, endRow);

        anchor.setAnchorType(AnchorType.DONT_MOVE_AND_RESIZE);
        drawing.createPicture(anchor, workbook.addPicture(imageBytes, SXSSFWorkbook.PICTURE_TYPE_PNG));

        /*int pictureIdx = workbook.addPicture(imageBytes, Workbook.PICTURE_TYPE_PNG);
        CreationHelper helper = workbook.getCreationHelper();
        Drawing drawing = sheet.createDrawingPatriarch();
        ClientAnchor anchor = helper.createClientAnchor();
        anchor.setCol1(0);
        anchor.setRow1(sheet.getLastRowNum());
        Picture pict = drawing.createPicture(anchor, pictureIdx);
        pict.resize(1);*/
    }

    /**
     * 输出到输出流
     */
    public void write(OutputStream out) {
        try (BufferedOutputStream bos = new BufferedOutputStream(out)) {
            createFreezePane();
            workbook.write(bos);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void write(String filepath) {
        try (OutputStream out = new FileOutputStream(filepath)) {
            write(out);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 导出
     */
    @Override
    public byte[] export() {
        ByteArrayOutputStream out = new ByteArrayOutputStream(8192);
        write(out);
        return out.toByteArray();
    }

    /**
     * 关闭
     */
    @Override
    public void close() {
        if (workbook != null) try {
            workbook.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        workbook = null;
        sheets.clear();
        images.clear();
        freezes.clear();
    }

    //--protected methods------------------------------------------------------------------
    protected SXSSFSheet getSheet(String name) {
        SXSSFSheet sheet = sheets.get(name);
        if (sheet == null) {
            sheet = workbook.createSheet(name);
            sheet.setDisplayGridlines(false); // 不显示网格线
            /*sheet.setDefaultColumnWidth(DEFAULT_WIDTH);
            sheet.setDefaultRowHeight(DEFAULT_HEIGHT);*/
            sheets.put(name, sheet);
        }
        return sheet;
    }

    protected void removeSheet(String name) {
        workbook.removeSheetAt(workbook.getSheetIndex(sheets.get(name)));
        sheets.remove(name);
    }

    private void createFreezePane() {
        for (Entry<String, SXSSFSheet> entry : sheets.entrySet()) {
            Freeze freeze = freezes.get(entry.getKey());
            if (freeze != null && freeze.freeze) {
                entry.getValue().createFreezePane(freeze.colSplit, freeze.rowSplit);
            }
        }
    }

    // 创建简单表头
    /*private void buildSimpleThead(String title, XSSFSheet sheet, CursorRow cursorRow, String[] theadName) {
        createCell(title, sheet, titleStyle, cursorRow, theadName.length);
        XSSFRow row = sheet.createRow(cursorRow.getAndIncrement());
        row.setHeight(DEFAULT_HEIGHT);
        for (int i = 0; i < theadName.length; i++) {
            sheet.setColumnWidth(i, DEFAULT_WIDTH);
            createCell(row, i, headStyle, theadName[i]);
        }
    }*/

    // 复合表头
    private void buildComplexThead(Table table, SXSSFSheet sheet, CursorRow cursorRow) {
        // create caption
        createBlankRow(table.getCaption(), sheet, titleStyle, cursorRow, table.getTotalLeafCount());

        // 约定非叶子节点不能跨行
        Set<Integer> rows = new HashSet<>();
        int beginCol, endRow, endCol, nodeLevel = 1;
        //sheet.trackAllColumnsForAutoSizing();
        for (int n = table.getThead().size(), i = 0; i < n; i++) {
            Thead cell = table.getThead().get(i);
            if (cell.getNodeLevel() > nodeLevel) {
                nodeLevel = cell.getNodeLevel();
                cursorRow.increment();
            }

            beginCol = cell.getLeftLeafCount();
            endCol = beginCol + cell.getChildLeafCount() - 1;
            if (cell.isLeaf()) {
                endRow = cursorRow.get() + table.getMaxTheadLevel() - cell.getNodeLevel();
                sheet.setColumnWidth(beginCol, DEFAULT_WIDTH);
                //sheet.autoSizeColumn(beginCol);
            } else {
                endRow = cursorRow.get(); // 约定非子节点不能跨行
            }

            SXSSFRow colRow;
            if (rows.add(cursorRow.get())) {
                colRow = sheet.createRow(cursorRow.get()); // 还未创建该行
                colRow.setHeight(DEFAULT_HEIGHT);
            } else {
                colRow = sheet.getRow(cursorRow.get());
            }
            createCell(colRow, beginCol, headStyle, cell.getName());

            // -----------------------------设置被合并单元格的样式------------------------------ //
            for (int b = beginCol + 1; b <= endCol; b++) { // 列
                createCell(colRow, b, headStyle, null);
            }
            for (int a = cursorRow.get() + 1; a <= endRow; a++) { // 行
                if (rows.add(a)) { // 行未创建
                    colRow = sheet.createRow(a);
                    colRow.setHeight(DEFAULT_HEIGHT);
                } else { // 行已创建
                    colRow = sheet.getRow(a);
                }

                for (int b = beginCol; b <= endCol; b++) {
                    createCell(colRow, b, headStyle, null);
                }
            }
            if (cursorRow.get() != endRow || beginCol != endCol) {
                sheet.addMergedRegion(new CellRangeAddress(cursorRow.get(), endRow, beginCol, endCol));
            }
            // -----------------------------设置被合并单元格的样式------------------------------ //
        }
        cursorRow.increment();
    }

    /**
     * 创建空行
     * @param text
     * @param sheet
     * @param style
     * @param cursorRow
     * @param columnLen
     */
    private void createBlankRow(String text, SXSSFSheet sheet, XSSFCellStyle style, 
                                CursorRow cursorRow, int columnLen) {
        SXSSFRow row = sheet.createRow(cursorRow.get());
        row.setHeight(DEFAULT_HEIGHT);
        createCell(row, 0, style, text);
        for (int i = 1; i < columnLen; i++) {
            createCell(row, i, style, null);
        }
        sheet.addMergedRegion(new CellRangeAddress(cursorRow.get(), cursorRow.get(), 0, columnLen - 1));
        cursorRow.increment();
    }

    private void createCell(SXSSFRow row, int colIndex, XSSFCellStyle style, Tmeta tmeta, Object value) {
        createCell(row, colIndex, style, tmeta, value, -1, -1, null);
    }

    private void createCell(SXSSFRow row, int colIndex, XSSFCellStyle style, Object value) {
        createCell(row, colIndex, style, null, value, -1, -1, null);
    }

    /**
     * 创建单元格
     * @param row
     * @param colIndex
     * @param style
     * @param tmeta
     * @param value
     * @param tbodyRowIdx
     * @param tbodyColIdx
     * @param options
     */
    private void createCell(SXSSFRow row, int colIndex, XSSFCellStyle style, Tmeta tmeta, Object value, 
                            int tbodyRowIdx, int tbodyColIdx, Map<CellStyleOptions, Object> options) {

        SXSSFCell cell = row.createCell(colIndex);
        cell.setCellStyle(style);

        // 设置单元格格式
        if (tmeta == null) {
            setCellString(cell, value);
        } else if (tmeta.getType() == Type.NUMERIC) {
            if (Strings.isBlank(value)) {
                cell.setCellType(CellType.NUMERIC);
                cell.setCellValue(new XSSFRichTextString());
            } else if (String.class.isInstance(value) && ((String) value).endsWith("%")) {
                String val = ((String) value).substring(0, ((String) value).length() - 1);
                cell.setCellValue(Numbers.toDouble(val.replace(",", "")) / 100);
            } else {
                cell.setCellValue(Numbers.toDouble(value.toString().replace(",", "")));
            }
        } else if (tmeta.getType() == Type.DATETIME) {
            if (value == null) {
                cell.setCellType(CellType.BLANK);
            } else if (value instanceof Date) {
                cell.setCellValue((Date) value);
            } else if (value instanceof Calendar) {
                cell.setCellValue((Calendar) value);
            } else {
                String str = value.toString();
                String format = ObjectUtils.ifNull(tmeta.getFormat(), Dates.DEFAULT_DATE_FORMAT);
                try {
                    cell.setCellValue(DateUtils.parseDate(str, format));
                } catch (ParseException e) {
                    throw new IllegalArgumentException("invalid date str: " + str + ", format: " + format, e);
                }
            }
        } else {
            setCellString(cell, value);
        }

        // 样式自定义处理
        processOptions(cell, tbodyRowIdx, tbodyColIdx, options);
    }

    /**
     * 处理其它配置项
     * @param cell
     * @param tbodyRowIdx
     * @param tbodyColIdx
     * @param options
     */
    @SuppressWarnings("unchecked")
    private void processOptions(SXSSFCell cell, int tbodyRowIdx, int tbodyColIdx, 
                                Map<CellStyleOptions, Object> options) {
        if (options == null || options.isEmpty()) {
            return;
        }

        // 单元格高亮显示
        Map<String, Object> highlight = (Map<String, Object>) options.get(CellStyleOptions.HIGHLIGHT);
        if (highlight != null && !highlight.isEmpty()) {
            for (List<Integer> c : (List<List<Integer>>) highlight.get("cells")) {
                if (c.get(0) == tbodyRowIdx && c.get(1) == tbodyColIdx) {
                    XSSFFont font = (XSSFFont) workbook.createFont();
                    font.setColor(new XSSFColor(Colors.hex2color((String) highlight.get("color"))));
                    XSSFCellStyle style = (XSSFCellStyle) workbook.createCellStyle();
                    style.cloneStyleFrom(cell.getCellStyle());
                    style.setFont(font);
                    cell.setCellStyle(style);
                }
            }
        }

        // 处理
        Consumer<Object[]> processor = (Consumer<Object[]>) options.get(CellStyleOptions.CELL_PROCESS);
        if (processor != null) {
            processor.accept(new Object[] { workbook, cell, tbodyRowIdx, tbodyColIdx });
        }
    }

    /**
     * create cell style, only called once
     * @param thead
     * @return
     */
    private List<XSSFCellStyle> createStyles(List<Thead> thead) {
        List<XSSFCellStyle> styles = new ArrayList<>();
        for (Thead cell : thead) {
            if (!cell.isLeaf()) {
                continue; // 非叶子节点
            }

            XSSFCellStyle style = (XSSFCellStyle) workbook.createCellStyle();
            styles.add(style);
            style.cloneStyleFrom(dataStyle);

            Tmeta tmeta = cell.getTmeta();
            if (tmeta != null) {
                switch (tmeta.getAlign()) { // 对齐方式
                    case LEFT:
                        style.setAlignment(HorizontalAlignment.LEFT);
                        break;
                    case CENTER:
                        style.setAlignment(HorizontalAlignment.CENTER);
                        break;
                    case RIGHT:
                        style.setAlignment(HorizontalAlignment.RIGHT);
                        break;
                    default:
                        break;
                }

                // 设置单元格格式
                if (StringUtils.isNotBlank(tmeta.getFormat())) {
                    //dataFormat.getFormat("0.00%")： 0.00%->0xa; #,###.00%->0xa5; #,##0->xxx;
                    style.setDataFormat(dataFormat.getFormat(tmeta.getFormat()));
                }

                // 设置颜色
                if (tmeta.getColor() != null) {
                    XSSFFont font = (XSSFFont) workbook.createFont();
                    font.setColor(new XSSFColor(tmeta.getColor()));
                    style.setFont(font);
                }
            } // end of tmeta

        }

        return styles;
    }

    private static void setCellString(SXSSFCell cell, Object value) {
        if (value != null) {
            cell.setCellValue(value.toString());
        } else {
            cell.setCellType(CellType.BLANK);
        }
    }

    /**
     * 游标行
     */
    @SuppressWarnings("unused")
    private static final class CursorRow {
        int current;

        CursorRow() {
            this(0);
        }

        CursorRow(int initValue) {
            this.current = initValue;
        }

        int getAndIncrement() {
            return this.current++;
        }

        int incrementAndGet() {
            return ++this.current;
        }

        int getAndDecrement() {
            return this.current--;
        }

        int decrementAndGet() {
            return --this.current;
        }

        void add(int i) {
            this.current += i;
        }

        int addAndGet(int i) {
            this.current += i;
            return this.current;
        }

        int getAndAdd(int i) {
            int temp = this.current;
            this.current += i;
            return temp;
        }

        void set(int i) {
            this.current = i;
        }

        int getAndSet(int i) {
            int temp = this.current;
            this.current = i;
            return temp;
        }

        int get() {
            return this.current;
        }

        void increment() {
            this.current++;
        }
    }

    /**
     * 窗口冻结
     */
    @SuppressWarnings("unused")
    private static final class Freeze {
        boolean freeze = true;
        final int colSplit;
        final int rowSplit;

        Freeze(int colSplit, int rowSplit) {
            this.colSplit = colSplit;
            this.rowSplit = rowSplit;
        }

        void enable() {
            freeze = true;
        }

        void disable() {
            freeze = false;
        }
    }

}
