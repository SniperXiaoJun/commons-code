package code.ponfee.commons.model;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

/**
 * 分页请求参数封装类
 * 
 * @author fupf
 */
public class PageRequestParams implements java.io.Serializable {

    private static final long serialVersionUID = 6176654946390797217L;

    public static final Set<String> PAGE_PARAMS = ImmutableSet.of(
        "pageNum", "pageSize", "offset", "limit"
    );

    public static final String SORT_PARAM = "sort";

    private int pageNum;
    private int pageSize;

    private int offset;
    private int limit;

    private String sort;

    private final Map<String, Object> params = new LinkedHashMap<>();

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

    public String getSort() {
        return sort;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public void put(String key, Object value) {
        this.params.put(key, value);
    }

}
