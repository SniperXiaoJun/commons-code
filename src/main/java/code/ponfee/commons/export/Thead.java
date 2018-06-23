package code.ponfee.commons.export;

import code.ponfee.commons.tree.NodeBase;

/**
 * 表头
 * 
 * @author fupf
 */
public class Thead extends NodeBase<Integer> {

    private static final long serialVersionUID = 1898674740598755648L;

    private final String name; // 列名称
    private final Tmeta tmeta; // 列配置信息

    public Thead(String name, Integer nid, Integer pid, Tmeta tmeta) {
        this(nid, pid, nid, name, tmeta);
    }

    public Thead(String name, Integer nid, Integer pid) {
        this(nid, pid, nid, name, null);
    }

    public Thead(String name, Integer nid, Integer pid, int orders) {
        this(nid, pid, orders, name, null);
    }

    /**
     * 
     * @param nid
     * @param pid
     * @param orders
     * @param name
     * @param tmeta
     */
    public Thead(Integer nid, Integer pid, int orders, 
                 String name, Tmeta tmeta) {
        super(nid, pid, orders, true, null);
        this.name = name;
        this.tmeta = tmeta;
    }

    public String getName() {
        return name;
    }

    public Tmeta getTmeta() {
        return tmeta;
    }

}
