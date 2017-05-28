package code.ponfee.commons.model;

import java.util.LinkedHashMap;

/**
 * 请求参数封装类
 * @author fupf
 */
public class RequestParams extends LinkedHashMap<String, Object> {
    private static final long serialVersionUID = 6176654946390797217L;

    private int pageNum;
    private int pageSize;

    private int offset;
    private int limit;

    public int getPageNum() {
        return pageNum;
    }

    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

}
