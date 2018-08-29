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

import javax.annotation.Nonnull;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import code.ponfee.commons.collect.Collects;
import code.ponfee.commons.reflect.Fields;

/**
 * 节点树形结构
 * 
 * @author Ponfee
 */
public final class TreeNode<T extends java.io.Serializable & Comparable<T>>
    extends AbstractNode<T> {

    private static final long serialVersionUID = -9081626363752680404L;
    public static final String DEFAULT_ROOT_ID = "__ROOT__";

    // 子节点列表（空列表则表示为叶子节点）
    private final List<TreeNode<T>> children = Lists.newArrayList();

    /**
     * 构造根节点
     * 
     * @param nid
     * @param pid
     * @param orders
     * @param enabled
     */
    private TreeNode(T nid, T pid, int orders, boolean enabled) {
        super(nid, pid, orders, enabled, null);
        this.available = enabled;
    }

    /**
     * 指定一个节点作为根节点
     * 
     * @param node  as a tree root node
     */
    private TreeNode(AbstractNode<T> node) {
        super(node.getNid(), node.getPid(), 
              node.getOrders(), node.isEnabled(), node);
        super.available = node.isAvailable();
    }

    public static <T extends java.io.Serializable & Comparable<T>> TreeNode<T> 
        createRoot(T nid) {
        return new TreeNode<>(nid, null, 0, true);
    }

    public static <T extends java.io.Serializable & Comparable<T>> TreeNode<T> 
        createRoot(T nid, T pid, int orders) {
        return new TreeNode<>(nid, pid, orders, true);
    }

    public static <T extends java.io.Serializable & Comparable<T>> TreeNode<T> 
        createRoot(T nid, T pid, int orders, boolean enabled) {
        return new TreeNode<>(nid, pid, orders, enabled);
    }

    /**
     * Returns a tree root node
     *  
     * @param node   the node for root
     * @return
     */
    public static <T extends java.io.Serializable & Comparable<T>> TreeNode<T> 
        createRoot(AbstractNode<T> node) {
        return new TreeNode<>(node);
    }

    public <E extends AbstractNode<T>> TreeNode<T> mount(List<E> nodes) {
        mount(nodes, false);
        return this;
    }

    /**
     * Mount a tree
     * 
     * @param list         子节点列表
     * @param ignoreOrphan {@code true}忽略孤儿节点
     */
    @SuppressWarnings("unchecked")
    public <E extends AbstractNode<T>> TreeNode<T> mount(@Nonnull List<E> list, 
                                                         boolean ignoreOrphan) {
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(list));

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
        this.level = 1; // root node level is 1
        this.path = null; // reset with null
        this.leftLeafCount = 0; // root node left leaf count is 1
        this.mount0(nodes, ignoreOrphan, this.nid);

        // 4、检查是否存在孤儿节点
        if (!ignoreOrphan && CollectionUtils.isNotEmpty(nodes)) {
            List<T> nids = nodes.stream().map(AbstractNode::getNid)
                                .collect(Collectors.toList());
            throw new RuntimeException("无效的孤儿节点：" + nids);
        }

        // 5、统计
        count();

        return this;
    }

    /**
     * 按继承方式展开节点：父子节点相邻 
     * 
     * should be before invoke {@link #mount(List)}
     * 
     * @return
     */
    public List<FlatNode<T>> flatInherit() {
        List<FlatNode<T>> collect = Lists.newArrayList();
        inherit(collect);
        return collect;
    }

    /**
     * 按层级展开节点：兄弟节点相邻
     * 
     * @return
     */
    public List<FlatNode<T>> flatHierarchy() {
        List<FlatNode<T>> collect = Lists.newArrayList(new FlatNode<>(this));
        hierarchy(collect);
        return collect;
    }

    // -----------------------------------------------------------private methods
    private <E extends AbstractNode<T>> List<AbstractNode<T>> before(List<E> nodes) {
        List<AbstractNode<T>> list = Lists.newArrayListWithCapacity(nodes.size());

        // nodes list
        for (AbstractNode<T> node : nodes) {
            if (node instanceof TreeNode) {
                list.addAll(((TreeNode<T>) node).flatInherit());
            } else {
                list.add(node); // node.clone()
            }
        }

        // the root node children
        if (CollectionUtils.isNotEmpty(this.children)) {
            List<FlatNode<T>> flat = this.flatInherit();
            list.addAll(flat.subList(1, flat.size()));
            this.children.clear();
        }
        return list;
    }

    private <E extends AbstractNode<T>> void mount0(
        List<E> nodes, boolean ignoreOrphan, T mountPidIfNull) {
        // current "this" is parent: AbstractNode parent = this;

        Set<Integer> uniqueOrders = Sets.newHashSet();
        // find child nodes for the current node
        for (Iterator<E> iter = nodes.iterator(); iter.hasNext();) {
            AbstractNode<T> node = iter.next();

            if (!ignoreOrphan && super.isEmpty(node.getPid())) {
                // 不忽略孤儿节点且节点的父节点为空，则其父节点视为根节点（挂载到根节点下）
                Fields.put(node, "pid", mountPidIfNull); // pid is final modify
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

                TreeNode<T> child = new TreeNode<>(node);
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
            this.children.sort(Comparator.comparing(TreeNode::getOrders));

            // recursion to mount child tree
            for (TreeNode<T> nt : this.children) {
                nt.mount0(nodes, ignoreOrphan, mountPidIfNull);
            }
        }

        this.setPath(Collects.add(this.path, this.nid)); // 节点路径追加自身的ID
    }

    private void inherit(List<FlatNode<T>> collect) {
        collect.add(new FlatNode<>(this));
        if (CollectionUtils.isNotEmpty(this.children)) {
            for (TreeNode<T> nt : this.children) {
                nt.inherit(collect);
            }
        }
    }

    private void hierarchy(List<FlatNode<T>> collect) {
        if (CollectionUtils.isNotEmpty(this.children)) {
            for (TreeNode<T> nt : this.children) {
                collect.add(new FlatNode<>(nt));
            }
            for (TreeNode<T> nt : this.children) {
                nt.hierarchy(collect);
            }
        }
    }

    private void count() {
        if (CollectionUtils.isNotEmpty(this.children)) { // 非叶子节点
            int maxChildTreeDepth = 0, sumTreeNodeCount = 0, 
                sumChildLeafCount = 0;
            TreeNode<T> child;
            for (int i = 0; i < this.children.size(); i++) {
                child = this.children.get(i);

                // 1、统计左叶子节点数量
                if (i == 0) {
                    // 最左子节点：左叶子节点个数=父节点的左叶子节点个数
                    child.leftLeafCount = this.leftLeafCount;
                } else {
                    // 若不是最左子节点，则其左叶子节点个数=
                    // 相邻左兄弟节点的左叶子节点个数+该兄弟节点的子节点个数
                    TreeNode<T> prevSibling = this.children.get(i - 1);
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
    public List<TreeNode<T>> getChildren() {
        return children;
    }

}
