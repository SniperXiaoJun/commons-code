package code.ponfee.commons.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 参考guthub
 *   新增分页的多项属性，主要参考:http://bbs.csdn.net/topics/360010907
 *   项目地址 : http://git.oschina.net/free/Mybatis_PageHelper
 * 
 * 从第1页开始到pages页
 */
public class Page<T> implements Serializable {
    private static final long serialVersionUID = 1313118491812094979L;

    private int pageNum; // 当前页
    private int pageSize; // 每页的数量
    private int size; // 当前页的数量
    //private String orderBy; // 排序
    private int startRow; // 当前页面第一个元素在数据库中的行号
    private int endRow; // 当前页面最后一个元素在数据库中的行号
    private long total; // 总记录数
    private int pages; // 总页数
    private List<T> rows; // 结果集
    private int prePage; // 前一页
    private int nextPage; // 下一页
    private boolean firstPage = false; // 是否为第一页
    private boolean lastPage = false; // 是否为最后一页
    private boolean hasPreviousPage = false; // 是否有前一页
    private boolean hasNextPage = false; // 是否有下一页
    private int navigatePages; // 导航页码数
    private int[] navigatepageNums; // 所有导航页号
    private int navigateFirstPage; // 导航条上的第一页
    private int navigateLastPage; // 导航条上的最后一页

    public Page() {
        this(new ArrayList<>());
    }

    /**
     * 包装Page对象
     * @param list
     */
    public Page(List<T> list) {
        this(list, 8);
    }

    /**
     * 包装Page对象
     * @param list          page结果
     * @param navigatePages 页码数量
     */
    public Page(List<T> list, int navigatePages) {
        if (list instanceof com.github.pagehelper.Page) {
            com.github.pagehelper.Page<T> page = (com.github.pagehelper.Page<T>) list;
            this.pageNum = page.getPageNum();
            this.pageSize = page.getPageSize();
            //this.orderBy = page.getOrderBy();

            this.pages = page.getPages();
            this.rows = copy(page);
            this.size = page.size();
            this.total = page.getTotal();
            if (this.size == 0) {
                this.startRow = 0;
                this.endRow = 0;
            } else {
                this.startRow = page.getStartRow() + 1; // 由于结果是>startRow的，所以实际的需要+1
                this.endRow = this.startRow - 1 + this.size; // 计算实际的endRow（最后一页的时候特殊）
            }
        } else {
            this.pageNum = 1;
            this.pageSize = list.size();

            this.pages = 1;
            this.rows = list;
            this.size = list.size();
            this.total = list.size();
            this.startRow = 0;
            this.endRow = list.size() > 0 ? list.size() - 1 : 0;
        }

        this.navigatePages = navigatePages;
        calcNavigatepageNums(); // 计算导航页
        calcPage(); // 计算前后页，第一页，最后一页
        judgePageBoudary(); // 判断页面边界
    }

    private List<T> copy(com.github.pagehelper.Page<T> page) {
        List<T> list = new ArrayList<>(page.size());
        for (T t : page) {
            list.add(t);
        }
        return list;
    }

    /**
     * 计算导航页
     */
    private void calcNavigatepageNums() {
        if (pages <= navigatePages) { // 当总页数小于或等于导航页码数时
            navigatepageNums = new int[pages];
            for (int i = 0; i < pages; i++) {
                navigatepageNums[i] = i + 1;
            }
        } else { // 当总页数大于导航页码数时
            navigatepageNums = new int[navigatePages];
            int startNum = pageNum - navigatePages / 2;
            int endNum = pageNum + navigatePages / 2;

            if (startNum < 1) {
                startNum = 1;
                // (最前navigatePages页
                for (int i = 0; i < navigatePages; i++) {
                    navigatepageNums[i] = startNum++;
                }
            } else if (endNum > pages) {
                endNum = pages;
                // 最后navigatePages页
                for (int i = navigatePages - 1; i >= 0; i--) {
                    navigatepageNums[i] = endNum--;
                }
            } else {
                // 所有中间页
                for (int i = 0; i < navigatePages; i++) {
                    navigatepageNums[i] = startNum++;
                }
            }
        }
    }

    /**
     * 计算前后页，第一页，最后一页
     */
    private void calcPage() {
        if (navigatepageNums != null && navigatepageNums.length > 0) {
            navigateFirstPage = navigatepageNums[0];
            navigateLastPage = navigatepageNums[navigatepageNums.length - 1];
            if (pageNum > 1) {
                prePage = pageNum - 1;
            }
            if (pageNum < pages) {
                nextPage = pageNum + 1;
            }
        }
    }

    /**
     * 判定页面边界
     */
    private void judgePageBoudary() {
        firstPage = pageNum == 1;
        lastPage = pageNum == pages;
        hasPreviousPage = pageNum > 1;
        hasNextPage = pageNum < pages;
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

    /*public String getOrderBy() {
        return orderBy;
    }
    
    public void setOrderBy(String orderBy) {
        this.orderBy = orderBy;
    }*/

    public boolean isFirstPage() {
        return firstPage;
    }

    public void setFirstPage(boolean firstPage) {
        this.firstPage = firstPage;
    }

    public boolean isLastPage() {
        return lastPage;
    }

    public void setLastPage(boolean lastPage) {
        this.lastPage = lastPage;
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

    public List<T> getRows() {
        return rows;
    }

    public void setRows(List<T> rows) {
        this.rows = rows;
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

    public int getNavigateLastPage() {
        return navigateLastPage;
    }

    public void setNavigateFirstPage(int navigateFirstPage) {
        this.navigateFirstPage = navigateFirstPage;
    }

    public void setNavigateLastPage(int navigateLastPage) {
        this.navigateLastPage = navigateLastPage;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("Page{");
        sb.append("pageNum=").append(pageNum);
        sb.append(", pageSize=").append(pageSize);
        sb.append(", size=").append(size);
        sb.append(", startRow=").append(startRow);
        sb.append(", endRow=").append(endRow);
        sb.append(", total=").append(total);
        sb.append(", pages=").append(pages);
        sb.append(", rows=").append(rows);
        sb.append(", prePage=").append(prePage);
        sb.append(", nextPage=").append(nextPage);
        sb.append(", firstPage=").append(firstPage);
        sb.append(", lastPage=").append(lastPage);
        sb.append(", hasPreviousPage=").append(hasPreviousPage);
        sb.append(", hasNextPage=").append(hasNextPage);
        sb.append(", navigatePages=").append(navigatePages);
        sb.append(", navigateFirstPage").append(navigateFirstPage);
        sb.append(", navigateLastPage").append(navigateLastPage);
        sb.append(", navigatepageNums=");
        if (navigatepageNums == null) {
            sb.append("null");
        } else {
            sb.append('[');
            for (int i = 0; i < navigatepageNums.length; ++i) {
                sb.append(i == 0 ? "" : ", ").append(navigatepageNums[i]);
            }
            sb.append(']');
        }
        sb.append('}');
        return sb.toString();
    }

}
