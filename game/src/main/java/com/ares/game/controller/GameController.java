package com.ares.game.controller;

import com.ares.common.bean.ServerType;
import com.ares.core.annotation.MsgId;
import com.ares.core.service.AresController;
import com.ares.core.tcp.AresTKcpContext;
import com.ares.core.timer.ScheduleService;
import com.ares.core.utils.AresContextThreadLocal;
import com.ares.dal.game.UserOnlineService;
import com.ares.dal.game.UserOnlineStateDO;
import com.ares.discovery.DiscoveryService;
import com.ares.game.bean.TimerBeanTest;
import com.ares.game.network.PeerConn;
import com.ares.game.player.GamePlayer;
import com.ares.game.service.PlayerRoleService;
import com.game.protoGen.ProtoCommon;
import com.game.protoGen.ProtoGame;
import com.game.protoGen.ProtoInner;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class GameController implements AresController {
    @Autowired
    private PlayerRoleService playerRoleService;
    @Autowired
    private PeerConn peerConn;
    @Autowired
    private UserOnlineService userOnlineService;
    @Autowired
    private DiscoveryService discoveryService;

    @MsgId(ProtoInner.InnerProtoCode.INNER_TO_GAME_LOGIN_REQ_VALUE)
    public void gameInnerLoginRequest(long pid, ProtoInner.InnerGameLoginRequest gameInnerLoginRequest) {
        /**
         * do some logic
         * check player token
         */
        log.info("======== gameInnerLoginRequest  ={}", gameInnerLoginRequest);
        AresTKcpContext aresTKcpContext = AresContextThreadLocal.get();
        GamePlayer player = playerRoleService.getPlayer(gameInnerLoginRequest.getUid());
        if (player == null) {
            player = playerRoleService.createGamePlayer(gameInnerLoginRequest.getUid(), "hello");
        }

        peerConn.recordPlayerFromContext(ServerType.GATEWAY, gameInnerLoginRequest.getUid(), aresTKcpContext.getCtx());
        /**
         * all player data should be loaded here
         */
        saveUserOnlineStates(player);
        sendPlayerLoginResponse(gameInnerLoginRequest.getUid());
    }

    private void saveUserOnlineStates(GamePlayer gamePlayer) {
        UserOnlineStateDO userOnlineStateDO = new UserOnlineStateDO();
        userOnlineStateDO.setId(gamePlayer.getUid());
        userOnlineStateDO.setGsrId(discoveryService.getEtcdRegister().getMyselfNodeInfo().getServiceId());
        userOnlineService.saveUserOnlineDo(userOnlineStateDO);
    }


    private void sendPlayerLoginResponse(long pid) {
        GamePlayer player = playerRoleService.getPlayer(pid);
        if (player == null) {
            log.error(" pid={} not found", pid);
            return;
        }
        ProtoInner.InnerGameLoginResponse innerGameLoginRes = ProtoInner.InnerGameLoginResponse.newBuilder()
                .setUid(pid).build();
        peerConn.sendGateWayMsg(pid, ProtoInner.InnerProtoCode.INNER_TO_GAME_LOGIN_RES_VALUE, innerGameLoginRes);
    }


    @MsgId(ProtoInner.InnerProtoCode.INNER_PLAYER_DISCONNECT_REQ_VALUE)
    public void playerDisconnected(long pid, ProtoInner.InnerPlayerDisconnectRequest innerLoginRequest) {
        log.info("======== playerDisconnected  ={}", innerLoginRequest);
    }

    @MsgId(ProtoCommon.ProtoCode.PERFORMANCE_TEST_REQ_VALUE)
    public void performanceTest(long uid, ProtoGame.PerformanceTestReq req) {
        GamePlayer player = playerRoleService.getPlayer(uid);
        if (player == null) {
            log.error(" uid ={} not found", uid);
            return;
        }

        //for test
        long begin = System.currentTimeMillis();
        playerRoleService.asynUpdateTest(player.getRoleDO());
        long now = System.currentTimeMillis();
        log.info("-----performanceTest  uid ={} body={}  dis ={} ", uid, req, (now - begin));
        ProtoGame.PerformanceTestRes performanceBOyd = ProtoGame.PerformanceTestRes.newBuilder()
                .setResBody("performanceBody")
                .setSendTime(req.getSendTime())
                .setSomeId(44444).build();
        peerConn.sendGateWayMsg(uid, ProtoCommon.ProtoCode.PERFORMANCE_TEST_RES_VALUE, performanceBOyd);


        //for timer test
        log.info("==================================== start timer call begin");
        TimerBeanTest timerBeanTest = new TimerBeanTest();
        timerBeanTest.msg = "timerTest";
        ScheduleService.INSTANCE.executeTimerTaskWithMS(playerRoleService::onTimerTest, timerBeanTest, 3000L);
    }
}
