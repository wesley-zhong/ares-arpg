package com.ares.transport.peer;

import com.ares.transport.bean.ServerNodeInfo;
import com.ares.transport.bean.TcpConnServerInfo;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class PeerMgr {
    private final Map<String, TcpConnServerInfo> allServerIdConnMap = new ConcurrentHashMap<>();
    private final Map<Long, TcpConnServerInfo> serviceIdConnMap = new ConcurrentHashMap<>();

    public TcpConnServerInfo addPeerConn(ServerNodeInfo serverNodeInfo, ChannelHandlerContext context) {
        TcpConnServerInfo tcpConnServerInfo = allServerIdConnMap.computeIfAbsent(serverNodeInfo.getServiceId(), (key) -> new TcpConnServerInfo(serverNodeInfo, 8));
        tcpConnServerInfo.addTcpConn(context.channel());
        long typeId = createServerVId(serverNodeInfo.getServerType(), serverNodeInfo.getId());
        serviceIdConnMap.putIfAbsent(typeId, tcpConnServerInfo);
        return tcpConnServerInfo;
    }


    public TcpConnServerInfo addPeerConn(TcpConnServerInfo tcpConnServerInfo) {
        ServerNodeInfo serverNodeInfo = tcpConnServerInfo.getServerNodeInfo();
        allServerIdConnMap.put(serverNodeInfo.getServiceId(), tcpConnServerInfo);
        long typeId = createServerVId(serverNodeInfo.getServerType(), serverNodeInfo.getId());
        serviceIdConnMap.putIfAbsent(typeId, tcpConnServerInfo);
        return tcpConnServerInfo;
    }

    public void delete(ServerNodeInfo serverNodeInfo) {
        TcpConnServerInfo tcpConnServerInfo = allServerIdConnMap.remove(serverNodeInfo.getServiceId());
        if (tcpConnServerInfo == null) {
            log.error("serverNodeInfo ={} not found connection", serverNodeInfo);
        }
        serviceIdConnMap.remove(serverNodeInfo.getId());
    }

    public TcpConnServerInfo getServerTcpConnInfo(String serviceId) {
        return allServerIdConnMap.get(serviceId);
    }

    public TcpConnServerInfo getServerTcpConnInfo(ServerNodeInfo serverNodeInfo) {
        return allServerIdConnMap.get(serverNodeInfo.getServiceId());
    }

    public TcpConnServerInfo getServerTcpConnInfo(int type, int serverId) {
        return serviceIdConnMap.get(createServerVId(type, serverId));
    }

    private long createServerVId(int type, int workerId) {
        return ((long) workerId & 0xFFFFFFFFl) | (((long) type << 32) & 0xFFFFFFFF00000000l);
    }
}
