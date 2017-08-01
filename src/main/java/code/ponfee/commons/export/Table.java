package code.ponfee.commons.export;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 表格
 * @author fupf
 */
public class Table implements Serializable {
    private static final long serialVersionUID = 1600567917100486004L;

    private String caption; // 标题
    private final List<Thead> thead; // 表头
    private List<Object[]> tobdy; // 表体
    private Object[] tfoot; // 表尾
    private String comment; // 注释说明
    private Map<String, Object> options; // 其它特殊配置项，如：{\"highlight\":{\"cells\":[[2,15],[2,16]],\"color\":\"#f00\"}}

    private int maxTheadLevel = 1; // 最大节点层级（从1开始）
    private int totalLeafCount = 0; // 总叶子节点个数

    public Table(List<Thead> thead) {
        this.thead = thead;
        resolveThead(this.thead);
    }

    public Table(String[] thead) {
        List<Thead> _thead = new ArrayList<>();
        for (int i = 0; i < thead.length; i++) {
            _thead.add(new Thead(thead[i], i + 1, 0));
        }
        this.thead = _thead;
        resolveThead(this.thead);
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public List<Thead> getThead() {
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

    public Map<String, Object> getOptions() {
        return options;
    }

    public void setOptions(Map<String, Object> options) {
        this.options = options;
    }

    public int getMaxTheadLevel() {
        return maxTheadLevel;
    }

    public int getTotalLeafCount() {
        return totalLeafCount;
    }

    private void resolveThead(List<Thead> thead) {
        // 1、验重
        Set<Integer> nodes = new HashSet<>();
        for (Thead cell : thead) {
            if (cell.getOrder() < 1) {
                throw new IllegalArgumentException("[" + cell.getOrder() + "]：节点次序不能小于1；");
            } else if (!nodes.add(cell.getOrder())) {
                throw new IllegalArgumentException("重复的次序[" + cell.getOrder() + "]；");
            }
        }

        // 2、排序
        Collections.sort(thead);

        // 3、搜索各节点的节点路径
        Set<Integer> parents = new HashSet<>();
        for (int i = 0; i < thead.size(); i++) {
            Thead cell = thead.get(i);
            int porder = cell.getPorder();
            if (porder < 0) {
                throw new IllegalArgumentException("[" + porder + "]：父节点次序不能小于0；");
            }
            List<Integer> nodePath = new ArrayList<>();
            int order = cell.getOrder();
            nodePath.add(order);

            while (porder > 0) { // parentOrders>0表示有父节点（为子节点）
                if (porder >= order) {
                    throw new IllegalArgumentException("[" + order + "]：子节点次序必须大于父节点次序；");
                } else if (!nodes.contains(porder)) {
                    throw new IllegalArgumentException("[" + order + "]：无对应次序的父节点；");
                }
                boolean isFound = false;
                for (int j = i - 1; j >= 0; j--) {
                    Thead parent = thead.get(j);
                    if (porder == parent.getOrder()) { // 找到了parent
                        parents.add(porder);
                        nodePath.add(0, porder);
                        porder = parent.getPorder(); // 递归往上层找
                        isFound = true;
                        break;
                    }
                }
                if (!isFound) {
                    throw new IllegalArgumentException("[" + order + "]：子节点次序不能小于父节点次序；");
                }
            }
            if (nodePath.size() > this.maxTheadLevel) {
                this.maxTheadLevel = nodePath.size();
            }
            cell.setNodePath(nodePath);
        }

        // 4、判断是否是叶子节点
        for (Thead cell : thead) {
            if (parents.contains(cell.getOrder())) {
                cell.setLeaf(false);
            } else {
                this.totalLeafCount++;
                cell.setLeaf(true);
            }
        }

        // 5、计算
        Collections.sort(thead);
        Collections.reverse(thead); // 逆序（叶子节点在前）

        Map<Integer, Thead> maps = new HashMap<>();

        // 计算子叶子节点数量
        for (int i = 0; i < thead.size(); i++) {
            Thead cell = thead.get(i);
            maps.put(cell.getOrder(), cell);

            int childLeafCount = 0; // 子叶子节点个数
            if (cell.isLeaf()) {
                childLeafCount = 1; // 设定叶子节点的子节点数量为1
            } else {
                for (int j = i - 1; j >= 0; j--) {
                    if (thead.get(j).isLeaf() && thead.get(j).getNodePath().contains(cell.getOrder())) {
                        childLeafCount++;
                    }
                }
            }
            cell.setChildLeafCount(childLeafCount);
        }

        Collections.reverse(thead); // 再逆序（此时已变为正序）

        // 计算左叶子节点数量，从第一层节点开始
        int level = 1, leftLeafCount = 0;
        for (Thead cell : thead) {
            if (cell.getNodeLevel() > level) {
                level = cell.getNodeLevel(); // 遍历到下一层级节点，左叶子节点个数重新设置基准值
                leftLeafCount = maps.get(cell.getPorder()).getLeftLeafCount(); // 获取其父节点的左子节点数量
            }
            cell.setLeftLeafCount(leftLeafCount);
            leftLeafCount += cell.getChildLeafCount(); // 累加上节点本身占用的子节点个数
        }
    }

}
