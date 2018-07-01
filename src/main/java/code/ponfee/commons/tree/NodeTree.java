/* __________              _____                                          *\
** \______   \____   _____/ ____\____   ____        Ponfee's code         **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \       (c) 2017-2018, MIT    **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/       http://www.ponfee.cn  **
**  |____|   \____/|___|  /__|  \___  >\___  >                            **
**                      \/          \/     \/                             **
\*                                                                        */

package code.ponfee.commons.tree;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import code.ponfee.commons.collect.Collects;
import code.ponfee.commons.reflect.Fields;

/**
 * 节点树形结构
 * 
 * @author Ponfee
 */
public final class NodeTree<T extends java.io.Serializable & Comparable<T>>
    extends AbstractNode<T> {

    private static final long serialVersionUID = -9081626363752680404L;
    public static final String DEFAULT_ROOT_NAME = "__ROOT__";

    // 子节点列表（为空列表则是叶子节点）
    private final List<NodeTree<T>> children = Lists.newArrayList();

    /**
     * 构造空结点作为根节点
     */
    public NodeTree(T nid) {
        this(nid, null, 0, true);
    }

    public NodeTree(T nid, T pid, int orders) {
        this(nid, pid, orders, true);
    }

    /**
     * 构造根节点
     * 
     * @param nid
     * @param pid
     * @param orders
     * @param enabled
     */
    public NodeTree(T nid, T pid, int orders, boolean enabled) {
        super(nid, pid, orders, enabled, null);
        this.available = enabled;
    }

    /**
     * 指定一个节点作为根节点
     * 
     * @param node  as a tree root node
     */
    public NodeTree(AbstractNode<T> node) {
        super(node.getNid(), node.getPid(), 
              node.getOrders(), node.isEnabled(), node);
        super.available = node.isAvailable();
    }

    public <E extends AbstractNode<T>> NodeTree<T> build(List<E> nodes) {
        build(nodes, false);
        return this;
    }

    /**
     * 以此节点为根，传入子节点来构建节点树
     * 
     * @param nodes        子节点列表
     * @param ignoreOrphan {@code true}忽略孤儿节点
     */
    @SuppressWarnings("unchecked")
    public <E extends AbstractNode<T>> NodeTree<T> build(List<E> list, 
                                                         boolean ignoreOrphan) {
        Set<T> nodeNids = Sets.newHashSet(this.nid);

        // 1、预处理
        List<AbstractNode<T>> nodes = before(list);

        // 2、检查是否存在重复节点
        for (AbstractNode<T> n : nodes) {
            if (!nodeNids.add(n.getNid())) {
                throw new RuntimeException("重复的节点：" + n.getNid());
            }
        }

        // 3、以此节点为根构建节点树
        this.level = 1; // reset with 1
        this.path = null; // reset with null
        this.leftLeafCount = 0; // reset with 0
        this.build0(nodes, ignoreOrphan, this.nid);

        // 4、检查是否存在孤儿节点
        if (!ignoreOrphan && CollectionUtils.isNotEmpty(nodes)) {
            List<T> nids = nodes.stream().map(n -> n.getNid()).collect(Collectors.toList());
            throw new RuntimeException("无效的孤儿节点：" + nids);
        }

        // 5、统计
        count();

        return this;
    }

    /**
     * 按继承方式展开节点：父子节点相邻 
     * 
     * should be before invoke {@link #build(List)}
     * 
     * @return
     */
    public List<NodeFlat<T>> flatInherit() {
        List<NodeFlat<T>> collect = Lists.newArrayList();
        inherit(collect);
        return collect;
    }

    /**
     * 按层级展开节点：兄弟节点相邻
     * 
     * @return
     */
    public List<NodeFlat<T>> flatHierarchy() {
        List<NodeFlat<T>> collect = Lists.newArrayList(new NodeFlat<>(this));
        hierarchy(collect);
        return collect;
    }

    // -----------------------------------------------------------private methods
    private <E extends AbstractNode<T>> List<AbstractNode<T>> before(List<E> nodes) {
        List<AbstractNode<T>> list = Lists.newArrayList();
        for (AbstractNode<T> node : nodes) {
            if (node instanceof NodeTree) {
                List<NodeFlat<T>> flat = ((NodeTree<T>) node).flatInherit();
                list.addAll(flat);
            } else {
                list.add(node.copy());
            }
        }
        if (CollectionUtils.isNotEmpty(this.children)) {
            List<NodeFlat<T>> flat = this.flatInherit();
            list.addAll(flat.subList(1, flat.size()));
            this.children.clear();
        }
        return list;
    }

    private <E extends AbstractNode<T>> void build0(
        List<E> nodes, boolean ignoreOrphan, T mountPidIfNull) {
        // current "this" is parent: AbstractNode parent = this;

        Set<Integer> uniqueOrders = Sets.newHashSet();
        // find child nodes for the current node
        for (Iterator<E> iter = nodes.iterator(); iter.hasNext();) {
            AbstractNode<T> node = iter.next();

            if (!ignoreOrphan && super.isEmpty(node.getPid())) {
                // 不忽略孤儿节点且节点的父节点为空，则其父节点视为根节点（挂载到根节点下）
                Fields.put(node, "pid", mountPidIfNull);
            }

            if (this.nid.equals(node.getPid())) {
                // found a child node
                if (CollectionUtils.isNotEmpty(this.path)
                    && this.path.contains(this.nid)) { // 节点路径中已经包含了此节点，则视为环状
                    throw new RuntimeException("节点循环依赖：" + node.getNid());
                }

                if (!uniqueOrders.add(node.getOrders())) {
                    throw new RuntimeException("兄弟节点次序重复：" + node.getNid());
                }

                NodeTree<T> child = new NodeTree<>(node);
                child.setAvailable(this.available && child.isEnabled());

                // 子节点路径=节点路径+自身节点
                child.setPath(Collects.add(this.path, this.nid));
                child.setLevel(this.level + 1);
                this.children.add(child); // 挂载子节点

                iter.remove(); // remove the found child node
            }
        }

        if (CollectionUtils.isNotEmpty(this.children)) {
            // sort the children list
            this.children.sort(Comparator.comparing(NodeTree::getOrders));

            // recursion to build child tree
            for (NodeTree<T> nt : this.children) {
                nt.build0(nodes, ignoreOrphan, mountPidIfNull);
            }
        }

        this.setPath(Collects.add(this.path, this.nid)); // 节点路径追加自身的ID
    }

    private void inherit(List<NodeFlat<T>> collect) {
        collect.add(new NodeFlat<>(this));
        if (CollectionUtils.isNotEmpty(this.children)) {
            for (NodeTree<T> nt : this.children) {
                nt.inherit(collect);
            }
        }
    }

    private void hierarchy(List<NodeFlat<T>> collect) {
        if (CollectionUtils.isNotEmpty(this.children)) {
            for (NodeTree<T> nt : this.children) {
                collect.add(new NodeFlat<>(nt));
            }
            for (NodeTree<T> nt : this.children) {
                nt.hierarchy(collect);
            }
        }
    }

    private void count() {
        if (CollectionUtils.isNotEmpty(this.children)) { // 非叶子节点
            int maxChildTreeDepth = 0, sumTreeNodeCount = 0, 
                sumChildLeafCount = 0;
            NodeTree<T> child;
            for (int i = 0; i < this.children.size(); i++) {
                child = this.children.get(i);

                // 1、统计左叶子节点数量
                if (i == 0) {
                    // 最左子节点：左叶子节点个数=父节点的左叶子节点个数
                    child.leftLeafCount = this.leftLeafCount;
                } else {
                    // 若不是最左子节点，则其左叶子节点个数=
                    // 相邻左兄弟节点的左叶子节点个数+该兄弟节点的子节点个数
                    NodeTree<T> prevSibling = this.children.get(i - 1);
                    child.leftLeafCount = prevSibling.leftLeafCount 
                                        + prevSibling.childLeafCount;
                }

                // 2、递归
                child.count();

                // 3、统计子叶子节点数量及整棵树节点的数量
                sumChildLeafCount += child.childLeafCount;
                maxChildTreeDepth = NumberUtils.max(maxChildTreeDepth, 
                                                    child.treeMaxDepth);
                sumTreeNodeCount += child.treeNodeCount;
            }
            this.childLeafCount = sumChildLeafCount; // 子节点的叶子节点之和
            this.treeMaxDepth = maxChildTreeDepth + 1; // 为子节点的上一层级
            this.treeNodeCount = sumTreeNodeCount + 1; // 要包含节点本身
        } else { // 叶子节点
            this.treeNodeCount = 1;
            this.childLeafCount = 1;
            this.treeMaxDepth = 1;
        }
    }

    // -----------------------------------------------getter/setter
    public List<NodeTree<T>> getChildren() {
        return children;
    }

}
