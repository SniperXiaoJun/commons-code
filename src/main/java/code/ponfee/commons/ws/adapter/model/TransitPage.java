package code.ponfee.commons.ws.adapter.model;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import code.ponfee.commons.model.Page;

/**
 * Page转换
 * @author fupf
 * @param <T>
 */
public class TransitPage<T> {

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

    public static <T> TransitPage<T> marshal(Page<?> page, T[] t) {
        TransitPage<T> transit = new TransitPage<>();
        transit.setRows(new ArrayItem<T>(t));
        copy(transit, page);
        return transit;
    }

    @SuppressWarnings("unchecked")
    public static <T> TransitPage<T> marshal(Class<?> type, Page<T> page) {
        TransitPage<T> transit = new TransitPage<>();
        List<T> data = page.getRows();
        T[] array = data.toArray((T[]) Array.newInstance(type, data.size()));
        transit.setRows(new ArrayItem<T>(array));
        copy(transit, page);
        return transit;
    }

    private static void copy(TransitPage<?> transit, Page<?> page) {
        transit.setPageNum(page.getPageNum());
        transit.setPageSize(page.getPageSize());
        transit.setSize(page.getSize());
        transit.setStartRow(page.getStartRow());
        transit.setEndRow(page.getEndRow());
        transit.setTotal(page.getTotal());
        transit.setPages(page.getPages());
        transit.setPrePage(page.getPrePage());
        transit.setNextPage(page.getNextPage());
        transit.setFirstPage(page.isFirstPage());
        transit.setLastPage(page.isLastPage());
        transit.setHasPreviousPage(page.isHasPreviousPage());
        transit.setHasNextPage(page.isHasNextPage());
        transit.setNavigatePages(page.getNavigatePages());
        transit.setNavigatepageNums(page.getNavigatepageNums());
        transit.setNavigateFirstPage(page.getNavigateFirstPage());
        transit.setNavigateLastPage(page.getNavigateLastPage());
    }

    public static <T> Page<T> unmarshal(TransitPage<T> transit) {
        Page<T> page = new Page<>();
        List<T> list = new ArrayList<>();
        for (T t : transit.getRows().getItem()) {
            list.add(t);
        }
        page.setRows(list);
        page.setPageNum(transit.getPageNum());
        page.setPageSize(transit.getPageSize());
        page.setSize(transit.getSize());
        page.setStartRow(transit.getStartRow());
        page.setEndRow(transit.getEndRow());
        page.setTotal(transit.getTotal());
        page.setPages(transit.getPages());
        page.setPrePage(transit.getPrePage());
        page.setNextPage(transit.getNextPage());
        page.setFirstPage(transit.isFirstPage());
        page.setLastPage(transit.isLastPage());
        page.setHasPreviousPage(transit.isHasPreviousPage());
        page.setHasNextPage(transit.isHasNextPage());
        page.setNavigatePages(transit.getNavigatePages());
        page.setNavigatepageNums(transit.getNavigatepageNums());
        page.setNavigateFirstPage(transit.getNavigateFirstPage());
        page.setNavigateLastPage(transit.getNavigateLastPage());
        return page;
    }
}