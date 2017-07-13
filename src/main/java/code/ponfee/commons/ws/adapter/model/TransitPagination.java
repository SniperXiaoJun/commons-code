package code.ponfee.commons.ws.adapter.model;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import code.ponfee.commons.model.Pagination;

/**
 * Pagination转换
 * @author fupf
 * @param <T>
 */
public class TransitPagination<T> {

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

    public static <T> TransitPagination<T> marshal(Pagination<?> pagination, T[] t) {
        TransitPagination<T> transit = new TransitPagination<>();
        transit.setRows(new ArrayItem<T>(t));
        copy(transit, pagination);
        return transit;
    }

    @SuppressWarnings("unchecked")
    public static <T> TransitPagination<T> marshal(Class<?> type, Pagination<T> pagination) {
        TransitPagination<T> transit = new TransitPagination<>();
        List<T> data = pagination.getRows();
        T[] array = data.toArray((T[]) Array.newInstance(type, data.size()));
        transit.setRows(new ArrayItem<T>(array));
        copy(transit, pagination);
        return transit;
    }

    private static void copy(TransitPagination<?> transit, Pagination<?> pagination) {
        transit.setPageNum(pagination.getPageNum());
        transit.setPageSize(pagination.getPageSize());
        transit.setSize(pagination.getSize());
        transit.setStartRow(pagination.getStartRow());
        transit.setEndRow(pagination.getEndRow());
        transit.setTotal(pagination.getTotal());
        transit.setPages(pagination.getPages());
        transit.setPrePage(pagination.getPrePage());
        transit.setNextPage(pagination.getNextPage());
        transit.setFirstPage(pagination.isFirstPage());
        transit.setLastPage(pagination.isLastPage());
        transit.setHasPreviousPage(pagination.isHasPreviousPage());
        transit.setHasNextPage(pagination.isHasNextPage());
        transit.setNavigatePages(pagination.getNavigatePages());
        transit.setNavigatepageNums(pagination.getNavigatepageNums());
        transit.setNavigateFirstPage(pagination.getNavigateFirstPage());
        transit.setNavigateLastPage(pagination.getNavigateLastPage());
    }

    public static <T> Pagination<T> unmarshal(TransitPagination<T> transit) {
        Pagination<T> pagination = new Pagination<>();
        List<T> list = new ArrayList<>();
        for (T t : transit.getRows().getItem()) {
            list.add(t);
        }
        pagination.setRows(list);
        pagination.setPageNum(transit.getPageNum());
        pagination.setPageSize(transit.getPageSize());
        pagination.setSize(transit.getSize());
        pagination.setStartRow(transit.getStartRow());
        pagination.setEndRow(transit.getEndRow());
        pagination.setTotal(transit.getTotal());
        pagination.setPages(transit.getPages());
        pagination.setPrePage(transit.getPrePage());
        pagination.setNextPage(transit.getNextPage());
        pagination.setFirstPage(transit.isFirstPage());
        pagination.setLastPage(transit.isLastPage());
        pagination.setHasPreviousPage(transit.isHasPreviousPage());
        pagination.setHasNextPage(transit.isHasNextPage());
        pagination.setNavigatePages(transit.getNavigatePages());
        pagination.setNavigatepageNums(transit.getNavigatepageNums());
        pagination.setNavigateFirstPage(transit.getNavigateFirstPage());
        pagination.setNavigateLastPage(transit.getNavigateLastPage());
        return pagination;
    }
}
