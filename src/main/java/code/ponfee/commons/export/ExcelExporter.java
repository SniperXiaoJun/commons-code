package code.ponfee.commons.export;

import java.awt.Color;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.ClientAnchor.AnchorType;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFDataFormat;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import code.ponfee.commons.export.Tmeta.Type;
import code.ponfee.commons.util.Colors;
import code.ponfee.commons.util.ImageUtils;
import code.ponfee.commons.util.ObjectUtils;

/**
 * excel导出
 * @author fupf
 */
public class ExcelExporter extends AbstractExporter {
    private static Logger logger = LoggerFactory.getLogger(ExcelExporter.class);

    /** row and cell config，默认的列宽和行高大小 */
    private static final int DEFAULT_WIDTH = 3200;
    private static final short DEFAULT_HEIGHT = 350;

    /** nested image config，office 2013版本 */
    private static final double RATE_WIDTH = 70.5; // 图片真实宽度与excel列宽的比例
    private static final double RATE_HEIGHT = 18; // 图片真实高度与excel行高的比例

    /** 作为分隔符（类似html的<hr />）的合并列数目 */
    private static final int MARGIN_ROW_CELL_SIZE = 26;

    private XSSFWorkbook workbook; // excel
    private final XSSFCellStyle titleStyle; // 标题样式
    private final XSSFCellStyle headStyle; // 表头样式
    private final XSSFCellStyle dataStyle; // 数据样式
    private final XSSFCellStyle tfootStyle; // 合计行样式
    private final XSSFCellStyle noneStyle; // 无样式
    private final XSSFCellStyle tipStyle; // 无样式
    private final XSSFDataFormat dataFormat; // 数据格式

    private final Map<String, XSSFSheet> sheets = new HashMap<>();
    private final Map<String, Integer> images   = new HashMap<>();
    private final Map<String, Freeze> freezes   = new HashMap<>();

    public ExcelExporter() {
        workbook = new XSSFWorkbook();
        dataFormat = workbook.createDataFormat();

        XSSFFont titleFont = workbook.createFont();
        titleFont.setBold(true);
        titleFont.setFontName("黑体");

        XSSFFont headFont = workbook.createFont();
        headFont.setBold(true);
        headFont.setFontName("宋体");

        XSSFFont redFont = workbook.createFont();
        redFont.setColor(new XSSFColor(new Color(255, 0, 0)));
        redFont.setFontName("宋体");

        XSSFCellStyle baseStyle = workbook.createCellStyle();
        baseStyle.setBorderLeft(BorderStyle.THIN);
        baseStyle.setBorderTop(BorderStyle.THIN);
        baseStyle.setBorderRight(BorderStyle.THIN);
        baseStyle.setBorderBottom(BorderStyle.THIN);
        baseStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        baseStyle.setWrapText(true);
        baseStyle.setDataFormat(dataFormat.getFormat("@"));

        titleStyle = workbook.createCellStyle();
        titleStyle.cloneStyleFrom(baseStyle);
        titleStyle.setAlignment(HorizontalAlignment.CENTER);
        titleStyle.setFont(titleFont);
        titleStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        titleStyle.setFillForegroundColor(new XSSFColor(new Color(255, 255, 224)));

        headStyle = workbook.createCellStyle();
        headStyle.cloneStyleFrom(baseStyle);
        headStyle.setAlignment(HorizontalAlignment.CENTER);
        headStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headStyle.setFillForegroundColor(new XSSFColor(new Color(192, 192, 192)));
        headStyle.setFont(headFont);
        headStyle.setWrapText(false);

        dataStyle = workbook.createCellStyle();
        dataStyle.cloneStyleFrom(baseStyle);

        tfootStyle = workbook.createCellStyle();
        tfootStyle.cloneStyleFrom(dataStyle);
        tfootStyle.setFont(headFont);
        tfootStyle.setWrapText(false);

        tipStyle = workbook.createCellStyle();
        tipStyle.cloneStyleFrom(baseStyle);
        tipStyle.setFont(redFont);

        noneStyle = workbook.createCellStyle();
        noneStyle.setVerticalAlignment(VerticalAlignment.CENTER);
    }

