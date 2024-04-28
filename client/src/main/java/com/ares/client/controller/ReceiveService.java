package com.ares.client.controller;

import com.ares.client.Client;
import com.ares.client.bean.ClientPlayer;
import com.ares.client.bean.PlayerMgr;
import com.ares.client.performance.LogicService;
import com.ares.client.performance.PerformanceTestService;
import com.ares.core.annotation.MsgId;
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
public class ReceiveService implements AresController {
    @Autowired
    private Client client;
    @Autowired
    private PerformanceTestService performanceTestService;
    @Autowired
    private LogicService logicService;
    @Value("${playerCount:1}")
    public int PLAYER_COUNT;

    private AtomicInteger loginSuccessCount = new AtomicInteger(0);

    @MsgId(ProtoCommon.MsgId.GAME_LOGIN_RES_VALUE)
    public void userLoginResponse(long uid1, ProtoGame.GameLoginRes response) {
        log.info("------login response ={}", response);
        long uid = response.getUid();
        ClientPlayer clientPlayer = PlayerMgr.Instance.getClientPlayer(uid);

        AresTKcpContext aresTKcpContext = AresContextThreadLocal.get();
        aresTKcpContext.cacheObj(clientPlayer);
//        logicService.sendDirectToTeam(clientPlayer);
//        logicService.sendRpcTest(clientPlayer);
//        logicService.createTeam(clientPlayer,"team_name");

        //\
        // logicService.joinTeam(clientPlayer, 1713345874421L);
        //   logicService.exitTeam(clientPlayer, 1713345874421L);
//        logicService.getAllTeamList(clientPlayer);
//        logicService.getSceneList(clientPlayer);
      //   logicService.enterDefaultScene(clientPlayer, 1);
//        logicService.sendRpcTest(clientPlayer);
        int nowCount = loginSuccessCount.incrementAndGet();
        log.info("###### clientId ={} login success  now count={} targetCount ={}", uid, loginSuccessCount,PLAYER_COUNT);
//        if (PLAYER_COUNT - nowCount < 10) {
//            //  performanceTestService.sendAllPlayerPerformanceMsg();
//             // performanceTestService.sendGatewayPerformanceMsg();
//            // performanceTestService.sendAllPlayerToTeamPerformanceMsg();
//            performanceTestService.sendAoiPerformanceMsg();
//        }
    }


