package code.ponfee.commons.loadbalance;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 最少活跃数
 *
 * @author fupf
 */
public class LeastActiveLoadBalance extends AbstractLoadBalance {

    private final Map<String, AtomicInteger> serverMap;
    private final List<Map.Entry<String, AtomicInteger>> servers;

    public LeastActiveLoadBalance(Map<String, AtomicInteger> serverMap) {
        this.serverMap = serverMap;
        this.servers = new ArrayList<>(serverMap.entrySet());
        this.servers.sort(Comparator.comparing(e -> e.getValue().get()));

        //this.servers.sort(Comparator.comparing(Entry::getValue));
        //this.servers.sort(Comparator.comparing(e -> e.getValue().get()));
        //this.servers.sort((o1, o2) -> o1.getValue().compareTo(o2.getValue()));
        //Collections.sort(servers, Comparator.comparing(Entry<String, Integer>::getValue));
    }

    @Override
    public String select() {
        return servers.get(0).getKey();
    }

    /**
     * 调用前活跃数加1
     *
     * @param server
     */
    public void begin(String server) {
        serverMap.get(server).incrementAndGet();
    }

    /**
     * 调用后活跃数减1
     *
     * @param server
     */
    public void end(String server) {
        serverMap.get(server).decrementAndGet();
    }

    public static void main(String[] args) {
        //List<String> list = Lists.newArrayList();
        //Collections.sort(list); == list.sort(null);
        //Collections.sort(list, comparator); == list.sort(comparator);

        HashMap<String, AtomicInteger> serverWeightMap = new HashMap<>();
        serverWeightMap.put("192.168.1.100", new AtomicInteger(1));
        serverWeightMap.put("192.168.1.101", new AtomicInteger(1));
        // 权重为4
        serverWeightMap.put("192.168.1.102", new AtomicInteger(4));
        serverWeightMap.put("192.168.1.103", new AtomicInteger(1));
        serverWeightMap.put("192.168.1.104", new AtomicInteger(1));
        // 权重为3
        serverWeightMap.put("192.168.1.105", new AtomicInteger(3));
        serverWeightMap.put("192.168.1.106", new AtomicInteger(1));
        // 权重为2
        serverWeightMap.put("192.168.1.107", new AtomicInteger(2));
        serverWeightMap.put("192.168.1.108", new AtomicInteger(1));
        serverWeightMap.put("192.168.1.109", new AtomicInteger(1));
        serverWeightMap.put("192.168.1.110", new AtomicInteger(0));
        serverWeightMap.put("192.168.1.111", new AtomicInteger(5));

        AbstractLoadBalance slb = new LeastActiveLoadBalance(serverWeightMap);
        System.out.println(slb.select());
    }
}
