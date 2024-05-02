package com.ares.game.controller;

import com.ares.common.bean.ServerType;
import com.ares.core.annotation.MsgId;
import com.ares.core.service.AresController;
import com.ares.core.tcp.AresTKcpContext;
import com.ares.core.utils.AresContextThreadLocal;
import com.ares.dal.game.UserOnlineService;
import com.ares.discovery.DiscoveryService;
import com.ares.game.DO.RoleDO;
import com.ares.game.network.PeerConn;
import com.ares.game.player.GamePlayer;
import com.ares.game.service.PlayerLoginService;
import com.ares.game.service.PlayerRoleService;
import com.game.protoGen.ProtoCommon;
import com.game.protoGen.ProtoGame;
import com.game.protoGen.ProtoInner;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Component
@Slf4j
public class GameInnerLoginController implements AresController {
    @Autowired
    private PlayerRoleService playerRoleService;
    @Autowired
    private PeerConn peerConn;

    @Autowired
    private PlayerLoginService playerLoginService;

    @MsgId(ProtoInner.InnerMsgId.INNER_TO_GAME_LOGIN_REQ_VALUE)
    public void gameInnerLoginRequest(long pid, ProtoInner.InnerGameLoginRequest gameInnerLoginRequest) {
        playerLoginService.gamePlayerInnerLogin(pid,gameInnerLoginRequest);
    }


    @MsgId(ProtoInner.InnerMsgId.INNER_PLAYER_DISCONNECT_REQ_VALUE)
    public void playerDisconnected(long pid, ProtoInner.InnerPlayerDisconnectRequest innerLoginRequest) {
        log.info("======== playerDisconnected  ={}", innerLoginRequest);
    }

    private static AtomicInteger atomicCount = new AtomicInteger(0);
    private volatile long start = System.currentTimeMillis();

    @MsgId(ProtoCommon.MsgId.PERFORMANCE_TEST_REQ_VALUE)
    public void performanceTest(long uid, ProtoGame.PerformanceTestReq req) {
        GamePlayer player = playerRoleService.getPlayer(uid);
        if (player == null) {
            log.error(" uid ={} not found", uid);
            return;
        }

        RoleDO roleDO = new RoleDO();
        player.toBin(roleDO);

        //for test
        //  long begin = System.currentTimeMillis();
        playerRoleService.asynUpdateTest(roleDO);
        //   long now = System.currentTimeMillis();
        //   log.info("-----performanceTest  uid ={} body={}  dis ={} ", uid, req, (now - begin));
        ProtoGame.PerformanceTestRes performanceBOyd = ProtoGame.PerformanceTestRes.newBuilder()
                .setResBody("performanceBody000000000000000000000000000000000000000000000fff00f0d0f")
                .setSendTime(req.getSendTime())
                .setSomeId(44444).build();
        peerConn.sendGateWayMsg(uid, ProtoCommon.MsgId.PERFORMANCE_TEST_RES_VALUE, performanceBOyd);


        //for timer test
        //  log.info("==================================== start timer call begin");
//        TimerBeanTest timerBeanTest = new TimerBeanTest();
//        timerBeanTest.msg = "timerTest";
        // ScheduleService.INSTANCE.executeTimerTaskWithMS(uid, playerRoleService::onTimerTest, timerBeanTest, 100L);
        int nowCount = atomicCount.incrementAndGet();
        long now = System.currentTimeMillis();
        long dis = now - start;
        if (dis >= 10000) {
            atomicCount.set(0);
            start = now;
            log.info("================ time ={}  count ={}", dis, nowCount);
        }
    }
}
