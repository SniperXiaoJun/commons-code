/* __________              _____                                          *\
** \______   \____   _____/ ____\____   ____        Ponfee's code         **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \       (c) 2017-2018, MIT    **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/       http://www.ponfee.cn  **
**  |____|   \____/|___|  /__|  \___  >\___  >                            **
**                      \/          \/     \/                             **
\*                                                                        */

package code.ponfee.commons.tree;

/**
 * 带名称的节点
 * 
 * The AbstractNode.attach example
 * 
 * @author Ponfee
 */
public class NamedNode<T extends java.io.Serializable & Comparable<T>>
    extends AbstractNode<T> {

    private static final long serialVersionUID = 7891325205513770857L;

    private final String name; // 节点名称

    /**
     * Speic the NameNode class Constructor
     * 
     * @param nid
     * @param pid
     * @param orders
     * @param enabled
     * @param name
     */
    public NamedNode(T nid, T pid, int orders, 
                     boolean enabled, String name) {
        super(nid, pid, orders, enabled, null);
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
