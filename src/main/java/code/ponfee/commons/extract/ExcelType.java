package code.ponfee.commons.extract;

/**
 * Excel file type
 * 
 * @author Ponfee
 */
public enum ExcelType {

    XLS, XLSX;

    public static ExcelType from(String type) {
        for (ExcelType et : ExcelType.values()) {
            if (et.name().equalsIgnoreCase(type)) {
                return et;
            }
        }
        return null;
    }
}
