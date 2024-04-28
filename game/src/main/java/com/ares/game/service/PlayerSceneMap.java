package com.ares.game.service;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class PlayerSceneMap {
    private final Map<Long, Long> playerSceneMap = new ConcurrentHashMap<>();

    public void recordPlayerScene(long uid, long sceneInstanceId) {
        playerSceneMap.put(uid, sceneInstanceId);
    }

    public long getPlayerSceneId(long uid) {
        Long sceneId = playerSceneMap.get(uid);
        if (sceneId == null) {
            return 0L;
        }
        return sceneId;
    }
}