    /**
     * 构建excel
     */
    @Override
    public void build(Table table) {
        // 1、校验表头是否为空
        if (ObjectUtils.isEmpty(table.getThead())) {
            throw new IllegalArgumentException("thead can't be null");
        }

        String name = this.getName();
        // 2、获取或创建工作簿
        XSSFSheet sheet = getOrCreateSheet(name);

        // 3、判断工作簿是否已创建过行数据
        CursorRow cursorRow = new CursorRow(sheet.getLastRowNum());
        if (cursorRow.get() > 0) {
            // 创建两行空白行
            cursorRow.increment();
            int i = cursorRow.getAndIncrement(), j = cursorRow.getAndIncrement();
            XSSFRow row1 = sheet.createRow(i);
            row1.setHeight(DEFAULT_HEIGHT);
            XSSFRow row2 = sheet.createRow(j);
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
            freezes.put(name, new Freeze(1, cursorRow.get()));
        }

        // 6、判断是否有数据
        if (ObjectUtils.isEmpty(table.getTobdy()) && ObjectUtils.isEmpty(table.getTfoot())) {
            createCell(TIP_NO_RESULT, sheet, tipStyle, cursorRow, table.getTotalLeafCount());
        } else {
            super.nonEmpty();
            List<XSSFCellStyle> styles = createStyles(table.getThead());
            XSSFRow row;

            // 7、处理tbody数据
            if (!ObjectUtils.isEmpty(table.getTobdy())) {
                Object[] data;
                List<Object[]> tbody = table.getTobdy();
                Map<String, Object> options = table.getOptions();
                for (int n = tbody.size(), i = 0; i < n; i++) {
                    data = tbody.get(i);
                    row = sheet.createRow(cursorRow.getAndIncrement());
                    row.setHeight(DEFAULT_HEIGHT);
                    for (int m = data.length, j = 0; j < m; j++) {
                        createCell(row, j, styles.get(j), table.getThead().get(j).getTmeta(), data[j], i, j, options);
                    }
                }
            }

            // 8、处理tfoot数据
            if (!ObjectUtils.isEmpty(table.getTfoot())) {
                int rowNum = cursorRow.getAndIncrement();
                row = sheet.createRow(rowNum);
                row.setHeight(DEFAULT_HEIGHT);

                // 合计单元格
                XSSFCellStyle style = workbook.createCellStyle();
                style.cloneStyleFrom(tfootStyle);
                style.setAlignment(HorizontalAlignment.RIGHT);
                createCell(row, 0, style, "合计");
                int mergeNum = table.getTotalLeafCount() - table.getTfoot().length;
                for (int i = 1; i < mergeNum; i++) {
                    createCell(row, i, style, null);
                }
                sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 0, mergeNum - 1));

                // 合计数据
                for (int i = 0; i < table.getTfoot().length; i++) {
                    createCell(row, i + mergeNum, styles.get(mergeNum + i), table.getThead().get(mergeNum + i).getTmeta(), table.getTfoot()[i]);
                }
            }

            // 9、文字注释
            if (StringUtils.isNotBlank(table.getComment())) {
                createCell(table.getComment(), sheet, tipStyle, cursorRow, table.getTotalLeafCount());
            }
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
        if (ObjectUtils.isEmpty(imageBytes)) return;
        super.nonEmpty();

        XSSFSheet sheet = getOrCreateSheet(getName());
        int startRow = images.get(getName()) == null ? 1 : images.get(getName()), startCol = 1;
        int endCol = startCol + (int) Math.round(((double) width) / RATE_WIDTH);
        int endRow = startRow + (int) Math.round(((double) height) / RATE_HEIGHT);
        images.put(getName(), endRow + 2);

        XSSFDrawing drawing = sheet.createDrawingPatriarch();
        XSSFClientAnchor anchor = new XSSFClientAnchor(0, 0, width, height, startCol, startRow, (short) endCol, endRow);

