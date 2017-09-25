package code.ponfee.commons.loadbalance;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
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

        //this.servers.sort(Comparator.comparing(e -> e.getValue()));
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

        HashMap<String, Integer> serverWeightMap = new HashMap<>();
        serverWeightMap.put("192.168.1.100", 1);
        serverWeightMap.put("192.168.1.101", 1);
        // 权重为4
        serverWeightMap.put("192.168.1.102", 4);
        serverWeightMap.put("192.168.1.103", 1);
        serverWeightMap.put("192.168.1.104", 1);
        // 权重为3
        serverWeightMap.put("192.168.1.105", 3);
        serverWeightMap.put("192.168.1.106", 1);
        // 权重为2
        serverWeightMap.put("192.168.1.107", 2);
        serverWeightMap.put("192.168.1.108", 1);
        serverWeightMap.put("192.168.1.109", 1);
        serverWeightMap.put("192.168.1.110", 0);
        serverWeightMap.put("192.168.1.111", 5);

        AbstractLoadBalance slb = new LeastActiveLoadBalance(serverWeightMap);
        System.out.println(slb.select());
    }
}
