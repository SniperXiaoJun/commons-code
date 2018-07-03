package code.ponfee.commons.model;

import static code.ponfee.commons.model.PageHandler.DEFAULT_PAGE_NUM;
import static code.ponfee.commons.model.PageHandler.DEFAULT_PAGE_SIZE;
import static code.ponfee.commons.model.PageHandler.DEFAULT_OFFSET;
import static code.ponfee.commons.model.PageHandler.DEFAULT_LIMIT;

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
        DEFAULT_PAGE_NUM, DEFAULT_PAGE_SIZE, DEFAULT_OFFSET, DEFAULT_LIMIT
    );

    public static final String SORT_PARAM = "sort";

    private int pageNum;
    private int pageSize;

    private int offset;
    private int limit;

    private String sort;

    // 包含pageNum、pageSize、offset、limit、sort
    private final Map<String, Object> params = new LinkedHashMap<>();

    public int getPageNum() {
        return pageNum;
    }

    public void setPageNum(int pageNum) {
        //this.put(DEFAULT_PAGE_NUM, pageNum);
        this.pageNum = pageNum;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        //this.put(DEFAULT_PAGE_SIZE, pageSize);
        this.pageSize = pageSize;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        //this.put(DEFAULT_OFFSET, offset);
        this.offset = offset;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        //this.put(DEFAULT_LIMIT, limit);
        this.limit = limit;
    }

    public String getSort() {
        return sort;
    }

    public void setSort(String sort) {
        //this.put(PageRequestParams.SORT_PARAM, sort);
        this.sort = sort;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public void put(String key, Object value) {
        this.params.put(key, value);
    }

}
