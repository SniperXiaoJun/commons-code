package code.ponfee.commons.model;

import java.util.ArrayList;
import java.util.List;

/**
 * 分页解析器
 * @author fupf
 */
public final class PageBoundsResolver {

    private PageBoundsResolver() {}

    /**
     * 多个数据源查询结果分页
     * @param pageNum 页号
     * @param pageSize 页大小
     * @param subTotalCounts 各数据源查询结果集行总计
     * @return
     */
    public static List<PageBounds> resolve(int pageNum, int pageSize, long... subTotalCounts) {
        if (subTotalCounts == null || subTotalCounts.length == 0) return null;

        // 总记录数
        long totalCounts = 0;
        for (long subTotalCount : subTotalCounts) {
            totalCounts += subTotalCount;
        }
        if (totalCounts < 1) return null;

        // pageSize小于1时表示查询全部
        if (pageSize < 1) {
            List<PageBounds> bounds = new ArrayList<>();
            for (int i = 0; i < subTotalCounts.length; i++) {
                bounds.add(new PageBounds(i, 1, 0, 0, 0)); // index,pageNum=1,pageSize=0,offset=0,limit=0
            }
            return bounds;
        }

        // 合理化pageNum、offset的值
        if (pageNum < 1) pageNum = 1;
        long offset = (pageNum - 1) * pageSize;
        if (offset >= totalCounts) { // 超出总记录数，则取最后一页
            pageNum = (int) (totalCounts + pageSize - 1) / pageSize;
            offset = (pageNum - 1) * pageSize;
        }

        // 分页计算
        List<PageBounds> bounds = new ArrayList<>();
        long start = offset, end = offset + pageSize, cursor = 0;
        for (int limit, i = 0; i < subTotalCounts.length; cursor += subTotalCounts[i], i++) {
            if (start >= cursor + subTotalCounts[i]) continue;

            offset = start - cursor;
            if (end > cursor + subTotalCounts[i]) {
                limit = (int) (cursor + subTotalCounts[i] - start);
                addBounds(bounds, i, pageNum, pageSize, offset, limit);
                start = cursor + subTotalCounts[i];
            } else {
                limit = (int) (end - start);
                addBounds(bounds, i, pageNum, pageSize, offset, limit);
                break;
            }
        }
        return bounds;
    }

    private static void addBounds(List<PageBounds> bounds, int index, int pageNum, long pageSize, long offset, int limit) {
        if (index > 0) {
            // 非第一个数据源，都是从第一页开始查询，页大小设置为记录数
            pageNum = 1;
            pageSize = limit;
        }
        bounds.add(new PageBounds(index, pageNum, pageSize, offset, limit));
    }

    /**
     * 单个数据源查询结果分页
     * @param pageNum
     * @param pageSize
     * @param totalCounts
     * @return
     */
    public static PageBounds resolve(int pageNum, int pageSize, long totalCounts) {
        List<PageBounds> list = resolve(pageNum, pageSize, new long[] { totalCounts });

        if (list == null) return null;
        else return list.get(0);
    }

    /**
     * 分页对象
     */
    public static final class PageBounds {
        private final int index; // 数据源下标（start 0）
        private final int pageNum; // 页码（start 1）
        private final long pageSize; // 页大小
        private final long offset; // 偏移量（start 0）
        private final int limit; // 数据行数

        PageBounds(int index, int pageNum, long pageSize, long offset, int limit) {
            this.index = index;
            this.pageNum = pageNum;
            this.pageSize = pageSize;
            this.offset = offset;
            this.limit = limit;
        }

        public int getIndex() {
            return index;
        }

        public int getPageNum() {
            return pageNum;
        }

        public long getPageSize() {
            return pageSize;
        }

        public long getOffset() {
            return offset;
        }

        public int getLimit() {
            return limit;
        }

        @Override
        public String toString() {
            return "PageBounds{index=" + index + ", pageNum=" + pageNum
                + ", pageSize=" + pageSize + ", offset=" + offset + ", limit=" + limit + "}";
        }
    }

    public static void main(String[] args) {
        System.out.println(resolve(11, 10, 101));

        System.out.println("\n==============================");
        System.out.println(resolve(6, 15, 80, 9, 7, 10));

        System.out.println("\n==============================");
        System.out.println(resolve(16, 10, 155, 100));

        System.out.println("\n==============================");
        System.out.println(resolve(20000, 10, 155));

        System.out.println("\n==============================");
        System.out.println(resolve(6, 55, 155));
    }
}
