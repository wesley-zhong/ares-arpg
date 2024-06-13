package com.ares.client.performance;

import com.ares.client.bean.ClientPlayer;
import com.ares.core.bean.AresPacket;
import com.game.protoGen.*;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

@Component
public class LogicService {
    public void sendPerformanceReq(ClientPlayer player) {
        ProtoGame.PerformanceTestReq helloPerformance = ProtoGame.PerformanceTestReq.newBuilder()
                .setSomeBody("hello performance")
                .setSendTime(System.currentTimeMillis())
                .setSomeId(11111).build();
        player.send(ProtoMsgId.MsgId.PERFORMANCE_TEST_REQ_VALUE, helloPerformance);

    }

    public void sendDirectToTeam(ClientPlayer clientPlayer) {
        ProtoGame.DirectToWorldReq req = ProtoGame.DirectToWorldReq.newBuilder().setResBody("OOOOOOOOOOOOOOOOO").setSomeId(13223333).build();
        clientPlayer.send(ProtoMsgId.MsgId.DIRECT_TO_TEAM_REQ_VALUE, req);
    }

    public void sendRpcTest(ClientPlayer clientPlayer) {
        ProtoCommon.MsgHeader rpcHeader = ProtoCommon.MsgHeader.newBuilder().setMsgId(ProtoMsgId.MsgId.RPC_REQ_TEST_VALUE)
                .setReqId(991).build();
        ProtoCommon.RpcReqTest rpcTestBody = ProtoCommon.RpcReqTest.newBuilder().setSomeId(881)
                .setSomeStr("rpcTestStr").build();
        clientPlayer.send(rpcHeader, rpcTestBody);
    }

    public void createTeam(ClientPlayer clientPlayer, String teamName) {
        ProtoTeam.CreateTeamPush createTeamReq = ProtoTeam.CreateTeamPush.newBuilder().setTeamName(teamName).build();
        clientPlayer.send(ProtoMsgId.MsgId.TEAM_CREATE_PUSH_VALUE, createTeamReq);
    }

    public void getTeamInfo(ClientPlayer clientPlayer, long teamId) {
        ProtoCommon.MsgHeader header = ProtoCommon.MsgHeader.newBuilder()
                .setMsgId(ProtoMsgId.MsgId.TEAM_DETAIL_PUSH_VALUE)
                .setUid(clientPlayer.getUId())
                 .build();
        ProtoTeam.GetTeamDetailPush detailPush = ProtoTeam.GetTeamDetailPush.newBuilder().setTeamId(teamId).build();
        AresPacket getTeamInfo = AresPacket.create(header, detailPush);
        clientPlayer.send(getTeamInfo);
    }

    public void startGame(ClientPlayer clientPlayer){
        clientPlayer.send(ProtoMsgId.MsgId.TEAM_START_GAME_PUSH_VALUE);
    }

    public void joinTeam(ClientPlayer clientPlayer, long teamId) {
        ProtoTeam.JoinTeamPush build = ProtoTeam.JoinTeamPush.newBuilder().setTeamId(teamId).build();
        clientPlayer.send(ProtoMsgId.MsgId.TEAM_JOIN_PUSH_VALUE, build);
    }

    public void exitTeam(ClientPlayer clientPlayer, long teamId) {
        ProtoTeam.ExitTeamPush.Builder builder = ProtoTeam.ExitTeamPush.newBuilder();
        clientPlayer.send(ProtoMsgId.MsgId.TEAM_EXIT_PUSH_VALUE);
    }

    public void getAllTeamList(ClientPlayer clientPlayer) {
        clientPlayer.send(ProtoMsgId.MsgId.TEAM_LIST_PUSH_VALUE);
    }

    // SCENE begin
    public void  enterSceneReady(ClientPlayer clientPlayer, int token) {
        ProtoScene.EnterSceneReadyReq.Builder builder = ProtoScene.EnterSceneReadyReq.newBuilder();
        builder.setEnterSceneToken(token);
        clientPlayer.send(ProtoMsgId.MsgId.ENTER_SCENE_READY_REQ_VALUE, builder.build());
    }

    public void sceneInitFinish(ClientPlayer clientPlayer, int token) {
        ProtoScene.SceneInitFinishReq.Builder builder = ProtoScene.SceneInitFinishReq.newBuilder();
        builder.setEnterSceneToken(token);
        clientPlayer.send(ProtoMsgId.MsgId.SCENE_INIT_FINISH_REQ_VALUE, builder.build());
    }

    public void enterSceneDone(ClientPlayer clientPlayer, int token) {
        ProtoScene.EnterSceneDoneReq.Builder builder = ProtoScene.EnterSceneDoneReq.newBuilder();
        builder.setEnterSceneToken(token);
        clientPlayer.send(ProtoMsgId.MsgId.ENTER_SCENE_DONE_REQ_VALUE, builder.build());
    }

    public void postEnterScene(ClientPlayer clientPlayer, int token) {
        ProtoScene.PostEnterSceneReq.Builder builder = ProtoScene.PostEnterSceneReq.newBuilder();
        builder.setEnterSceneToken(token);
        clientPlayer.send(ProtoMsgId.MsgId.POST_ENTER_SCENE_REQ_VALUE, builder.build());
    }

    public void avatarMove(ClientPlayer clientPlayer) {
        float x = ThreadLocalRandom.current().nextFloat(10_0000);

        ProtoScene.SceneEntitiesMovePush.Builder builder = ProtoScene.SceneEntitiesMovePush.newBuilder();
        ProtoScene.EntityMoveInfo.Builder moveInfo = ProtoScene.EntityMoveInfo.newBuilder();
        moveInfo.setEntityId(clientPlayer.getAvatarEntityId());
        moveInfo.setMotionInfo(ProtoScene.MotionInfo.newBuilder()
                        .setPos(ProtoCommon.PbVector.newBuilder()
                                .setX(x)
                                .setY(x)
                                .setZ(x)
                                .build())
                        .setState(ProtoScene.MotionState.MOTION_STANDBY)
                .build());
        builder.addEntityMoveInfoList(moveInfo.build());
     //   clientPlayer.send(ProtoMsgId.MsgId.SCENE_ENTITIES_MOVE_PUSH_VALUE, builder.build());
    }
    // SCENE end
}
