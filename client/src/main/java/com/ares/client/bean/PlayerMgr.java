package com.ares.client.bean;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerMgr {
    private Map<Long, ClientPlayer> clientPlayerMap = new ConcurrentHashMap<>();
    public static PlayerMgr Instance = new PlayerMgr();

    public void addClientPlayer(ClientPlayer clientPlayer) {
        clientPlayerMap.put(clientPlayer.getUId(), clientPlayer);
    }

    public ClientPlayer getClientPlayer(long pid) {
        return clientPlayerMap.get(pid);
    }

    public Collection<ClientPlayer> getAllClientPlayer() {
        return clientPlayerMap.values();
    }
}
