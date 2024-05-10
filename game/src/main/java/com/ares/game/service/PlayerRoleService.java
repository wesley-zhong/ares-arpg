package com.ares.game.service;

import com.ares.common.util.LRUCache;
import com.ares.game.DO.RoleDO;
import com.ares.game.bean.TimerBeanTest;
import com.ares.game.player.Player;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PlayerRoleService {
    private int MAX_PLAYER_CACHE = 2000;
    // this should be use lru replaced

    public static PlayerRoleService Instance;

    public PlayerRoleService() {
        Instance = this;
    }

    private LRUCache<Long, Player> playerLRUCache = new LRUCache<>(MAX_PLAYER_CACHE);

    public Player getPlayer(long uid) {
        return playerLRUCache.get(uid);
    }

    public void cachePlayer(Player player) {
        playerLRUCache.put(player.getUid(), player);
    }

//    public GamePlayer createGamePlayer(long uid, String name) {
//        GamePlayer gamePlayer = new GamePlayer(uid);
//        RoleDO roleDO = new RoleDO();
//        roleDO.setUid(uid);
//        roleDO.setId(uid);
//        roleDO.setName(name);
//        roleDAO.insert(roleDO);
//        gamePlayer.setRoleDO(roleDO);
//        playerMap.put(uid, gamePlayer);
//        return gamePlayer;
//    }

    public void asynUpdateTest(RoleDO roleDO) {
//        for (int i = 0; i < 2000; i++) {
//            roleDO.setCountTest(roleDO.getCountTest() + 1);
//            roleDAO.asynUpInsert(roleDO);
//           // roleDAO.upInsert(roleDO);
//        }
    }

    public void onTimerTest(TimerBeanTest timerBeanTest) {
        //  log.info("============ onTimerTest msg ={}", timerBeanTest.msg);
    }
}
