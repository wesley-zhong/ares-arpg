package com.ares.discovery.utils;

import lombok.extern.slf4j.Slf4j;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

@Slf4j
public class NetUtils {
    public static List<String> getIpAddress() {
        List<String> list = new LinkedList<>();
        try {
            Enumeration<NetworkInterface> enumeration = NetworkInterface.getNetworkInterfaces();
            while (enumeration.hasMoreElements()) {
                NetworkInterface network = enumeration.nextElement();
                if (network.isVirtual() || !network.isUp() || network.isLoopback())
                    continue;
                Enumeration<InetAddress> addresses = network.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress address = (InetAddress) addresses.nextElement();
                    if (address instanceof Inet4Address) {
                        list.add(address.getHostAddress());
                    }
                }
            }
            return list;
        } catch (Exception e) {
            log.error("======error", e);
        }
        return null;
    }

    public static String createServiceId(String appName, String ip, int port, int areaId) {
        if(areaId == 0){
            return  appName + "/" + ip + ":" + port;
        }
        return areaId + "/" + appName + "/" + ip + ":" + port;
    }
}
