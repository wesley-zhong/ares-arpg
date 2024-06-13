package com.ares.game.service;

import com.ares.common.util.LRUCache;
import com.ares.game.bean.TimerBeanTest;
import com.ares.game.player.Player;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PlayerRoleService implements InitializingBean {
    @Value("${server.max-player-count:20000}")
    private int maxPlayerCount;
    public static PlayerRoleService Instance;

    public PlayerRoleService() {
        Instance = this;
    }

    private LRUCache<Long, Player> playerLRUCache;

    public Player getPlayer(long uid) {
        return playerLRUCache.get(uid);
    }

    public void cachePlayer(Player player) {
        playerLRUCache.put(player.getUid(), player);
    }

    public void removePlayer(Player player) {
        playerLRUCache.remove(player.getUid());
    }

    public void onTimerTest(TimerBeanTest timerBeanTest) {
        //  log.info("============ onTimerTest msg ={}", timerBeanTest.msg);
    }

    @Override
    public void afterPropertiesSet() {
        playerLRUCache = new LRUCache<>(maxPlayerCount);
    }
}
