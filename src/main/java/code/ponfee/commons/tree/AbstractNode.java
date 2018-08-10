/* __________              _____                                          *\
** \______   \____   _____/ ____\____   ____        Ponfee's code         **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \       (c) 2017-2018, MIT    **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/       http://www.ponfee.cn  **
**  |____|   \____/|___|  /__|  \___  >\___  >                            **
**                      \/          \/     \/                             **
\*                                                                        */

package code.ponfee.commons.tree;

import code.ponfee.commons.serial.JdkSerializer;
import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * 基于树形结构节点的基类
 * 
 * @author Ponfee
 */
public abstract class AbstractNode<T 
    extends java.io.Serializable & Comparable<T>>
    implements java.io.Serializable {

    private static final long serialVersionUID = -4116799955526185765L;

    protected final T nid; // node id
    protected final T pid; // parent node id
    protected final int orders; // 节点次序（只用于兄弟节点间的排序）
    protected final boolean enabled; // 状态：false无效；true有效；
    protected final /*transient*/ AbstractNode<T> attach; // 附加节点（附加信息，与业务相关）

    protected boolean available; // 是否可用（parent.available && enabled）
    protected int level; // 节点层级（以根节点为1开始，往下逐级加1）
    protected List<T> path; // 节点路径list<nid>（父节点在前，末尾元素是节点本身的nid）

    protected int childLeafCount; // 子叶子节点数量（若为叶子节点则为1）
    protected int leftLeafCount; // 左叶子节点数量（在其左边的所有叶子节点数量）
    protected int treeNodeCount; // 整棵树的节点数量（包括根节点）
    protected int treeMaxDepth; // 节点树的最大深度（包括自身层级）

    public AbstractNode(T nid, T pid, int orders, 
                        boolean enabled, AbstractNode<T> attach) {
        Preconditions.checkArgument(isNotEmpty(nid), "节点编号不能为空");
        this.nid = nid;
        this.pid = pid;
        this.orders = orders;
        this.enabled = enabled;
        this.attach = innermostAttach(attach);
        this.available = enabled;
    }

    @SuppressWarnings("unchecked")
    public @Override AbstractNode<T> clone() {
        JdkSerializer serializer = new JdkSerializer();
        byte[] bytes = serializer.serialize(this);
        return serializer.deserialize(bytes, this.getClass());
    }

    public boolean isEmpty(T id) {
        if (id instanceof CharSequence) {
            return StringUtils.isBlank((CharSequence) id);
        }
        return id == null;
    }

    public boolean isNotEmpty(T id) {
        return !isEmpty(id);
    }

    private AbstractNode<T> innermostAttach(AbstractNode<T> attach) {
        if (attach == null || attach.attach == null) {
            return attach; // attach.clone()
        } else {
            return innermostAttach(attach.attach);
        }
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

    public /*@Transient*/ AbstractNode<T> getAttach() {
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

    public int getChildLeafCount() {
        return childLeafCount;
    }

    public int getTreeMaxDepth() {
        return treeMaxDepth;
    }

    public int getLeftLeafCount() {
        return leftLeafCount;
    }

}
