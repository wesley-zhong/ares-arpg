package com.ares.transport.bean;

import org.springframework.util.CollectionUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

public class ServerNodeSortedMap {
    private final Map<Integer, ConcurrentSkipListSet<ServerNodeInfo>> serverTypeServerNodeInfoMap = new ConcurrentHashMap<>();

    public void add(ServerNodeInfo serverNodeInfo) {
        ConcurrentSkipListSet<ServerNodeInfo> serverNodeInfos = serverTypeServerNodeInfoMap.computeIfAbsent(serverNodeInfo.getServerType(), (value) -> createGatewayWatcher());
        serverNodeInfos.add(serverNodeInfo);
    }

    public boolean remove(ServerNodeInfo serverNodeInfo) {
        ConcurrentSkipListSet<ServerNodeInfo> serverNodeInfos = serverTypeServerNodeInfoMap.get(serverNodeInfo.getServerType());
        if (CollectionUtils.isEmpty(serverNodeInfos)) {
            return false;
        }
        return serverNodeInfos.remove(serverNodeInfo);
    }

    public ServerNodeInfo getLeastCountServerNode(int serverType){
        ConcurrentSkipListSet<ServerNodeInfo> serverNodeInfos = serverTypeServerNodeInfoMap.get(serverType);
        if(CollectionUtils.isEmpty(serverNodeInfos)){
            return  null;
        }
        return serverNodeInfos.first();
    }


    private ConcurrentSkipListSet<ServerNodeInfo> createGatewayWatcher() {
        return new ConcurrentSkipListSet<>((o1, o2) -> {
            int o1Count = o1.getOnlineCount();
            int o2Count = o2.getOnlineCount();
            return o1Count - o2Count;
        });
    }
}
