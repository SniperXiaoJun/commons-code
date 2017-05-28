package code.ponfee.commons.export;

import java.io.Serializable;
import java.util.List;

/**
 * 表头
 * @author fupf
 */
public class Thead implements Serializable, Comparable<Thead> {
    private static final long serialVersionUID = 1898674740598755648L;

    private final String name; // 列名称
    private final int order; // 节点顺序
    private final int porder; // 父节点顺序 
    private final Tmeta tmeta;

    private transient boolean isLeaf; // 是否叶子节点
    private transient List<Integer> nodePath; // 节点路径
    private transient int childLeafCount; // 子叶子节点数量
    private transient int leftLeafCount; // 左边叶子节点数量

    public Thead(String name, int order, int porder) {
        this(name, order, porder, null);
    }

    public Thead(String name, int order, int porder, Tmeta tmeta) {
        this.name = name;
        this.order = order;
        this.porder = porder;
        this.tmeta = tmeta;
    }

    public String getName() {
        return name;
    }

    public int getOrder() {
        return order;
    }

    public int getPorder() {
        return porder;
    }

    public Tmeta getTmeta() {
        return tmeta;
    }

    boolean isLeaf() {
        return isLeaf;
    }

    void setLeaf(boolean isLeaf) {
        this.isLeaf = isLeaf;
    }

    int getChildLeafCount() {
        return childLeafCount;
    }

    void setChildLeafCount(int childLeafCount) {
        this.childLeafCount = childLeafCount;
    }

    int getLeftLeafCount() {
        return leftLeafCount;
    }

    void setLeftLeafCount(int leftLeafCount) {
        this.leftLeafCount = leftLeafCount;
    }

    List<Integer> getNodePath() {
        return nodePath;
    }

    void setNodePath(List<Integer> nodePath) {
        this.nodePath = nodePath;
    }

    int getNodeLevel() {
        if (nodePath == null) return 0;
        else return nodePath.size();
    }

    @Override
    public int compareTo(Thead o) {
        if (this.getOrder() == o.getOrder()) {
            throw new IllegalArgumentException("repeated order: " + getOrder());
        } else if (this.getNodeLevel() > o.getNodeLevel()) {
            return 1;
        } else if (this.getNodeLevel() < o.getNodeLevel()) {
            return -1;
        } else if (this.getOrder() > o.getOrder()) {
            return 1;
        } else {
            return -1;
        }
    }

}
