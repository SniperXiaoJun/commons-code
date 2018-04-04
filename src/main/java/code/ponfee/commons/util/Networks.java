package code.ponfee.commons.util;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

import org.apache.commons.lang3.StringUtils;

/**
 * 网络工具类
 * 
 * @author fupf
 */
public final class Networks {
    private Networks() {}

    /** the max ip value*/
    public static final long MAX_IP_VALUE = (1L << 32) - 1; // toLong("255.255.255.255")

    /** 掩码 */
    private static final long[] MASK = { 0xFF000000, 0x00FF0000, 0x0000FF00, 0x000000FF };

    /** local ip */
    public static final String LOCAL_IP = getLocalIp();

    /** 
     * getMachineNetworkFlag 获取机器的MAC或者IP，优先获取MAC
     * @param ia
     * @return
     */
    public static String getMacOrIp(InetAddress ia) {
        if (ia == null) {
            try {
                ia = InetAddress.getLocalHost();
            } catch (UnknownHostException e) {
                throw new RuntimeException(e);
            }
        }
        String mac = getMacAddress(ia);
        return StringUtils.isBlank(mac) ? getIpAddress(ia) : mac;
    }

    /** 
     * 获取指定地址的mac地址，不指定默认取本机的mac地址
     * @param ia
     */
    public static String getMacAddress(InetAddress ia) {
        byte[] mac;
        try {
            if (ia == null) {
                ia = InetAddress.getLocalHost();
            }
            mac = NetworkInterface.getByInetAddress(ia).getHardwareAddress();
        } catch (SocketException | UnknownHostException e) {
            throw new RuntimeException(e);
        }

        if (mac == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder(17);
        for (int i = 0; i < mac.length; i++) {
            if (i != 0) {
                sb.append("-");
            }
            sb.append(Bytes.hexEncode(mac[i], false));
        }
        return sb.toString();
    }

    /** 
     * 获取指定地址的ip地址，不指定默认取本机的ip地址
     * @param ia
     */
    private static String getIpAddress(InetAddress ia) {
        return ia.getHostAddress();
    }

    /**
     * 获取主机域名
     * @return
     */
    public static String getHostName() {
        try {
            for (Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces(); e.hasMoreElements();) {
                NetworkInterface net = e.nextElement();
                if (net.isLoopback()) {
                    continue;
                }

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
    public static String getLocalIp() {
        try {
            for (Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces(); e.hasMoreElements();) {
                for (Enumeration<InetAddress> a = e.nextElement().getInetAddresses(); a.hasMoreElements();) {
                    InetAddress addr = a.nextElement();
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
     * ip转long，max  4294967295
     * @param ip
     * @return
     */
    public static long toLong(String ip) {
        if (!RegexUtils.isIp(ip)) {
            throw new IllegalArgumentException("invalid ip address[" + ip + "]");
        }
        String[] ipNums = ip.split("\\.");
        return (Long.parseLong(ipNums[0]) << 24) 
             + (Long.parseLong(ipNums[1]) << 16)
             + (Long.parseLong(ipNums[2]) <<  8) 
             + (Long.parseLong(ipNums[3])      );
    }

    /**
     * long转ip
     * @param ip
     * @return
     */
    public static String fromLong(long ip) {
        return new StringBuilder(15)
            .append((ip & MASK[0]) >> 24).append('.')
            .append((ip & MASK[1]) >> 16).append('.')
            .append((ip & MASK[2]) >>  8).append('.')
            .append((ip & MASK[3])      ).toString();
    }

    private static void bindPort(String host, int port) throws IOException {
        try (Socket s = new Socket()) {
            s.bind(new InetSocketAddress(host, port));
        } catch (IOException e) {
            throw e;
        }
    }

    /**
     * 判断端口是否被占用
     * @param port 待测试端口
     * @return boolean
     */
    public static boolean isPortAvailable(int port) {
        try {
            bindPort("0.0.0.0", port);
            bindPort(InetAddress.getLocalHost().getHostAddress(), port);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static int getAvailablePort(int port) {
        int result = port;
        while (true) {
            if (isPortAvailable(result)) {
                break;
            }
            result++;
        }
        return result;
    }

}
