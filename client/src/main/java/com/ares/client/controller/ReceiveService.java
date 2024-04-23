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

    private int loginSuccessCount;

    @MsgId(ProtoCommon.MsgId.GAME_LOGIN_RES_VALUE)
    public void userLoginResponse(ProtoGame.GameLoginRes response) {
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
//        logicService.enterDefaultScene(clientPlayer, 1);
//        logicService.sendRpcTest(clientPlayer);
        loginSuccessCount++;
        log.info("###### clientId ={} login success  now count={}", uid, loginSuccessCount);
        if (PLAYER_COUNT == loginSuccessCount) {
           performanceTestService.sendAllPlayerPerformanceMsg();
           //  performanceTestService.sendGatewayPerformanceMsg();
        }
    }


    @MsgId(ProtoCommon.MsgId.PERFORMANCE_TEST_RES_VALUE)
    public void onPerformanceTest(ProtoGame.PerformanceTestRes res) {
        AresTKcpContext aresTKcpContext = AresContextThreadLocal.get();
        ClientPlayer clientPlayer = (ClientPlayer) aresTKcpContext.getCacheObj();
        long now = System.currentTimeMillis();
        ProtoGame.PerformanceTestReq testReq = ProtoGame.PerformanceTestReq.newBuilder()
                .setSendTime(System.currentTimeMillis())
                .setSomeBody("ooooooooooooooooooooooooooooooooooooooooooooooo")
                .setSomeId(111111L).build();
        clientPlayer.send(ProtoCommon.MsgId.PERFORMANCE_TEST_REQ_VALUE, testReq);
        // log.info("==============  PERFORMANCE_TEST_RES_VALUE  response ={}  dis ={} ", res, now - res.getSendTime());
    }

    @MsgId(ProtoInner.InnerMsgId.INNER_GATEWAY_PERFORMANCE_RES_VALUE)
    public void onGatewayPerformanceTest(ProtoGame.PerformanceTestRes res) {
        AresTKcpContext aresTKcpContext = AresContextThreadLocal.get();
        ClientPlayer clientPlayer = (ClientPlayer) aresTKcpContext.getCacheObj();
        ProtoGame.PerformanceTestReq testReq = ProtoGame.PerformanceTestReq.newBuilder()
                .setSendTime(System.currentTimeMillis())
                .setSomeBody("ooooooooooooooooooooooooooooooooooooooooooooooo")
                .setSomeId(111111L).build();
        clientPlayer.send(ProtoInner.InnerMsgId.INNER_GATEWAY_PERFORMANCE_REQ_VALUE, testReq);
    }

    @MsgId(ProtoCommon.MsgId.DIRECT_TO_TEAM_RES_VALUE)
    public void onWorldResponse(ProtoGame.DirectToWorldRes res) {
        log.info("==============  DIRECT_TO_TEAM_RES_VALUE response ={} ", res);
    }

    @MsgId(ProtoCommon.MsgId.TEAM_CREATE_RES_VALUE)
    public void onTeamCreateResponse(ProtoTeam.TeamInfo teamInfo) {
        AresTKcpContext aresTKcpContext = AresContextThreadLocal.get();
        ClientPlayer clientPlayer = (ClientPlayer) aresTKcpContext.getCacheObj();
        log.info("TEAM_CREATE_RES_VALUE ={}", teamInfo);
        logicService.getTeamInfo(clientPlayer, teamInfo.getTeamId());
    }

    @MsgId(ProtoCommon.MsgId.TEAM_DETAIL_RES_VALUE)
    public void onTeamDetailResponse(ProtoTeam.TeamInfo teamDetailRes) {
        log.info("---------- teamDetail ={}", teamDetailRes);
    }

    @MsgId(ProtoCommon.MsgId.TEAM_JOIN_RES_VALUE)
    public void onTeamJoinRes(ProtoTeam.TeamInfo joinTeamRes) {
        log.info("======== team joinRes = {}", joinTeamRes);
    }

    @MsgId(ProtoCommon.MsgId.TEAM_LIST_RES_VALUE)
    public void onTeamListRes(ProtoTeam.GetTeamListRes res) {
        log.info("PPPPPPPp team list ={}", res);
    }

    // SCENE BEGIN
    @MsgId(ProtoCommon.MsgId.SCENE_LIST_RES_VALUE)
    public void onSceneListRes(ProtoScene.SceneListRes res) {
        log.info("---------- SceneListRes ={}", res);
    }

    @MsgId(ProtoCommon.MsgId.CREATE_SCENE_RES_VALUE)
    public void onCreateSceneRes(ProtoScene.CreateSceneRes res) {
        log.info("---------- CreateSceneRes ={}", res);

        AresTKcpContext aresTKcpContext = AresContextThreadLocal.get();
        ClientPlayer clientPlayer = (ClientPlayer) aresTKcpContext.getCacheObj();
        logicService.getSceneList(clientPlayer);
        logicService.enterScene(clientPlayer, res.getSceneId(), 2);
    }

    @MsgId(ProtoCommon.MsgId.CREATE_SCENE_FINISH_NTF_VALUE)
    public void onCreateSceneFinishNtf(ProtoScene.CreateSceneFinishNtf res) {
        log.info("---------- CreateSceneFinishNtf ={}", res);
    }

    @MsgId(ProtoCommon.MsgId.ENTER_SCENE_RES_VALUE)
    public void onEnterSceneRes(ProtoScene.EnterSceneRes res) {
        log.info("---------- EnterSceneRes ={}", res);

        AresTKcpContext aresTKcpContext = AresContextThreadLocal.get();
        ClientPlayer clientPlayer = (ClientPlayer) aresTKcpContext.getCacheObj();
        logicService.clientSceneFinishLoading(clientPlayer);
        logicService.sceneMessagePush(clientPlayer, 10, false, new byte[]{0, 1, 2});
        logicService.sceneMessagePush(clientPlayer, 11, false, "hello world");
        logicService.clientSyncMovePush(clientPlayer);
        logicService.resetScene(clientPlayer);
    }

    @MsgId(ProtoCommon.MsgId.ENTER_DEFAULT_SCENE_RES_VALUE)
    public void onEnterDefaultSceneRes(ProtoScene.EnterDefaultSceneRes res) {
        log.info("---------- EnterDefaultSceneRes ={}", res);

        AresTKcpContext aresTKcpContext = AresContextThreadLocal.get();
        ClientPlayer clientPlayer = (ClientPlayer) aresTKcpContext.getCacheObj();
        logicService.leaveScene(clientPlayer);
    }

    @MsgId(ProtoCommon.MsgId.LEAVE_SCENE_RES_VALUE)
    public void onLeaveSceneRes(ProtoScene.LeaveSceneRes res) {
        log.info("---------- onEnterSceneRes ={}", res);

        AresTKcpContext aresTKcpContext = AresContextThreadLocal.get();
        ClientPlayer clientPlayer = (ClientPlayer) aresTKcpContext.getCacheObj();
        logicService.createScene(clientPlayer, "test_scene", true);
    }

    @MsgId(ProtoCommon.MsgId.PLAYER_ENTER_SCENE_NTF_VALUE)
    public void onPlayerEnterSceneNtf(ProtoScene.PlayerEnterSceneNtf res) {
        log.info("---------- PlayerEnterSceneNtf ={}", res);
    }

    @MsgId(ProtoCommon.MsgId.PLAYER_LEAVE_SCENE_NTF_VALUE)
    public void onPlayerLeaveSceneNtf(ProtoScene.PlayerLeaveSceneNtf res) {
        log.info("---------- PlayerLeaveSceneNtf ={}", res);
    }

    @MsgId(ProtoCommon.MsgId.SYNC_SCENE_MESSAGE_NTF_VALUE)
    public void onSyncSceneMessageNtf(ProtoScene.SyncSceneMessageNtf res) {
        log.info("---------- SyncSceneMessageNtf ={}", res);
    }

    @MsgId(ProtoCommon.MsgId.SERVER_SCENE_FINISH_LOADING_NTF_VALUE)
    public void onServerSceneFinishLoadingNtf(ProtoScene.ServerSceneFinishLoadingNtf res) {
        log.info("---------- ServerSceneFinishLoadingNtf ={}", res);
    }

    @MsgId(ProtoCommon.MsgId.CLIENT_SCENE_FINISH_LOADING_RES_VALUE)
    public void onClientSceneFinishLoadingRes(ProtoScene.ClientSceneFinishLoadingRes res) {
        log.info("---------- ClientSceneFinishLoadingRes ={}", res);
    }

    @MsgId(ProtoCommon.MsgId.RESET_SCENE_RES_VALUE)
    public void onResetSceneRes(ProtoScene.ResetSceneRes res) {
        log.info("---------- ResetSceneRes ={}", res);
    }

    @MsgId(ProtoCommon.MsgId.PLAYER_MOVE_TO_NTF_VALUE)
    public void onPlayerMoveToNtf(ProtoScene.PlayerMoveToNtf res) {
        log.info("---------- PlayerMoveToNtf ={}", res);
    }
    // SCENE END
}