        anchor.setAnchorType(AnchorType.DONT_MOVE_AND_RESIZE);
        drawing.createPicture(anchor, workbook.addPicture(imageBytes, XSSFWorkbook.PICTURE_TYPE_PNG));

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
            bos.flush();
            out.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void write(String filepath) {
        try (OutputStream out = new FileOutputStream(new File(filepath))) {
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
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        write(output);
        return output.toByteArray();
    }

    /**
     * 关闭
     */
    @Override
    public void close() {
        if (workbook != null) try {
            workbook.close();
        } catch (IOException e) {
            logger.error("closing XSSFWorkbook occur error", e);
        }
        workbook = null;
    }

    //--private methods------------------------------------------------------------------
    protected XSSFSheet getOrCreateSheet(String name) {
        XSSFSheet sheet = sheets.get(name);
        if (sheet == null) {
            sheet = workbook.createSheet(name);
            sheet.setDisplayGridlines(false);
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
        for (Entry<String, XSSFSheet> entry : sheets.entrySet()) {
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
    private void buildComplexThead(Table table, XSSFSheet sheet, CursorRow cursorRow) {
        // create caption
        createCell(table.getCaption(), sheet, titleStyle, cursorRow, table.getTotalLeafCount());

        // 约定非叶子节点不能跨行
        Set<Integer> rows = new HashSet<Integer>();
        int beginCol, endRow, endCol, nodeLevel = 1;
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
            } else {
                endRow = cursorRow.get(); // 约定非子节点不能跨行
            }

            XSSFRow colRow = null;
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

    // 创建单元格
    private void createCell(String text, XSSFSheet sheet, XSSFCellStyle style, CursorRow cursorRow, int columnLen) {
        XSSFRow row = sheet.createRow(cursorRow.get());
        row.setHeight((short) 500);
        createCell(row, 0, style, text);
        for (int i = 1; i < columnLen; i++) {
            createCell(row, i, style, null);
        }
        sheet.addMergedRegion(new CellRangeAddress(cursorRow.get(), cursorRow.get(), 0, columnLen - 1));
        cursorRow.increment();
    }

    private void createCell(XSSFRow row, int colIndex, XSSFCellStyle style, Tmeta tmeta, Object value) {
        createCell(row, colIndex, style, tmeta, value, -1, -1, null);
    }

    private void createCell(XSSFRow row, int colIndex, XSSFCellStyle style, Object value) {
        createCell(row, colIndex, style, null, value, -1, -1, null);
    }

    /**
     * 创建单元格
     * @param row
     * @param colIndex
     * @param style
     * @param tmeta
     * @param value
     * @param r
     * @param c
     * @param options
     */
    private void createCell(XSSFRow row, int colIndex, XSSFCellStyle style, Tmeta tmeta, 
                              Object value, int r, int c, Map<String, Object> options) {
        XSSFCell cell = row.createCell(colIndex);
        // 设置单元格格式
        if (tmeta != null && tmeta.getType() == Type.NUMERIC) {
            cell.setCellType(CellType.NUMERIC);
            if (value == null || (String.class.isInstance(value) && StringUtils.isBlank((String) value))) {
                cell.setCellValue(new XSSFRichTextString());
            } else if (String.class.isInstance(value) && ((String) value).endsWith("%")) {
                String val = (String) value;
                cell.setCellValue(Double.parseDouble(val.substring(0, val.length() - 1).replaceAll(",", "")) / 100);
            } else {
                cell.setCellValue(Double.parseDouble(value.toString().replaceAll(",", "")));
            }
        } else if (tmeta != null && tmeta.getType() == Type.DATETIME && StringUtils.isNotBlank(tmeta.getFormat())) {
            if (value != null) {
                try {
                    cell.setCellValue(new SimpleDateFormat(tmeta.getFormat()).parse(value.toString()));
                } catch (ParseException e) {
                    throw new IllegalArgumentException(e);
                }
            }
        } else {
            cell.setCellType(CellType.STRING);
            if (value != null) {
                cell.setCellValue(value.toString());
            }
        }
        cell.setCellStyle(style);

        processOptions(cell, r, c, options);
    }

    /**
     * 处理其它配置项
     * @param cell
     * @param row
     * @param col
     * @param options
     */
    @SuppressWarnings("unchecked")
    private void processOptions(XSSFCell cell, int row, int col, Map<String, Object> options) {
        if (ObjectUtils.isEmpty(options)) return;

        // 单元格高亮显示
        if (!ObjectUtils.isEmpty(options.get("highlight")) && row >= 0 && col >= 0) {
            Map<String, Object> highlight = (Map<String, Object>) options.get("highlight");
            for (List<Integer> c : (List<List<Integer>>) highlight.get("cells")) {
                if (c.get(0).equals(row) && c.get(1).equals(col)) {
                    XSSFFont font = workbook.createFont();
                    font.setColor(new XSSFColor(Colors.hex2color((String) highlight.get("color"))));
                    XSSFCellStyle style = workbook.createCellStyle();
                    style.cloneStyleFrom(cell.getCellStyle());
                    style.setFont(font);
                    cell.setCellStyle(style);
                }
            }
        }
    }

    /**
     * 创建样式
     * @param thead
     * @return
     */
    private List<XSSFCellStyle> createStyles(List<Thead> thead) {
        List<XSSFCellStyle> styles = new ArrayList<>();
        for (Thead cell : thead) {
            if (!cell.isLeaf()) continue; // 非叶子节点

            XSSFCellStyle style = workbook.createCellStyle();
            styles.add(style);
            style.cloneStyleFrom(dataStyle);

            Tmeta tmeta = cell.getTmeta();
            if (tmeta == null) continue;

            // 对齐方式
            switch (tmeta.getAlign()) {
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
                //dataFormat.getFormat("0.00%")： 0.00%->0xa; #,###.00%->0xa5; #,##0 -> xxx
                style.setDataFormat(dataFormat.getFormat(tmeta.getFormat()));
            }

            // 设置颜色
            if (tmeta.getColor() != null) {
                XSSFFont font = workbook.createFont();
                font.setColor(new XSSFColor(tmeta.getColor()));
                style.setFont(font);
            }
        }
        return styles;
    }

    /**
     * 游标行
     */
    @SuppressWarnings("unused")
    private static final class CursorRow {
        int current;

        CursorRow(int initValue) {
            this.current = initValue;
        }

        CursorRow() {
            this.current = 0;
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
