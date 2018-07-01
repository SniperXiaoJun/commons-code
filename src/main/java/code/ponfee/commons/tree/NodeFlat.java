/* __________              _____                                          *\
** \______   \____   _____/ ____\____   ____        Ponfee's code         **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \       (c) 2017-2018, MIT    **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/       http://www.ponfee.cn  **
**  |____|   \____/|___|  /__|  \___  >\___  >                            **
**                      \/          \/     \/                             **
\*                                                                        */

package code.ponfee.commons.tree;

import org.apache.commons.collections4.CollectionUtils;

/**
 * 节点扁平结构
 * 
 * @author Ponfee
 */
public final class NodeFlat<T extends java.io.Serializable & Comparable<T>>
    extends AbstractNode<T> {

    private static final long serialVersionUID = 5191371614061952661L;

    private final boolean leaf; // 是否叶子节点

    public NodeFlat(NodeTree<T> nt) {
        super(nt.getNid(), nt.getPid(), nt.getOrders(), 
              nt.isEnabled(), nt.getAttach());

        super.available = nt.isAvailable();
        super.level = nt.getLevel();
        super.path = nt.getPath();

        super.treeNodeCount = nt.treeNodeCount;
        super.childLeafCount = nt.childLeafCount;
        super.leftLeafCount = nt.leftLeafCount;
        super.treeMaxDepth = nt.treeMaxDepth;

        this.leaf = CollectionUtils.isEmpty(nt.getChildren());
    }

    // ----------------------------------------------getter/setter
    public boolean isLeaf() {
        return leaf;
    }

}
