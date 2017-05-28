package code.ponfee.commons.util;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.regex.Pattern;

/**
 * 网络工具类
 * @author fupf
 */
public class Networks {
    /** ip正则 */
    private static final Pattern IP_PATTERN = Pattern.compile("^(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|[1-9])\\.(00?\\d|1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\.(00?\\d|1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\.(00?\\d|1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)$");

    /** 掩码 */
    private static final long[] MASK = { 0x000000FF, 0x0000FF00, 0x00FF0000, 0xFF000000 };

    /**
     * 获取主机域名
     * @return
     */
    public static String getHostName() {
        try {
            for (Enumeration<NetworkInterface> itfs = NetworkInterface.getNetworkInterfaces(); itfs.hasMoreElements();) {
                NetworkInterface net = itfs.nextElement();
                if (net.isLoopback()) continue;

                for (Enumeration<InetAddress> addrs = net.getInetAddresses(); addrs.hasMoreElements();) {
                    InetAddress addr = addrs.nextElement();
                    if (!addr.getCanonicalHostName().equalsIgnoreCase(addr.getHostAddress())) {
                        return addr.getCanonicalHostName();
                    } else if (addr.isSiteLocalAddress()) {
                        return addr.getHostAddress();
                    }
                }
            }
            return "localhost";
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取内网IP
     * @return 内网IP
     */
    public static String getSiteIp() {
        try {
            for (Enumeration<NetworkInterface> itfs = NetworkInterface.getNetworkInterfaces(); itfs.hasMoreElements();) {
                for (Enumeration<InetAddress> addrs = itfs.nextElement().getInetAddresses(); addrs.hasMoreElements();) {
                    InetAddress addr = addrs.nextElement();
                    //if (addr.isSiteLocalAddress()) return addr.getHostAddress();
                    if (addr != null && Inet4Address.class.isInstance(addr)
                        && !addr.isLinkLocalAddress() && !addr.isLoopbackAddress()) {
                        return addr.getHostAddress();
                    }
                }
            }
            return "127.0.0.1";
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * ip转long
     * @param ip
     * @return
     */
    public static long ip2long(String ip) {
        if (!IP_PATTERN.matcher(ip).matches()) {
            throw new IllegalArgumentException("invalid ip address[" + ip + "]");
        }
        String[] ipNums = ip.split("\\.");
        return (Long.parseLong(ipNums[0]) << 24) + (Long.parseLong(ipNums[1]) << 16)
            + (Long.parseLong(ipNums[2]) << 8) + (Long.parseLong(ipNums[3]));
    }

    /**
     * long转ip
     * @param ip
     * @return
     */
    public static String long2ip(long ip) {
        StringBuilder ipAddress = new StringBuilder();
        for (int i = 0; i < MASK.length; i++) {
            ipAddress.insert(0, (ip & MASK[i]) >> (i * 8));
            if (i < MASK.length - 1) ipAddress.insert(0, ".");
        }
        return ipAddress.toString();
    }

    public static int ipReduce(String ip, boolean isPoint) {
        if (!IP_PATTERN.matcher(ip).matches()) {
            throw new IllegalArgumentException("invalid ip address[" + ip + "]");
        }

        String[] ipNums = ip.split("\\.");
        int num = 0;
        for (int i = 0; i < ipNums.length; i++) {
            num += Integer.parseInt(ipNums[i]);
            if (!isPoint) continue;

            String[] sections = ipNums[i].split("");
            for (int j = 0; j < sections.length; j++) {
                if ("".equals(sections[j])) continue;
                num += Integer.parseInt(sections[j]);
            }
        }

        return num;
    }

    public static int ipReduce(String ip) {
        return ipReduce(ip, true);
    }

    public static int ipReduce(boolean isPoint) {
        return ipReduce(Networks.getSiteIp(), isPoint);
    }

    public static int ipReduce() {
        return ipReduce(Networks.getSiteIp(), true);
    }

    public static void main(String[] args) {
        System.out.println(getSiteIp());
        /*System.out.println(ipReduce("114.55.92.22", false));
        System.out.println(ipReduce("114.55.108.14", false));
        System.out.println(ipReduce("114.55.108.73", false));
        System.out.println(ipReduce("255.255.255.255", false));
        System.out.println(ipReduce(false));
        System.out.println("===============================");
        System.out.println(ipReduce("114.55.92.22"));
        System.out.println(ipReduce("114.55.108.14"));
        System.out.println(ipReduce("114.55.108.73"));
        System.out.println(ipReduce("255.255.255.255"));
        System.out.println(ipReduce());*/
    }
}
