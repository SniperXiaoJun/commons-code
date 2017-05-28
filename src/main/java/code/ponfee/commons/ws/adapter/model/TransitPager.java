package code.ponfee.commons.ws.adapter.model;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import code.ponfee.commons.model.Pager;

/**
 * pager转换
 * @author fupf
 * @param <T>
 */
public class TransitPager<T> {

    private ArrayItem<T> rows;
    private int pageNum; // 当前页
    private int pageSize; // 每页的数量
    private int size; // 当前页的数量
    private int startRow; // 当前页面第一个元素在数据库中的行号
    private int endRow; // 当前页面最后一个元素在数据库中的行号
    private long total; // 总记录数
    private int pages; // 总页数
    private int prePage; // 前一页
    private int nextPage; // 下一页
    private boolean isFirstPage = false; // 是否为第一页
    private boolean isLastPage = false; // 是否为最后一页
    private boolean hasPreviousPage = false; // 是否有前一页
    private boolean hasNextPage = false; // 是否有下一页
    private int navigatePages; // 导航页码数
    private int[] navigatepageNums; // 所有导航页号
    private int navigateFirstPage; // 导航条上的第一页
    private int navigateLastPage; // 导航条上的最后一页

    public ArrayItem<T> getRows() {
        return rows;
    }

    public void setRows(ArrayItem<T> rows) {
        this.rows = rows;
    }

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

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getStartRow() {
        return startRow;
    }

    public void setStartRow(int startRow) {
        this.startRow = startRow;
    }

    public int getEndRow() {
        return endRow;
    }

    public void setEndRow(int endRow) {
        this.endRow = endRow;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public int getPages() {
        return pages;
    }

    public void setPages(int pages) {
        this.pages = pages;
    }

    public int getPrePage() {
        return prePage;
    }

    public void setPrePage(int prePage) {
        this.prePage = prePage;
    }

    public int getNextPage() {
        return nextPage;
    }

    public void setNextPage(int nextPage) {
        this.nextPage = nextPage;
    }

    public boolean isFirstPage() {
        return isFirstPage;
    }

    public void setFirstPage(boolean isFirstPage) {
        this.isFirstPage = isFirstPage;
    }

    public boolean isLastPage() {
        return isLastPage;
    }

    public void setLastPage(boolean isLastPage) {
        this.isLastPage = isLastPage;
    }

    public boolean isHasPreviousPage() {
        return hasPreviousPage;
    }

    public void setHasPreviousPage(boolean hasPreviousPage) {
        this.hasPreviousPage = hasPreviousPage;
    }

    public boolean isHasNextPage() {
        return hasNextPage;
    }

    public void setHasNextPage(boolean hasNextPage) {
        this.hasNextPage = hasNextPage;
    }

    public int getNavigatePages() {
        return navigatePages;
    }

    public void setNavigatePages(int navigatePages) {
        this.navigatePages = navigatePages;
    }

    public int[] getNavigatepageNums() {
        return navigatepageNums;
    }

    public void setNavigatepageNums(int[] navigatepageNums) {
        this.navigatepageNums = navigatepageNums;
    }

    public int getNavigateFirstPage() {
        return navigateFirstPage;
    }

    public void setNavigateFirstPage(int navigateFirstPage) {
        this.navigateFirstPage = navigateFirstPage;
    }

    public int getNavigateLastPage() {
        return navigateLastPage;
    }

    public void setNavigateLastPage(int navigateLastPage) {
        this.navigateLastPage = navigateLastPage;
    }

    public static <T> TransitPager<T> marshal(Pager<?> pager, T[] t) {
        TransitPager<T> transit = new TransitPager<>();
        transit.setRows(new ArrayItem<T>(t));
        copy(transit, pager);
        return transit;
    }

    @SuppressWarnings("unchecked")
    public static <T> TransitPager<T> marshal(Class<?> type, Pager<T> pager) {
        TransitPager<T> transit = new TransitPager<>();
        List<T> data = pager.getRows();
        T[] array = data.toArray((T[]) Array.newInstance(type, data.size()));
        transit.setRows(new ArrayItem<T>(array));
        copy(transit, pager);
        return transit;
    }

    private static void copy(TransitPager<?> transit, Pager<?> pager) {
        transit.setPageNum(pager.getPageNum());
        transit.setPageSize(pager.getPageSize());
        transit.setSize(pager.getSize());
        transit.setStartRow(pager.getStartRow());
        transit.setEndRow(pager.getEndRow());
        transit.setTotal(pager.getTotal());
        transit.setPages(pager.getPages());
        transit.setPrePage(pager.getPrePage());
        transit.setNextPage(pager.getNextPage());
        transit.setFirstPage(pager.isFirstPage());
        transit.setLastPage(pager.isLastPage());
        transit.setHasPreviousPage(pager.isHasPreviousPage());
        transit.setHasNextPage(pager.isHasNextPage());
        transit.setNavigatePages(pager.getNavigatePages());
        transit.setNavigatepageNums(pager.getNavigatepageNums());
        transit.setNavigateFirstPage(pager.getNavigateFirstPage());
        transit.setNavigateLastPage(pager.getNavigateLastPage());
    }

    public static <T> Pager<T> unmarshal(TransitPager<T> transit) {
        Pager<T> pager = new Pager<>();
        List<T> list = new ArrayList<>();
        for (T t : transit.getRows().getItem()) {
            list.add(t);
        }
        pager.setRows(list);
        pager.setPageNum(transit.getPageNum());
        pager.setPageSize(transit.getPageSize());
        pager.setSize(transit.getSize());
        pager.setStartRow(transit.getStartRow());
        pager.setEndRow(transit.getEndRow());
        pager.setTotal(transit.getTotal());
        pager.setPages(transit.getPages());
        pager.setPrePage(transit.getPrePage());
        pager.setNextPage(transit.getNextPage());
        pager.setFirstPage(transit.isFirstPage());
        pager.setLastPage(transit.isLastPage());
        pager.setHasPreviousPage(transit.isHasPreviousPage());
        pager.setHasNextPage(transit.isHasNextPage());
        pager.setNavigatePages(transit.getNavigatePages());
        pager.setNavigatepageNums(transit.getNavigatepageNums());
        pager.setNavigateFirstPage(transit.getNavigateFirstPage());
        pager.setNavigateLastPage(transit.getNavigateLastPage());
        return pager;
    }
}
