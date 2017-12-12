package org.tfelab.proxy_hub.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

/**
 * Utils methods for deal with network
 *
 * @author Liu Dong
 */
public class Networks {

    private static Logger logger = LoggerFactory.getLogger(Networks.class);

    public static final int HOST_TYPE_IPV6 = 0;
    public static final int HOST_TYPE_IPV4 = 1;
    public static final int HOST_TYPE_DOMAIN = 2;

    /**
     * Ipv4, ipv6, or domain
     */
    public static int getHostType(String host) {
        if (host.contains(":") && !host.contains(".")) {
            return HOST_TYPE_IPV6;
        }
        if (host.matches("^[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}")) {
            return HOST_TYPE_IPV4;
        }
        return HOST_TYPE_DOMAIN;
    }

    /**
     * Generate wildcard host for long domains
     */
    public static String wildcardHost(String host) {
        if (getHostType(host) != HOST_TYPE_DOMAIN) {
            return host;
        }
        String[] items = host.split("\\.");
        if (items.length <= 3) {
            return host;
        }
        return "*." + items[items.length - 3] + "." + items[items.length - 2] + "." + items[items.length - 1];
    }

    /**
     * Split address to host and port
     */
    public static NetAddress parseAddress(String address) {
        int idx = address.indexOf(":");
        if (idx > 0) {
            String host = address.substring(0, idx);
            int port = Integer.parseInt(address.substring(idx + 1));
            return new NetAddress(host, port);
        } else {
            return new NetAddress(address, -1);
        }
    }


    public static List<NetworkInfo> getNetworkInfoList() {
        Enumeration<NetworkInterface> networkInterfaces;
        try {
            networkInterfaces = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            logger.warn("cannot get local network interface ip address", e);
            return Collections.emptyList();
        }
        List<NetworkInfo> list = new ArrayList<>();
        while (networkInterfaces.hasMoreElements()) {
            NetworkInterface networkInterface = networkInterfaces.nextElement();
            try {
                if (!networkInterface.isUp() || networkInterface.isPointToPoint()) {
                    continue;
                }
            } catch (SocketException e) {
                logger.warn("", e);
                continue;
            }
            String name = networkInterface.getName();
            Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
            while (inetAddresses.hasMoreElements()) {
                InetAddress inetAddress = inetAddresses.nextElement();
                if (inetAddress instanceof Inet4Address) {
                    String ip = inetAddress.getHostAddress();
                    list.add(new NetworkInfo(name, ip));
                } else if (inetAddress instanceof Inet6Address) {
                    String ip = inetAddress.getHostAddress();
                    list.add(new NetworkInfo(name, ip));
                }
            }
        }
        return list;
    }
}
