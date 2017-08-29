package code.ponfee.commons.export;

/**
 * 数据导出
 * @author fupf
 */
public interface DataExporter extends AutoCloseable {

    /** 提示无结果 */
    String TIP_NO_RESULT = "data not found";

    /**
     * 构建表格
     */
    void build(Table table);

    /**
     * 获取表格
     */
    Object export();

    /**
     * 判断是否为空
     */
    boolean isEmpty();

    /**
     * 关闭资源
     */
    @Override void close();
}
