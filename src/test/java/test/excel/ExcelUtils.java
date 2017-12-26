package test.excel;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ComparatorUtils;
import org.apache.commons.collections4.comparators.ComparableComparator;
import org.apache.commons.collections4.comparators.ComparatorChain;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.streaming.SXSSFCell;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;

import code.ponfee.commons.math.Numbers;
import code.ponfee.commons.reflect.Fields;

/**
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class ExcelUtils {

    private static Logger logger = LoggerFactory.getLogger(ExcelUtils.class);

    /**
     * 用来验证excel与Vo中的类型是否一致 <br>
     * Map<栏位类型,只能是哪些Cell类型>
     */
    private static final Map<Class<?>, CellType[]> VALIDATE_MAP = ImmutableMap.<Class<?>, CellType[]> builder()
        .put(String[].class, new CellType[] {CellType.STRING })
        .put(Double[].class, new CellType[] { CellType.NUMERIC })
        .put(String.class, new CellType[] {CellType.STRING })
        .put(Double.class, new CellType[] { CellType.NUMERIC })
        .put(Date.class, new CellType[] { CellType.NUMERIC, CellType.STRING })
        .put(Integer.class, new CellType[] { CellType.NUMERIC })
        .put(Float.class, new CellType[] { CellType.NUMERIC })
        .put(Long.class, new CellType[] { CellType.NUMERIC })
        .put(Boolean.class, new CellType[] { CellType.BOOLEAN })
        .build();

    private static final Map<CellType, String> CELLTYPE_MAPPING = ImmutableMap.<CellType, String> builder()
        .put(CellType.BLANK, "Null type")
        .put(CellType.BOOLEAN, "Boolean type")
        .put(CellType.ERROR, "Error type")
        .put(CellType.FORMULA, "Formula type")
        .put(CellType.NUMERIC, "Numeric type")
        .put(CellType.STRING, "String type")
        .build();

    /**
     * 利用JAVA的反射机制，将放置在JAVA集合中并且符号一定条件的数据以EXCEL 的形式输出到指定IO设备上<br>
     * 用于单个sheet
     *
     * @param <T>
     * @param headers 表格属性列名数组
     * @param dataset 需要显示的数据集合,集合中一定要放置符合javabean风格的类的对象。此方法支持的
     *                javabean属性的数据类型有基本数据类型及String,Date,String[],Double[]
     * @param out     与输出设备关联的流对象，可以将EXCEL文档导出到本地文件或者网络中
     */
    public static <T> void write(Map<String, String> headers, Collection<T> dataset, OutputStream out) {
        write(headers, dataset, out, null);
    }

    /**
     * 利用JAVA的反射机制，将放置在JAVA集合中并且符号一定条件的数据以EXCEL 的形式输出到指定IO设备上<br>
     * 用于单个sheet
     *
     * @param <T>
     * @param headers 表格属性列名数组
     * @param dataset 需要显示的数据集合,集合中一定要放置符合javabean风格的类的对象。此方法支持的
     *                javabean属性的数据类型有基本数据类型及String,Date,String[],Double[]
     * @param out     与输出设备关联的流对象，可以将EXCEL文档导出到本地文件或者网络中
     * @param pattern 如果有时间数据，设定输出格式。默认为"yyy-MM-dd"
     */
    public static <T> void write(Map<String, String> headers, Collection<T> dataset,
                                 OutputStream out, String pattern) {
        try (SXSSFWorkbook workbook = new SXSSFWorkbook();) {
            write(workbook.createSheet(), headers, dataset, pattern);
            workbook.write(out);
        } catch (IOException e) {
            logger.error(e.toString(), e);
        }
    }

    public static <T extends Object> void write(List<T[]> datalist, OutputStream out) {
        try (SXSSFWorkbook workbook = new SXSSFWorkbook()) {
            SXSSFSheet sheet = workbook.createSheet();
            for (int i = 0; i < datalist.size(); i++) {
                SXSSFRow row = sheet.createRow(i);
                T[] record = datalist.get(i);
                for (int j = 0; j < record.length; j++) {
                    if (record[j] == null) {
                        continue;
                    }

                    String value = record[j].toString();
                    SXSSFCell cell = row.createCell(j);
                    //cell max length 32767
                    if (value.length() > 32767) {
                        value = "--此字段过长(超过32767)，已被截断--" + value;
                        value = value.substring(0, 32766);
                    }
                    cell.setCellValue(value);
                }
            }
            //自动列宽
            if (datalist.size() > 0) {
                int colcount = datalist.get(0).length;
                for (int i = 0; i < colcount; i++) {
                    sheet.autoSizeColumn(i);
                }
            }
            workbook.write(out);
        } catch (IOException e) {
            logger.error(e.toString(), e);
        }
    }

    public static <T extends Object> void write(List<T[]> datalist, ExcelCellProcessor<T> process, OutputStream out) {
        try (SXSSFWorkbook workbook = new SXSSFWorkbook()) {
            SXSSFSheet sheet = workbook.createSheet();
            for (int i = 0; i < datalist.size(); i++) {
                SXSSFRow row = sheet.createRow(i);
                T[] record = datalist.get(i);
                for (int j = 0; j < record.length; j++) {
                    process.process(workbook, row.createCell(j), record[j], i, j);
                }
            }
            //自动列宽
            if (datalist.size() > 0) {
                int colcount = datalist.get(0).length;
                for (int i = 0; i < colcount; i++) {
                    sheet.autoSizeColumn(i);
                }
            }
            workbook.write(out);
        } catch (IOException e) {
            logger.error(e.toString(), e);
        }
    }

    public static <T extends Object> void write(String[] headers, List<T[]> datalist,
        ExcelCellProcessor<T> process, OutputStream out) {
        try (SXSSFWorkbook workbook = new SXSSFWorkbook()) {
            XSSFCellStyle headStyle = (XSSFCellStyle) workbook.createCellStyle();
            headStyle.setBorderLeft(BorderStyle.THIN);
            headStyle.setBorderTop(BorderStyle.THIN);
            headStyle.setBorderRight(BorderStyle.THIN);
            headStyle.setBorderBottom(BorderStyle.THIN);
            headStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            headStyle.setAlignment(HorizontalAlignment.CENTER);
            headStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headStyle.setFillForegroundColor(new XSSFColor(new Color(192, 192, 192)));
            XSSFFont headFont = (XSSFFont) workbook.createFont();
            headFont.setBold(true);
            headFont.setFontName("宋体");
            headStyle.setFont(headFont);
            headStyle.setWrapText(false);

            SXSSFSheet sheet = workbook.createSheet();
            SXSSFRow row = sheet.createRow(0);
            SXSSFCell cell;
            for (int n = headers.length, j = 0; j < n; j++) {
                cell = row.createCell(j);
                cell.setCellValue(headers[j]);
                cell.setCellStyle(headStyle);
            }

            for (int n = datalist.size(), i = 0; i < n; i++) {
                row = sheet.createRow(i + 1);
                T[] record = datalist.get(i);
                for (int j = 0; j < record.length; j++) {
                    process.process(workbook, row.createCell(j), record[j], i, j);
                }
            }
            //自动列宽
            if (datalist.size() > 0) {
                int colcount = datalist.get(0).length;
                for (int i = 0; i < colcount; i++) {
                    sheet.autoSizeColumn(i);
                }
            }
            workbook.write(out);
        } catch (IOException e) {
            logger.error(e.toString(), e);
        }
    }

    /**
     * 利用JAVA的反射机制，将放置在JAVA集合中并且符号一定条件的数据以EXCEL 的形式输出到指定IO设备上<br>
     * 用于多个sheet
     *
     * @param <T>
     * @param sheets {@link ExcelSheet}的集合
     * @param out    与输出设备关联的流对象，可以将EXCEL文档导出到本地文件或者网络中
     */
    public static <T> void export(List<ExcelSheet<T>> sheets, OutputStream out) {
        export(sheets, out, null);
    }

    /**
     * 利用JAVA的反射机制，将放置在JAVA集合中并且符号一定条件的数据以EXCEL 的形式输出到指定IO设备上<br>
     * 用于多个sheet
     *
     * @param <T>
     * @param sheets  {@link ExcelSheet}的集合
     * @param out     与输出设备关联的流对象，可以将EXCEL文档导出到本地文件或者网络中
     * @param pattern 如果有时间数据，设定输出格式。默认为"yyy-MM-dd"
     */
    public static <T> void export(List<ExcelSheet<T>> sheets, OutputStream out, String pattern) {
        if (CollectionUtils.isEmpty(sheets)) {
            return;
        }
        try (SXSSFWorkbook workbook = new SXSSFWorkbook()) {
            for (ExcelSheet<T> sheet : sheets) {
                SXSSFSheet XSSFSheet = workbook.createSheet(sheet.getSheetName());
                write(XSSFSheet, sheet.getHeaders(), sheet.getDataset(), pattern);
            }
            workbook.write(out);
        } catch (IOException e) {
            logger.error(e.toString(), e);
        }
    }

    /**
     * 把Excel的数据封装成voList
     *
     * @param clazz       vo的Class
     * @param inputStream excel输入流
     * @param pattern     如果有时间数据，设定输入格式。默认为"yyy-MM-dd"
     * @param logs        错误log集合
     * @param arrayCount  如果vo中有数组类型,那就按照index顺序,把数组应该有几个值写上.
     * @return voList
     * @throws RuntimeException
     */
    public static <T> Collection<T> read(Class<T> clazz, InputStream inputStream,
                                         String pattern, ExcelLogs logs, Integer... arrayCount) {
        Workbook workBook;
        try {
            workBook = WorkbookFactory.create(inputStream);
        } catch (Exception e) {
            logger.error("load excel file error", e);
            return null;
        }
        List<T> list = new ArrayList<>();
        Iterator<Row> rowIterator = workBook.getSheetAt(0).rowIterator();
        try {
            List<ExcelLog> logList = new ArrayList<>();
            // Map<title,index>
            Map<String, Integer> titleMap = new HashMap<>();

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                if (row.getRowNum() == 0) {
                    if (clazz == Map.class) {
                        // 解析map用的key,就是excel标题行
                        Iterator<Cell> cellIterator = row.cellIterator();
                        Integer index = 0;
                        while (cellIterator.hasNext()) {
                            String value = cellIterator.next().getStringCellValue();
                            titleMap.put(value, index);
                            index++;
                        }
                    }
                    continue;
                }
                // 整行都空，就跳过
                boolean allRowIsNull = true;
                Iterator<Cell> cellIterator = row.cellIterator();
                while (cellIterator.hasNext()) {
                    Object cellValue = getCellValue(cellIterator.next());
                    if (cellValue != null) {
                        allRowIsNull = false;
                        break;
                    }
                }
                if (allRowIsNull) {
                    logger.warn("Excel row " + row.getRowNum() + " all row value is null!");
                    continue;
                }
                StringBuilder log = new StringBuilder();
                if (clazz == Map.class) {
                    Map<String, Object> map = new HashMap<>();
                    for (String k : titleMap.keySet()) {
                        Integer index = titleMap.get(k);
                        Cell cell = row.getCell(index);
                        // 判空
                        if (cell == null) {
                            map.put(k, null);
                        } else {
                            cell.setCellType(CellType.STRING);
                            String value = cell.getStringCellValue();
                            map.put(k, value);
                        }
                    }
                    list.add((T) map);
                } else {
                    T t = clazz.newInstance();
                    int arrayIndex = 0;// 标识当前第几个数组了
                    int cellIndex = 0;// 标识当前读到这一行的第几个cell了
                    List<FieldForSortting> fields = sortFieldByAnno(clazz);
                    for (FieldForSortting ffs : fields) {
                        Field field = ffs.getField();
                        field.setAccessible(true);
                        if (field.getType().isArray()) {
                            Integer count = arrayCount[arrayIndex];
                            Object[] value;
                            if (field.getType().equals(String[].class)) {
                                value = new String[count];
                            } else {
                                // 目前只支持String[]和Double[]
                                value = new Double[count];
                            }
                            for (int i = 0; i < count; i++) {
                                Cell cell = row.getCell(cellIndex);
                                String errMsg = validateCell(cell, field, cellIndex);
                                if (StringUtils.isBlank(errMsg)) {
                                    value[i] = getCellValue(cell);
                                } else {
                                    log.append(errMsg);
                                    log.append(";");
                                    logs.setHasError(true);
                                }
                                cellIndex++;
                            }
                            field.set(t, value);
                            arrayIndex++;
                        } else {
                            Cell cell = row.getCell(cellIndex);
                            String errMsg = validateCell(cell, field, cellIndex);
                            if (StringUtils.isBlank(errMsg)) {
                                Object value = null;
                                // 处理特殊情况,Excel中的String,转换成Bean的Date
                                if (field.getType().equals(Date.class)
                                    && cell.getCellTypeEnum() == CellType.STRING) {
                                    Object strDate = getCellValue(cell);
                                    try {
                                        value = new SimpleDateFormat(pattern).parse(strDate.toString());
                                    } catch (ParseException e) {

                                        errMsg =
                                            MessageFormat.format("the cell [{0}] can not be converted to a date ", CellReference.convertNumToColString(cell.getColumnIndex()));
                                    }
                                } else {
                                    value = getCellValue(cell);
                                    // 处理特殊情况,excel的value为String,且bean中为其他,且defaultValue不为空,那就=defaultValue
                                    ExcelCell annoCell = field.getAnnotation(ExcelCell.class);
                                    if (value instanceof String && !field.getType().equals(String.class)
                                        && StringUtils.isNotBlank(annoCell.defaultValue())) {
                                        value = annoCell.defaultValue();
                                    }
                                }
                                field.set(t, value);
                            }
                            if (StringUtils.isNotBlank(errMsg)) {
                                log.append(errMsg);
                                log.append(";");
                                logs.setHasError(true);
                            }
                            cellIndex++;
                        }
                    }
                    list.add(t);
                    logList.add(new ExcelLog(t, log.toString(), row.getRowNum() + 1));
                }
            }
            logs.setLogList(logList);
        } catch (InstantiationException e) {
            throw new RuntimeException(MessageFormat.format("can not instance class:{0}", clazz.getSimpleName()), e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(MessageFormat.format("can not instance class:{0}", clazz.getSimpleName()), e);
        }
        return list;
    }

    /**
     * 验证
     * @param cell
     * @param field
     * @param cellNum 
     * @return
     */
    private static String validateCell(Cell cell, Field field, int cellNum) {
        String columnName = CellReference.convertNumToColString(cellNum);
        String result = null;
        CellType[] cellTypeArr = VALIDATE_MAP.get(field.getType());
        if (cellTypeArr == null) {
            result = MessageFormat.format("Unsupported type [{0}]", field.getType().getSimpleName());
            return result;
        }
        ExcelCell annoCell = field.getAnnotation(ExcelCell.class);
        if (cell == null
            || (cell.getCellTypeEnum() == CellType.STRING && StringUtils.isBlank(cell.getStringCellValue()))) {
            if (annoCell != null && annoCell.valid().allowNull() == false) {
                result = MessageFormat.format("the cell [{0}] can not null", columnName);
            }
            ;
        } else if (cell.getCellTypeEnum() == CellType.BLANK && annoCell.valid().allowNull()) {
            return result;
        } else {
            List<CellType> cellTypes = Arrays.asList(cellTypeArr);

            // 如果類型不在指定範圍內,並且沒有默認值
            if (!(cellTypes.contains(cell.getCellTypeEnum()))
                || StringUtils.isNotBlank(annoCell.defaultValue())
                    && cell.getCellTypeEnum() == CellType.STRING) {
                StringBuilder strType = new StringBuilder();
                for (int i = 0; i < cellTypes.size(); i++) {
                    strType.append(getValue(cellTypes.get(i)));
                    if (i != cellTypes.size() - 1) {
                        strType.append(",");
                    }
                }
                result =
                    MessageFormat.format("the cell [{0}] type must [{1}]", columnName, strType.toString());
            } else {
                // 类型符合验证,但值不在要求范围内的
                // String in
                if (annoCell.valid().in().length != 0 && cell.getCellTypeEnum() == CellType.STRING) {
                    String[] in = annoCell.valid().in();
                    String cellValue = cell.getStringCellValue();
                    boolean isIn = false;
                    for (String str : in) {
                        if (str.equals(cellValue)) {
                            isIn = true;
                        }
                    }
                    if (!isIn) {
                        result = MessageFormat.format("the cell [{0}] value must in {1}", columnName, in);
                    }
                }
                // 数字型
                if (cell.getCellTypeEnum() == CellType.NUMERIC) {
                    double cellValue = cell.getNumericCellValue();
                    // 小于
                    if (!Double.isNaN(annoCell.valid().lt())) {
                        if (!(cellValue < annoCell.valid().lt())) {
                            result =
                                MessageFormat.format("the cell [{0}] value must less than [{1}]", columnName, annoCell.valid().lt());
                        }
                    }
                    // 大于
                    if (!Double.isNaN(annoCell.valid().gt())) {
                        if (!(cellValue > annoCell.valid().gt())) {
                            result =
                                MessageFormat.format("the cell [{0}] value must greater than [{1}]", columnName, annoCell.valid().gt());
                        }
                    }
                    // 小于等于
                    if (!Double.isNaN(annoCell.valid().le())) {
                        if (!(cellValue <= annoCell.valid().le())) {
                            result =
                                MessageFormat.format("the cell [{0}] value must less than or equal [{1}]", columnName, annoCell.valid().le());
                        }
                    }
                    // 大于等于
                    if (!Double.isNaN(annoCell.valid().ge())) {
                        if (!(cellValue >= annoCell.valid().ge())) {
                            result =
                                MessageFormat.format("the cell [{0}] value must greater than or equal [{1}]", columnName, annoCell.valid().ge());
                        }
                    }
                }
            }
        }
        return result;
    }

    /**
     * 根据annotation的seq排序后的栏位
     *
     * @param clazz
     * @return
     */
    private static List<FieldForSortting> sortFieldByAnno(Class<?> clazz) {
        Field[] fieldsArr = clazz.getDeclaredFields();
        List<FieldForSortting> fields = new ArrayList<>();
        List<FieldForSortting> annoNullFields = new ArrayList<>();
        for (Field field : fieldsArr) {
            ExcelCell ec = field.getAnnotation(ExcelCell.class);
            if (ec == null) {
                // 没有ExcelCell Annotation 视为不汇入
                continue;
            }
            int id = ec.index();
            fields.add(new FieldForSortting(field, id));
        }
        fields.addAll(annoNullFields);
        sortByProperties(fields, true, false, "index");
        return fields;
    }

    private static <T extends Object> void sortByProperties(List<T> list, boolean isNullHigh,
        boolean isReversed, String... props) {
        if (CollectionUtils.isNotEmpty(list)) {
            Comparator<?> typeComp = ComparableComparator.INSTANCE;
            if (isNullHigh == true) {
                typeComp = ComparatorUtils.nullHighComparator(typeComp);
            } else {
                typeComp = ComparatorUtils.nullLowComparator(typeComp);
            }
            if (isReversed) {
                typeComp = ComparatorUtils.reversedComparator(typeComp);
            }

            List<Object> sortCols = new ArrayList<Object>();

            if (props != null) {
                for (String prop : props) {
                    sortCols.add(new BeanComparator<T>(prop, typeComp));
                }
            }
            if (sortCols.size() > 0) {
                Comparator<Object> sortChain = new ComparatorChain(sortCols);
                Collections.sort(list, sortChain);
            }
        }
    }

    private static final String getValue(CellType type) {
        return type == null ? "Unknown type" : CELLTYPE_MAPPING.get(type);
    }

    /**
     * 获取单元格值
     *
     * @param cell
     * @return
     */
    private static Object getCellValue(Cell cell) {
        if (cell == null
            || (cell.getCellTypeEnum() == CellType.STRING && StringUtils.isBlank(cell.getStringCellValue()))) {
            return null;
        }
        CellType cellType = cell.getCellTypeEnum();
        if (cellType == CellType.BLANK) return null;
        else if (cellType == CellType.BOOLEAN) return cell.getBooleanCellValue();
        else if (cellType == CellType.ERROR) return cell.getErrorCellValue();
        else if (cellType == CellType.FORMULA) {
            try {
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue();
                } else {
                    return cell.getNumericCellValue();
                }
            } catch (IllegalStateException e) {
                return cell.getRichStringCellValue();
            }
        } else if (cellType == CellType.NUMERIC) {
            if (DateUtil.isCellDateFormatted(cell)) {
                return cell.getDateCellValue();
            } else {
                return cell.getNumericCellValue();
            }
        } else if (cellType == CellType.STRING) return cell.getStringCellValue();
        else return null;
    }

    /**
     * 每个sheet的写入
     *
     * @param sheet   页签
     * @param headers 表头
     * @param dataset 数据集合
     * @param pattern 日期格式
     */
    private static <T> void write(SXSSFSheet sheet, Map<String, String> headers,
                                  Collection<T> dataset, String dateFormat) {
        if (StringUtils.isEmpty(dateFormat)) {
            dateFormat = "yyyy-MM-dd";
        }
        // 产生表格标题行
        SXSSFRow row = sheet.createRow(0);
        // 标题行转中文
        Set<String> keys = headers.keySet();
        Iterator<String> it1 = keys.iterator();
        String key = ""; //存放临时键变量
        int c = 0; //标题列数
        while (it1.hasNext()) {
            key = it1.next();
            if (headers.containsKey(key)) {
                SXSSFCell cell = row.createCell(c);
                XSSFRichTextString text = new XSSFRichTextString(headers.get(key));
                cell.setCellValue(text);
                c++;
            }
        }

        // 遍历集合数据，产生数据行
        int index = 0;
        for (Iterator<T> it = dataset.iterator(); it.hasNext();) {
            index++;
            row = sheet.createRow(index);
            T t = it.next();
            try {
                if (t instanceof Map) {
                    Map<String, Object> map = (Map<String, Object>) t;
                    int cellNum = 0;
                    //遍历列名
                    Iterator<String> it2 = keys.iterator();
                    while (it2.hasNext()) {
                        key = it2.next();
                        if (!headers.containsKey(key)) {
                            logger.error("Map 中 不存在 key [" + key + "]");
                            continue;
                        }
                        Object value = map.get(key);
                        SXSSFCell cell = row.createCell(cellNum);

                        cellNum = setCellValue(cell, value, dateFormat, cellNum, null, row);

                        cellNum++;
                    }
                } else {
                    List<FieldForSortting> fields = sortFieldByAnno(t.getClass());
                    int cellNum = 0;
                    for (int i = 0; i < fields.size(); i++) {
                        SXSSFCell cell = row.createCell(cellNum);
                        Field field = fields.get(i).getField();
                        field.setAccessible(true);
                        Object value = field.get(t);

                        cellNum = setCellValue(cell, value, dateFormat, cellNum, field, row);

                        cellNum++;
                    }
                }
            } catch (Exception e) {
                logger.error(e.toString(), e);
            }
        }
        // 设定自动宽度
        for (int i = 0; i < headers.size(); i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private static int setCellValue(SXSSFCell cell, Object value, String pattern, int cellNum, Field field, SXSSFRow row) {
        String textValue = null;
        if (value instanceof Number) {
            cell.setCellValue(Numbers.toDouble(value));
        } else if (value instanceof Boolean) {
            cell.setCellValue((boolean) value);
        } else if (value instanceof Date) {
            textValue = DateFormatUtils.format((Date) value, pattern);
        } else if (value instanceof String[]) {
            String[] strArr = (String[]) value;
            for (int j = 0; j < strArr.length; j++) {
                String str = strArr[j];
                cell.setCellValue(str);
                if (j != strArr.length - 1) {
                    cellNum++;
                    cell = row.createCell(cellNum);
                }
            }
        } else if (value instanceof Double[]) {
            Double[] douArr = (Double[]) value;
            for (int j = 0; j < douArr.length; j++) {
                Double val = douArr[j];
                // 值不为空则set Value
                if (val != null) {
                    cell.setCellValue(val);
                }
                if (j != douArr.length - 1) {
                    cellNum++;
                    cell = row.createCell(cellNum);
                }
            }
        } else {
            // 其它数据类型都当作字符串简单处理
            String empty = StringUtils.EMPTY;
            if (field != null) {
                ExcelCell anno = field.getAnnotation(ExcelCell.class);
                if (anno != null) {
                    empty = anno.defaultValue();
                }
            }
            textValue = (value == null) ? empty : value.toString();
        }
        if (textValue != null) {
            XSSFRichTextString richString = new XSSFRichTextString(textValue);
            cell.setCellValue(richString);
        }
        return cellNum;
    }

    private static class BeanComparator<T> implements Comparator<T>, Serializable {
        private static final long serialVersionUID = -2328076680442232058L;

        private String property;
        private final Comparator<?> comparator;

        public BeanComparator(final String property, final Comparator<?> comparator) {
            setProperty(property);
            if (comparator != null) {
                this.comparator = comparator;
            } else {
                this.comparator = ComparableComparator.INSTANCE;
            }
        }

        public void setProperty(final String property) {
            this.property = property;
        }

        public int compare(final T o1, final T o2) {
            if (property == null) {
                return internalCompare(o1, o2);
            }

            final Object value1 = Fields.get(o1, property);
            final Object value2 = Fields.get(o2, property);
            return internalCompare(value1, value2);
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof BeanComparator)) {
                return false;
            }

            final BeanComparator<?> beanComparator = (BeanComparator<?>) o;

            if (!comparator.equals(beanComparator.comparator)) {
                return false;
            }
            if (property != null) {
                if (!property.equals(beanComparator.property)) {
                    return false;
                }
            } else {
                return (beanComparator.property == null);
            }
            return true;
        }

        @Override
        public int hashCode() {
            return comparator.hashCode();
        }

        private int internalCompare(final Object val1, final Object val2) {
            final Comparator c = comparator;
            return c.compare(val1, val2);
        }
    }

}
