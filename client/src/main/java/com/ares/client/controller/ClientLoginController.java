package com.ares.client.controller;

import com.ares.client.bean.ClientPlayer;
import com.ares.client.bean.PlayerMgr;
import com.ares.client.performance.LogicService;
import com.ares.client.performance.PerformanceTestService;
import com.ares.core.annotation.MsgId;
import com.ares.core.bean.AresPacket;
import com.ares.core.service.AresController;
import com.ares.core.tcp.AresTKcpContext;
import com.ares.core.utils.AresContextThreadLocal;
import com.game.protoGen.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Controller
public class ClientLoginController implements AresController {
    @Autowired
    private PerformanceTestService performanceTestService;
    @Autowired
    private LogicService logicService;
    @Value("${playerCount:1}")
    public int PLAYER_COUNT;

    private AtomicInteger loginSuccessCount = new AtomicInteger(0);

    @MsgId(ProtoMsgId.MsgId.GAME_LOGIN_NTF_VALUE)
    public void userLoginResponse(long uid1, ProtoGame.GameLoginNtf response) {
        log.info("------login response ={}", response);
        long uid = response.getUid();
        ClientPlayer clientPlayer = PlayerMgr.Instance.getClientPlayer(uid);
        AresTKcpContext aresTKcpContext = AresContextThreadLocal.get();
        aresTKcpContext.cacheObj(clientPlayer);
      // sendHeartBeatReq(aresTKcpContext);
//        logicService.sendDirectToTeam(clientPlayer);
//        logicService.sendRpcTest(clientPlayer);
     //  logicService.createTeam(clientPlayer, "team_name_1");
         // dismissTeam(clientPlayer);
      //  logicService.startGame(clientPlayer);

        //\
       logicService.joinTeam(clientPlayer, 190044720647373824L);
        //   logicService.exitTeam(clientPlayer, 1713345874421L);
        //logicService.getAllTeamList(clientPlayer);
        // logicService.getAllTeamList(clientPlayer);
        //   logicService.enterDefaultScene(clientPlayer, 1);
//        logicService.sendRpcTest(clientPlayer);
        int nowCount = loginSuccessCount.incrementAndGet();
        log.info("###### clientId ={} login success  now count={} targetCount ={}", uid, loginSuccessCount, PLAYER_COUNT);
        if (PLAYER_COUNT - nowCount < 10) {
            //  performanceTestService.sendAllPlayerPerformanceMsg();
            // performanceTestService.sendGatewayPerformanceMsg();
            //   performanceTestService.sendAllPlayerToTeamPerformanceMsg();
            //  performanceTestService.sendAoiPerformanceMsg();
        }
    }

    public void onConnectLost(AresTKcpContext channel) {
        log.info("XXXXXXXXXXXXXXX on server lost ={}", channel.getCtx());
    }

    public void sendHeartBeatReq(AresTKcpContext channel) {
        AresPacket packet = AresPacket.create(ProtoMsgId.MsgId.HEART_BEAT_PUSH_VALUE, ProtoGame.HeartBeatPush.newBuilder().build());
        channel.send(packet);
    }


    @MsgId(ProtoMsgId.MsgId.HEART_BEAT_NTF_VALUE)
    public void onHeartBeat(long uid, ProtoGame.HeartBeatNtf heartBeatNtf) {
        log.info("------- uid = {} heartbeat = {}", uid, heartBeatNtf);
    }

    @MsgId(ProtoMsgId.MsgId.PERFORMANCE_TEST_RES_VALUE)
    public void onPerformanceTest(long uid, ProtoGame.PerformanceTestRes res) {
        ClientPlayer clientPlayer = PlayerMgr.Instance.getClientPlayer(uid);
        long now = System.currentTimeMillis();
        ProtoGame.PerformanceTestReq testReq = ProtoGame.PerformanceTestReq.newBuilder()
                .setSendTime(System.currentTimeMillis())
                .setSomeBody("ooooooooooooooooooooooooooooooooooooooooooooooo")
                .setSomeId(111111L).build();
        clientPlayer.send(ProtoMsgId.MsgId.PERFORMANCE_TEST_REQ_VALUE, testReq);
        // log.info("==============  PERFORMANCE_TEST_RES_VALUE  response ={}  dis ={} ", res, now - res.getSendTime());
    }


    @MsgId(ProtoInner.InnerMsgId.INNER_GATEWAY_PERFORMANCE_RES_VALUE)
    public void onGatewayPerformanceTest(long uid, ProtoGame.PerformanceTestRes res) throws InterruptedException {
        Thread.sleep(10);
        ClientPlayer clientPlayer = PlayerMgr.Instance.getClientPlayer(uid);
        ProtoGame.PerformanceTestReq testReq = ProtoGame.PerformanceTestReq.newBuilder()
                .setSendTime(System.currentTimeMillis())
                .setSomeBody("ooooooooooooooooooooooooooooooooooooooooooooooo")
                .setSomeId(111111L).build();
        clientPlayer.send(ProtoInner.InnerMsgId.INNER_GATEWAY_PERFORMANCE_REQ_VALUE, testReq);
    }

    @MsgId(ProtoMsgId.MsgId.DIRECT_TO_TEAM_RES_VALUE)
    public void onWorldResponse(long uid, ProtoGame.DirectToWorldRes res) {
        log.info("==============  DIRECT_TO_TEAM_RES_VALUE response ={} ", res);
        ClientPlayer clientPlayer = PlayerMgr.Instance.getClientPlayer(uid);
        ProtoGame.DirectToWorldReq testReq = ProtoGame.DirectToWorldReq.newBuilder()
                .setSomeIdAdd(System.currentTimeMillis())
                .setResBody("oooooooooooooooooooooooooooooooooooo0000000000000000ooooooooooo")
                .setSomeId(111111L).build();
        clientPlayer.send(ProtoMsgId.MsgId.DIRECT_TO_TEAM_REQ_VALUE, testReq);
    }


    @MsgId(ProtoMsgId.MsgId.MAP_AOP_TEST_RES_VALUE)
    public void onAoiMapRes(long uid, ProtoCommon.AoiTestRes aoiRes) throws InterruptedException {
        log.info("----------------uid={}- onAOi ={} then sleep 3s", uid, aoiRes.getEntitiesCount());
        Thread.sleep(2000);
        ClientPlayer clientPlayer = PlayerMgr.Instance.getClientPlayer(uid);
        Random random = new Random();
        int x = random.nextInt(3007200);
        int y = random.nextInt(3007200);
        int r = 12000;
        ProtoCommon.AoiTestReq aoiTestReq = ProtoCommon.AoiTestReq.newBuilder()
                .setHeight(r)
                .setWidth(r)
                .setPosX(x)
                .setPosY(y).build();
        clientPlayer.send(ProtoMsgId.MsgId.MAP_AOI_TEST_REQ_VALUE, aoiTestReq);
    }
}
