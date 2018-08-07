package code.ponfee.commons.export;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import code.ponfee.commons.tree.FlatNode;
import code.ponfee.commons.tree.TreeNode;

/**
 * 表格
 * @author fupf
 */
public class Table implements Serializable {
    private static final long serialVersionUID = 1600567917100486004L;

    private static final int ROOT_PID = 0;

    private String caption; // 标题
    private final List<FlatNode<Integer>> thead; // 表头
    private List<Object[]> tobdy; // 表体
    private Object[] tfoot; // 表尾
    private String comment; // 注释说明
    private Map<CellStyleOptions, Object> options; // 其它特殊配置项，如：{HIGHLIGHT:{\"cells\":[[2,15],[2,16]],\"color\":\"#f00\"}}

    public Table(List<Thead> list) {
        this.thead = TreeNode.createRoot(ROOT_PID, null, 0)
                             .mount(list).flatHierarchy();
    }

    public Table(String[] names) {
        List<Thead> list = new ArrayList<>(names.length);
        for (int i = 0; i < names.length; i++) {
            list.add(new Thead(names[i], i + 1, ROOT_PID));
        }
        this.thead = TreeNode.createRoot(ROOT_PID, null, 0)
                             .mount(list).flatHierarchy();
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public List<FlatNode<Integer>> getThead() {
        return thead;
    }

    public List<Object[]> getTobdy() {
        return tobdy;
    }

    public void setTobdy(List<Object[]> tobdy) {
        this.tobdy = tobdy;
    }

    public Object[] getTfoot() {
        return tfoot;
    }

    public void setTfoot(Object[] tfoot) {
        this.tfoot = tfoot;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Map<CellStyleOptions, Object> getOptions() {
        return options;
    }

    public void setOptions(Map<CellStyleOptions, Object> options) {
        this.options = options;
    }

}
