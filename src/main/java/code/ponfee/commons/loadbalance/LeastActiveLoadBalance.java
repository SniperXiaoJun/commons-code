package code.ponfee.commons.loadbalance;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 最少已使用法
 * 需要考虑如何恢复权重
 * @author fupf
 */
public class LeastActiveLoadBalance extends AbstractLoadBalance {
    private final List<Map.Entry<String, Integer>> servers;

    public LeastActiveLoadBalance(Map<String, Integer> serverMap) {
        this.servers = new ArrayList<>(serverMap.entrySet());
        this.servers.sort(Comparator.comparing(Entry::getValue));
        //this.servers.sort((o1, o2) -> o1.getValue().compareTo(o2.getValue()));
        //Collections.sort(servers, Comparator.comparing(Entry<String, Integer>::getValue));
    }

    @Override
    public String select() {
        return servers.get(0).getKey();
    }

    public static void main(String[] args) {
        //List<String> list = Lists.newArrayList();
        //Collections.sort(list); == list.sort(null);
        //Collections.sort(list, comparator); == list.sort(comparator);
    }
}
