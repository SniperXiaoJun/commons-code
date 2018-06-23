/* __________              _____                                          *\
** \______   \____   _____/ ____\____   ____        Ponfee's code         **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \       (c) 2017-2018, MIT    **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/       http://www.ponfee.cn  **
**  |____|   \____/|___|  /__|  \___  >\___  >                            **
**                      \/          \/     \/                             **
\*                                                                        */

package code.ponfee.commons.tree;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

/**
 * 基于树形结构节点的基类
 * 
 * @author Ponfee
 */
public class NodeBase<T extends java.io.Serializable & Comparable<T>>
    implements java.io.Serializable {

    private static final long serialVersionUID = -4116799955526185765L;

    protected final T nid; // 节点ID
    protected final T pid; // 父节点ID
    protected final int orders; // 节点次序（只用于兄弟节点间的排序）
    protected final boolean enabled; // 状态：false无效；true有效；
    protected final /*transient*/ NodeBase<T> attach; // 附加节点（附加信息）

    protected boolean available; // 是否可用（parent.available && enabled）
    protected int level; // 节点层级（以根节点为1开始，往下逐级加1）
    protected List<T> path; // 节点路径（父节点在前）

    protected int childLeafCount; // 子叶子节点数量（若为叶子节点，则为1）
    protected int leftLeafCount; // 左叶子节点数量
    protected int treeNodeCount; // 整棵树的节点数量
    protected int treeMaxDepth; // 节点树的最大深度（包括自身层级）

    public NodeBase(T nid, T pid, int orders, 
                    boolean enabled, NodeBase<T> attach) {
        Preconditions.checkArgument(!isEmpty(nid), "节点编号不能为空");

        this.nid = nid;
        this.pid = pid;
        this.orders = orders;
        this.enabled = enabled;
        this.attach = innermostAttach(attach);

        this.available = enabled;
    }

    public NodeBase<T> copy() {
        NodeBase<T> node = new NodeBase<>(
            this.nid, this.pid, this.orders, this.enabled, 
            (this.attach != null ? this.attach : this)
        );

        node.available = this.available;
        node.level = this.level;
        node.path = (this.path != null) 
                    ? Lists.newArrayList(this.path) : null;

        node.treeNodeCount = this.treeNodeCount;
        node.childLeafCount = this.childLeafCount;
        node.treeMaxDepth = this.treeMaxDepth;
        node.leftLeafCount = this.leftLeafCount;

        return node;
    }

    // -----------------------------------------------getter/setter
    public T getPid() {
        return pid;
    }

    public T getNid() {
        return nid;
    }

    public int getOrders() {
        return orders;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public /*@Transient*/ NodeBase<T> getAttach() {
        return attach;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public List<T> getPath() {
        return path;
    }

    public void setPath(List<T> path) {
        this.path = path;
    }

    public int getTreeNodeCount() {
        return treeNodeCount;
    }

    public void setTreeNodeCount(int treeNodeCount) {
        this.treeNodeCount = treeNodeCount;
    }

    public int getChildLeafCount() {
        return childLeafCount;
    }

    public void setChildLeafCount(int childLeafCount) {
        this.childLeafCount = childLeafCount;
    }

    public int getTreeMaxDepth() {
        return treeMaxDepth;
    }

    public void setTreeMaxDepth(int treeMaxDepth) {
        this.treeMaxDepth = treeMaxDepth;
    }

    public int getLeftLeafCount() {
        return leftLeafCount;
    }

    public void setLeftLeafCount(int leftLeafCount) {
        this.leftLeafCount = leftLeafCount;
    }

    private NodeBase<T> innermostAttach(NodeBase<T> attach) {
        if (attach == null || attach.attach == null) {
            return attach;
        }
        return innermostAttach(attach.attach);
    }

    public static <E extends java.io.Serializable & Comparable<E>> boolean isEmpty(E e) {
        if (e instanceof CharSequence) {
            return StringUtils.isBlank((CharSequence) e);
        }
        return e == null;
    }
}
