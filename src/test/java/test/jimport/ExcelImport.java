//package com.sf.ddt.tools.utils;
//
//import org.apache.commons.io.FilenameUtils;
//import org.apache.commons.lang3.StringUtils;
//import org.apache.poi.hssf.usermodel.HSSFCell;
//import org.apache.poi.hssf.usermodel.HSSFRow;
//import org.apache.poi.hssf.usermodel.HSSFWorkbook;
//import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
//import org.apache.poi.ss.usermodel.*;
//import org.apache.poi.xssf.usermodel.XSSFWorkbook;
//
//import java.beans.BeanInfo;
//import java.beans.IntrospectionException;
//import java.beans.Introspector;
//import java.beans.PropertyDescriptor;
//import java.io.*;
//import java.lang.reflect.InvocationTargetException;
//import java.lang.reflect.Method;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
///**
// * 描述：
// *
// * @author 80002023
// *         2016/11/3.
// * @version 1.0
// * @since JDK1.6
// */
//public class ExcelUtils {
//
//    /**
//     * Excel 2003
//     */
//    private final static String XLS = "xls";
//    /**
//     * Excel 2007
//     */
//    private final static String XLSX = "xlsx";
//    /**
//     * 分隔符
//     */
//    private final static String SEPARATOR = ":";
//
//    /**
//     * 由Excel文件的Sheet导出至List
//     *
//     * @param file
//     * @param sheetNum
//     * @return
//     */
//    public static List<String> exportListFromExcel(File file, int sheetNum, int cellNum)
//            throws IOException {
//        return exportListFromExcel(new FileInputStream(file), FilenameUtils.getExtension(file.getName()), sheetNum, cellNum);
//    }
//
//    /**
//     * 由Excel流的Sheet导出至List
//     *
//     * @param is
//     * @param extensionName
//     * @param sheetNum
//     * @return
//     * @throws IOException
//     */
//    public static List<String> exportListFromExcel(InputStream is,
//                                                   String extensionName, int sheetNum, int cellNum) throws IOException {
//
//        Workbook workbook = null;
//
//        if (extensionName.toLowerCase().equals(XLS)) {
//            workbook = new HSSFWorkbook(is);
//        } else if (extensionName.toLowerCase().equals(XLSX)) {
//            workbook = new XSSFWorkbook(is);
//        }
//
//        return exportListFromExcel(workbook, sheetNum, cellNum);
//    }
//
//    /**
//     * 由指定的Sheet导出至List
//     *
//     * @param workbook
//     * @param sheetNum
//     * @return
//     * @throws IOException
//     */
//    private static List<String> exportListFromExcel(Workbook workbook,
//                                                    int sheetNum, int cellNum) {
//        Sheet childSheet = workbook.getSheetAt(sheetNum);
//        List<String> list = new ArrayList<>();
//        for (int j = 0; j <= childSheet.getLastRowNum(); j++) {
//            HSSFRow row = (HSSFRow) childSheet.getRow(j);
//            StringBuilder sb = new StringBuilder();
//            if (null != row) {
//                for (int k = 0; k < row.getLastCellNum(); k++) {
//                    HSSFCell cell = row.getCell(k);
//                    if (null != cell) {
//                        switch (cell.getCellType()) {
//                            case HSSFCell.CELL_TYPE_NUMERIC: // 数字
//                                cell.setCellType(HSSFCell.CELL_TYPE_STRING);
//                                sb.append(getContent(cell.getStringCellValue())).append("").append(SEPARATOR);
//                                break;
//                            case HSSFCell.CELL_TYPE_STRING: // 字符串
//                                if (StringUtils.isNotEmpty((cell.getStringCellValue())))
//                                    sb.append(getContent(cell.getStringCellValue())).append("").append(SEPARATOR);
//                                else
//                                    sb.append("  " + SEPARATOR);
//                                break;
//                            case HSSFCell.CELL_TYPE_BOOLEAN: // Boolean
//                                sb.append(cell.getBooleanCellValue()).append("").append(SEPARATOR);
//                                break;
//                            case HSSFCell.CELL_TYPE_FORMULA: // 公式
//                                sb.append(cell.getCellFormula()).append("").append(SEPARATOR);
//                                break;
//                            case HSSFCell.CELL_TYPE_BLANK: // 空值
//                                sb.append("   " + SEPARATOR);
//                                break;
//                            case HSSFCell.CELL_TYPE_ERROR: // 故障
//                                sb.append("  " + SEPARATOR);
//                                break;
//                            default:
//                                sb.append("未知类型   ");
//                                break;
//                        }
//                    } else {
//                        sb.append("   " + SEPARATOR);
//                    }
//                }
//            }
//            if (row != null && row.getLastCellNum() < cellNum) {
//                for (int i = 0; i < cellNum - row.getLastCellNum(); i++) {
//                    sb.append("   " + SEPARATOR);
//                }
//            }
//            list.add(sb.toString());
//        }
//        return list;
//    }
//
//    public static String getContent(String string) {
//        if (string == null) {
//            return " ";
//        }
//        string = string.replaceAll("\\s+", "");
//        string = string.replaceAll(" ", "");
//        string = string.replaceAll("'", "");
//        string = string.replaceAll("\"", "");
//        string = string.replaceAll(";", "");
//        string = string.replaceAll(":", "");
//        string = string.trim();
//        return string;
//    }
//
//    public static <T> List<T> extract(InputStream is, int sheetNum, String[] properties, Class<T> clazz) {
//        try {
//            Workbook wb = WorkbookFactory.create(is);
//            Sheet sheet = wb.getSheetAt(sheetNum);
//            if (properties != null) {
//                List<T> records = new ArrayList<>();
//                Long lineCount = 0L;
//                for (Row row : sheet) {
//                    lineCount++;
//                    if (lineCount == 1) {
//                        continue;
//                    }
//                    Map<String, Object> rowValueMap = new HashMap<>();
//                    for (int i = 0; i < properties.length; i++) {
//                        Cell cell = row.getCell(i, Row.CREATE_NULL_AS_BLANK);
//                        switch (cell.getCellType()) {
//                            case Cell.CELL_TYPE_BLANK:
//                                rowValueMap.put(properties[i], "");
//                                break;
//                            case Cell.CELL_TYPE_BOOLEAN:
//                                rowValueMap.put(properties[i], Boolean.toString(cell.getBooleanCellValue()));
//                                break;
//                            //数值
//                            case Cell.CELL_TYPE_NUMERIC:
//                                if (DateUtil.isCellDateFormatted(cell)) {
//                                    rowValueMap.put(properties[i], String.valueOf(cell.getDateCellValue()));
//                                } else {
//                                    cell.setCellType(Cell.CELL_TYPE_STRING);
//                                    rowValueMap.put(properties[i], cell.getStringCellValue().trim());
////                                    String temp = cell.getStringCellValue();
//                                }
//                                break;
//                            case Cell.CELL_TYPE_STRING:
//                                rowValueMap.put(properties[i], cell.getStringCellValue().trim());
//                                break;
//                            case Cell.CELL_TYPE_ERROR:
//                                rowValueMap.put(properties[i], "");
//                                break;
//                            case Cell.CELL_TYPE_FORMULA:
//                                cell.setCellType(Cell.CELL_TYPE_STRING);
//                                rowValueMap.put(properties[i], cell.getStringCellValue().trim());
//                                break;
//                            default:
//                                rowValueMap.put(properties[i], "");
//                                break;
//                        }
//                    }
//                    T t = clazz.newInstance();
//                    org.apache.commons.beanutils.BeanUtils.populate(t, rowValueMap);
//                    records.add(t);
//                }
//                return records;
//            }
//        } catch (IOException | InvalidFormatException | InvocationTargetException | IllegalAccessException | InstantiationException e) {
//            e.printStackTrace();
//        }finally {
//            ioClose(is);
//        }
//        return null;
//    }
//
//    /**
//     * 将一个 Map 对象转化为一个 JavaBean
//     *
//     * @param type 要转化的类型
//     * @param map  包含属性值的 map
//     * @return 转化出来的 JavaBean 对象
//     * @throws IntrospectionException    如果分析类属性失败
//     * @throws IllegalAccessException    如果实例化 JavaBean 失败
//     * @throws InstantiationException    如果实例化 JavaBean 失败
//     * @throws InvocationTargetException 如果调用属性的 setter 方法失败
//     */
//    public static Object convertMap(Class type, Map map)
//            throws IntrospectionException, IllegalAccessException,
//            InstantiationException, InvocationTargetException {
//        BeanInfo beanInfo = Introspector.getBeanInfo(type); // 获取类属性
//        Object obj = type.newInstance(); // 创建 JavaBean 对象
//
//        // 给 JavaBean 对象的属性赋值
//        PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
//        for (int i = 0; i < propertyDescriptors.length; i++) {
//            PropertyDescriptor descriptor = propertyDescriptors[i];
//            String propertyName = descriptor.getName();
//
//            if (map.containsKey(propertyName)) {
//                // 下面一句可以 try 起来，这样当一个属性赋值失败的时候就不会影响其他属性赋值。
//                Object value = map.get(propertyName);
//
//                Object[] args = new Object[1];
//                args[0] = value;
//
//                descriptor.getWriteMethod().invoke(obj, args);
//            }
//        }
//        return obj;
//    }
//
//    public static <T> void export(InputStream is, int sheetNum, String[] properties, List<T> records, OutputStream os) {
//        try {
//            Workbook wb = WorkbookFactory.create(is);
//            Sheet sheet = wb.getSheetAt(sheetNum);
//            for (T t : records) {
//                Row row = sheet.createRow((short) (sheet.getLastRowNum() + 1));
//                Map<String, Object> map = transBean2Map(t);
//                for (int i = 0; i < properties.length; i++) {
//                    try {
//                        row.createCell(i).setCellValue(String.valueOf(map.get(properties[i])));
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//
//            wb.write(os);
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            ioClose(is, os);
//        }
//
//    }
//
//    private static void ioClose(Closeable... ios) {
//        if (ios != null) {
//            for (Closeable io : ios) {
//                try {
//                    io.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//    }
//
//    public static Map<String, Object> transBean2Map(Object obj) {
//
//        if (obj == null) {
//            return null;
//        }
//        Map<String, Object> map = new HashMap<String, Object>();
//        try {
//            BeanInfo beanInfo = Introspector.getBeanInfo(obj.getClass());
//            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
//            for (PropertyDescriptor property : propertyDescriptors) {
//                String key = property.getName();
//
//                // 过滤class属性
//                if (!key.equals("class")) {
//                    // 得到property对应的getter方法
//                    Method getter = property.getReadMethod();
//                    Object value = getter.invoke(obj);
//
//                    map.put(key, value);
//                }
//
//            }
//        } catch (Exception e) {
//            System.out.println("transBean2Map Error " + e);
//        }
//
//        return map;
//
//    }
//}