    @MsgId(ProtoCommon.MsgId.PERFORMANCE_TEST_RES_VALUE)
    public void onPerformanceTest(long uid, ProtoGame.PerformanceTestRes res) {
        ClientPlayer clientPlayer = PlayerMgr.Instance.getClientPlayer(uid);
        long now = System.currentTimeMillis();
        ProtoGame.PerformanceTestReq testReq = ProtoGame.PerformanceTestReq.newBuilder()
                .setSendTime(System.currentTimeMillis())
                .setSomeBody("ooooooooooooooooooooooooooooooooooooooooooooooo")
                .setSomeId(111111L).build();
        clientPlayer.send(ProtoCommon.MsgId.PERFORMANCE_TEST_REQ_VALUE, testReq);
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

    @MsgId(ProtoCommon.MsgId.DIRECT_TO_TEAM_RES_VALUE)
    public void onWorldResponse(long uid, ProtoGame.DirectToWorldRes res) {
        log.info("==============  DIRECT_TO_TEAM_RES_VALUE response ={} ", res);
        ClientPlayer clientPlayer = PlayerMgr.Instance.getClientPlayer(uid);
        ProtoGame.DirectToWorldReq testReq = ProtoGame.DirectToWorldReq.newBuilder()
                .setSomeIdAdd(System.currentTimeMillis())
                .setResBody("oooooooooooooooooooooooooooooooooooo0000000000000000ooooooooooo")
                .setSomeId(111111L).build();
        clientPlayer.send(ProtoCommon.MsgId.DIRECT_TO_TEAM_REQ_VALUE, testReq);
    }

    @MsgId(ProtoCommon.MsgId.TEAM_CREATE_RES_VALUE)
    public void onTeamCreateResponse(long uid, ProtoTeam.TeamInfo teamInfo) {
        ClientPlayer clientPlayer = PlayerMgr.Instance.getClientPlayer(uid);
        log.info("TEAM_CREATE_RES_VALUE ={}", teamInfo);
        logicService.getTeamInfo(clientPlayer, teamInfo.getTeamId());
    }

    @MsgId(ProtoCommon.MsgId.TEAM_DETAIL_RES_VALUE)
    public void onTeamDetailResponse(long uid, ProtoTeam.TeamInfo teamDetailRes) {
        log.info("---------- teamDetail ={}", teamDetailRes);
    }

    @MsgId(ProtoCommon.MsgId.TEAM_JOIN_RES_VALUE)
    public void onTeamJoinRes(long uid, ProtoTeam.TeamInfo joinTeamRes) {
        log.info("======== team joinRes = {}", joinTeamRes);
    }

    @MsgId(ProtoCommon.MsgId.TEAM_LIST_RES_VALUE)
    public void onTeamListRes(long uid, ProtoTeam.GetTeamListRes res) {
        log.info("PPPPPPPp team list ={}", res);
    }

    // SCENE BEGIN
    @MsgId(ProtoCommon.MsgId.OLD_SCENE_LIST_RES_VALUE)
    public void onSceneListRes(long uid, ProtoOldScene.OldSceneListRes res) {
        log.info("---------- SceneListRes ={}", res);
    }

    @MsgId(ProtoCommon.MsgId.OLD_CREATE_SCENE_RES_VALUE)
    public void onCreateSceneRes(long uid, ProtoOldScene.OldCreateSceneRes res) {
        log.info("---------- CreateSceneRes ={}", res);
        ClientPlayer clientPlayer = PlayerMgr.Instance.getClientPlayer(uid);
        logicService.getSceneList(clientPlayer);
        logicService.enterScene(clientPlayer, res.getSceneId(), 2);
    }

    @MsgId(ProtoCommon.MsgId.OLD_CREATE_SCENE_FINISH_NTF_VALUE)
    public void onCreateSceneFinishNtf(long uid, ProtoOldScene.OldCreateSceneFinishNtf res) {
        log.info("---------- CreateSceneFinishNtf ={}", res);
    }

    @MsgId(ProtoCommon.MsgId.OLD_ENTER_SCENE_RES_VALUE)
    public void onEnterSceneRes(long uid, ProtoOldScene.OldEnterSceneRes res) {
        log.info("---------- EnterSceneRes ={}", res);
        ClientPlayer clientPlayer = PlayerMgr.Instance.getClientPlayer(uid);
        logicService.clientSceneFinishLoading(clientPlayer);
        logicService.sceneMessagePush(clientPlayer, 10, false, new byte[]{0, 1, 2});
        logicService.sceneMessagePush(clientPlayer, 11, false, "hello world");
        logicService.clientSyncMovePush(clientPlayer);
        logicService.resetScene(clientPlayer);
    }

    @MsgId(ProtoCommon.MsgId.OLD_ENTER_DEFAULT_SCENE_RES_VALUE)
    public void onEnterDefaultSceneRes(long uid, ProtoOldScene.OldEnterDefaultSceneRes res) {
        log.info("---------- EnterDefaultSceneRes ={}", res);
        ClientPlayer clientPlayer = PlayerMgr.Instance.getClientPlayer(uid);
        logicService.leaveScene(clientPlayer);
    }

    @MsgId(ProtoCommon.MsgId.OLD_LEAVE_SCENE_RES_VALUE)
    public void onLeaveSceneRes(long uid, ProtoOldScene.OldLeaveSceneRes res) {
        log.info("---------- onEnterSceneRes ={}", res);
        ClientPlayer clientPlayer = PlayerMgr.Instance.getClientPlayer(uid);
        logicService.createScene(clientPlayer, "test_scene", true);
    }

    @MsgId(ProtoCommon.MsgId.OLD_PLAYER_ENTER_SCENE_NTF_VALUE)
    public void onPlayerEnterSceneNtf(long uid, ProtoOldScene.OldPlayerEnterSceneNtf res) {
        log.info("---------- PlayerEnterSceneNtf ={}", res);
    }

    @MsgId(ProtoCommon.MsgId.OLD_PLAYER_LEAVE_SCENE_NTF_VALUE)
    public void onPlayerLeaveSceneNtf(long uid, ProtoOldScene.OldPlayerLeaveSceneNtf res) {
        log.info("---------- PlayerLeaveSceneNtf ={}", res);
    }

    @MsgId(ProtoCommon.MsgId.OLD_SYNC_SCENE_MESSAGE_NTF_VALUE)
    public void onSyncSceneMessageNtf(long uid, ProtoOldScene.OldSyncSceneMessageNtf res) {
        log.info("---------- SyncSceneMessageNtf ={}", res);
    }

    @MsgId(ProtoCommon.MsgId.OLD_SERVER_SCENE_FINISH_LOADING_NTF_VALUE)
    public void onServerSceneFinishLoadingNtf(long uid, ProtoOldScene.OldServerSceneFinishLoadingNtf res) {
        log.info("---------- ServerSceneFinishLoadingNtf ={}", res);
    }

    @MsgId(ProtoCommon.MsgId.OLD_CLIENT_SCENE_FINISH_LOADING_RES_VALUE)
    public void onClientSceneFinishLoadingRes(long uid, ProtoOldScene.OldClientSceneFinishLoadingRes res) {
        log.info("---------- ClientSceneFinishLoadingRes ={}", res);
    }

    @MsgId(ProtoCommon.MsgId.OLD_RESET_SCENE_RES_VALUE)
    public void onResetSceneRes(long uid, ProtoOldScene.OldResetSceneRes res) {
        log.info("---------- ResetSceneRes ={}", res);
    }

    @MsgId(ProtoCommon.MsgId.OLD_PLAYER_MOVE_TO_NTF_VALUE)
    public void onPlayerMoveToNtf(long uid, ProtoOldScene.OldPlayerMoveToNtf res) {
        log.info("---------- PlayerMoveToNtf ={}", res);
    }
    // SCENE END

    @MsgId(ProtoCommon.MsgId.MAP_AOP_TEST_RES_VALUE)
    public void onAoiMapRes(long uid, ProtoCommon.AoiTestRes aoiRes) throws InterruptedException {
        log.info("----------------uid={}- onAOi ={} then sleep 3s",uid, aoiRes.getEntitiesCount());
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
        clientPlayer.send(ProtoCommon.MsgId.MAP_AOI_TEST_REQ_VALUE, aoiTestReq);
    }
}
