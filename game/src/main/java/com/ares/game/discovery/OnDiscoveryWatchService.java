package com.ares.game.discovery;

import com.ares.discovery.DiscoveryService;
import com.ares.transport.bean.ServerNodeInfo;
import com.ares.transport.bean.ServerNodeSortedMap;
import com.ares.transport.client.AresTcpClient;
import io.etcd.jetcd.watch.WatchEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
public class OnDiscoveryWatchService implements com.ares.discovery.transfer.OnDiscoveryWatchService {
    @Autowired
    private AresTcpClient aresTcpClient;

    private final ServerNodeSortedMap serverNodeSortedMap = new ServerNodeSortedMap();
    @Autowired
    @Lazy
    private DiscoveryService discoveryService;

    @Value("${netty.client.CONN_COUNT:8}")
    private int clientConnCount;


    @Override
    public ServerNodeInfo getServerNodeInfo(String serviceId) {
        return discoveryService.getEtcdDiscovery().getServerList().get(serviceId);
    }

    public ServerNodeInfo getLowerLoadServerNodeInfo(int serverType) {
        return serverNodeSortedMap.getLeastCountServerNode(serverType);
    }
    @Override
    public ServerNodeInfo getMySelfNodeInfo(){
        return discoveryService.getEtcdRegister().getMyselfNodeInfo();
    }


    @Override
    public Void onWatchServiceChange(WatchEvent.EventType eventType, ServerNodeInfo serverNodeInfo) {
        if (eventType == WatchEvent.EventType.PUT) {
            serverNodeSortedMap.add(serverNodeInfo);
            aresTcpClient.connect(serverNodeInfo, clientConnCount);
            return null;
        }
        if (eventType == WatchEvent.EventType.DELETE) {
            aresTcpClient.close(serverNodeInfo);
            serverNodeSortedMap.remove(serverNodeInfo);
        }
        return null;
    }
}
