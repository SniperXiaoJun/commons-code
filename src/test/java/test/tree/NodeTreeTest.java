/* __________              _____                                          *\
** \______   \____   _____/ ____\____   ____        Ponfee's code         **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \       (c) 2017-2018, MIT    **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/       http://www.ponfee.cn  **
**  |____|   \____/|___|  /__|  \___  >\___  >                            **
**                      \/          \/     \/                             **
\*                                                                        */

package test.tree;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import code.ponfee.commons.json.Jsons;
import code.ponfee.commons.tree.NamedNode;
import code.ponfee.commons.tree.AbstractNode;
import code.ponfee.commons.tree.NodeTree;

/**
 * 
 * @author Ponfee
 */
public class NodeTreeTest {

    @Test
    public void test1() {
        List<AbstractNode<String>> list = new ArrayList<>();
        list.add(new NamedNode<>("100000", null, 1, true, "nid100000"));
        list.add(new NamedNode<>("100010", "100000", 2, true, "nid100010"));
        list.add(new NamedNode<>("100011", "100010", 3, false, "nid100011"));
        list.add(new NamedNode<>("100012", "100010", 4, true, "nid100012"));
        list.add(new NamedNode<>("100020", "100000", 1, false, "nid100020"));
        list.add(new NamedNode<>("100021", "100020", 5, true, "nid100020"));
        list.add(new NamedNode<>("100022", "100020", 3, false, "nid100022"));

        list.add(new NamedNode<>("200000", null, 2, true, "nid200000"));

        list.add(new NamedNode<>("300000", null, 4, true, "nid300000"));

        list.add(new NamedNode<>("400000", null, 5, true, "nid400000"));

        NodeTree<String> subtree = new NodeTree<>("400010", "400000", 1, true);
        List<AbstractNode<String>> list1 = new ArrayList<>();
        list1.add(new NamedNode<>("400011", "400010", 2, true,  "nid400011"));
        list1.add(new NamedNode<>("400012", "400010", 3, false, "nid400012"));
        subtree.build(list1);
        list.add(subtree);

        
        list.add(new NamedNode<>("500000", null, 3, false, "nid500000"));
        list.add(new NamedNode<>("500010", "500000", 3, true, "nid500010"));
        list.add(new NamedNode<>("500011", "500010", 3, true, "nid500011"));

        NodeTree<String> root = new NodeTree<>(NodeTree.DEFAULT_ROOT_NAME);
        System.out.println(Jsons.toJson(root));
        root.build(list);
        System.out.println(Jsons.toJson(root));
        System.out.println(Jsons.toJson(root.flatInherit()));
        System.out.println(Jsons.toJson(root.flatHierarchy()));
    }

    @Test
    public void test2() {
        List<AbstractNode<String>> list = new ArrayList<>();
        list.add(new NamedNode<>("a", "b", 2, true, "")); // 节点循环依赖
        list.add(new NamedNode<>("b", "a", 2, true, ""));
    }

    @Test
    public void test3() {
        List<AbstractNode<String>> list = new ArrayList<>();
        list.add(new NamedNode<>("100001", null, 2, true, "nid100010")); // 节点编号不能为空
        list.add(new NamedNode<>(null, "100001", 2, true, "nid100010"));
        NodeTree<String> root = new NodeTree<>(NodeTree.DEFAULT_ROOT_NAME);
        root.build(list);
        System.out.println(Jsons.toJson(root));
    }
    
    @Test
    public void test4() {
        List<AbstractNode<String>> list = new ArrayList<>();
        list.add(new NamedNode<>("100000", "notfound", 1, true, "nid100000")); // 无效的孤儿节点
        list.add(new NamedNode<>("200000", "notfound", 1, true, "nid200000")); // 无效的孤儿节点

        new NodeTree<>(NodeTree.DEFAULT_ROOT_NAME).build(list);
    }
}
