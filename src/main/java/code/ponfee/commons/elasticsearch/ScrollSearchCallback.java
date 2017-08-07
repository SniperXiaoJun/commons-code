package code.ponfee.commons.elasticsearch;

import org.elasticsearch.search.SearchHits;

/**
 * 滚动搜索回调函数
 * @author fupf
 */
@FunctionalInterface
public interface ScrollSearchCallback {

    /**
     * 滚动到下一页
     * @param searchHits   搜索结果
     * @param totalRecord  总记录数
     * @param totalPage    总页数
     * @param pageNo       当前滚动的页码
     */
    void nextPage(SearchHits searchHits, int totalRecord, int totalPage, int pageNo);

    /**
     * 无结果数据
     */
    default void noResult() {};
}
