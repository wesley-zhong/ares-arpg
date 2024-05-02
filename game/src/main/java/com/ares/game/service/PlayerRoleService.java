package com.ares.game.service;

import com.ares.game.DO.RoleDO;
import com.ares.game.bean.TimerBeanTest;
import com.ares.game.player.GamePlayer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class PlayerRoleService {
    // this should be use lru replaced
    private final Map<Long, GamePlayer> playerMap = new ConcurrentHashMap<>();

    public GamePlayer getPlayer(long uid) {
        return playerMap.get(uid);
    }

    public void cachePlayer(GamePlayer gamePlayer) {
        playerMap.put(gamePlayer.getUid(), gamePlayer);
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
