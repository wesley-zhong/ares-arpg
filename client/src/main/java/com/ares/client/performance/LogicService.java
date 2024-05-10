package com.ares.client.performance;

import com.ares.client.bean.ClientPlayer;
import com.ares.core.bean.AresPacket;
import com.game.protoGen.*;
import com.google.protobuf.ByteString;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

@Component
public class LogicService {
    public void sendPerformanceReq(ClientPlayer player) {
        ProtoGame.PerformanceTestReq helloPerformance = ProtoGame.PerformanceTestReq.newBuilder()
                .setSomeBody("hello performance")
                .setSendTime(System.currentTimeMillis())
                .setSomeId(11111).build();
        player.send(ProtoCommon.MsgId.PERFORMANCE_TEST_REQ_VALUE, helloPerformance);

    }

    public void sendDirectToTeam(ClientPlayer clientPlayer) {
        ProtoGame.DirectToWorldReq req = ProtoGame.DirectToWorldReq.newBuilder().setResBody("OOOOOOOOOOOOOOOOO").setSomeId(13223333).build();
        clientPlayer.send(ProtoCommon.MsgId.DIRECT_TO_TEAM_REQ_VALUE, req);
    }

    public void sendRpcTest(ClientPlayer clientPlayer) {
        ProtoCommon.MsgHeader rpcHeader = ProtoCommon.MsgHeader.newBuilder().setMsgId(ProtoCommon.MsgId.RPC_REQ_TEST_VALUE)
                .setReqId(991).build();
        ProtoCommon.RpcReqTest rpcTestBody = ProtoCommon.RpcReqTest.newBuilder().setSomeId(881)
                .setSomeStr("rpcTestStr").build();
        clientPlayer.send(rpcHeader, rpcTestBody);
    }

    public void createTeam(ClientPlayer clientPlayer, String teamName) {
        ProtoTeam.CreateTeamReq createTeamReq = ProtoTeam.CreateTeamReq.newBuilder().setTeamName(teamName).build();
        clientPlayer.send(ProtoCommon.MsgId.TEAM_CREATE_REQ_VALUE, createTeamReq);
    }

    public void getTeamInfo(ClientPlayer clientPlayer, long teamId) {
        ProtoCommon.MsgHeader header = ProtoCommon.MsgHeader.newBuilder()
                .setMsgId(ProtoCommon.MsgId.TEAM_DETAIL_REQ_VALUE)
                .setUid(clientPlayer.getUId())
                .setReqId(678).build();
        ProtoTeam.GetTeamDetailReq detailReq = ProtoTeam.GetTeamDetailReq.newBuilder().setTeamId(teamId).build();
        AresPacket getTeamInfo = AresPacket.create(header, detailReq);
        clientPlayer.send(getTeamInfo);
    }

    public void joinTeam(ClientPlayer clientPlayer, long teamId) {
        ProtoTeam.JoinTeamReq build = ProtoTeam.JoinTeamReq.newBuilder().setTeamId(teamId).build();
        clientPlayer.send(ProtoCommon.MsgId.TEAM_JOIN_REQ_VALUE, build);
    }

    public void exitTeam(ClientPlayer clientPlayer, long teamId) {
        ProtoTeam.ExitTeamReq.Builder builder = ProtoTeam.ExitTeamReq.newBuilder();
        clientPlayer.send(ProtoCommon.MsgId.TEAM_EXIT_REQ_VALUE);
    }

    public void getAllTeamList(ClientPlayer clientPlayer) {
        clientPlayer.send(ProtoCommon.MsgId.TEAM_LIST_REQ_VALUE);
    }

    // OLD_SCENE begin
    public void createScene(ClientPlayer clientPlayer, String sceneName, boolean resetData) {
        ProtoOldScene.OldCreateSceneReq.Builder builder = ProtoOldScene.OldCreateSceneReq.newBuilder();
        builder.setSceneName(sceneName);
        builder.setResetData(resetData);
        builder.setActorId(clientPlayer.getUId());
        clientPlayer.send(ProtoCommon.MsgId.OLD_CREATE_SCENE_REQ_VALUE, builder.build());
    }

    public void getSceneList(ClientPlayer clientPlayer) {
        ProtoOldScene.OldSceneListReq.Builder builder = ProtoOldScene.OldSceneListReq.newBuilder();
        clientPlayer.send(ProtoCommon.MsgId.OLD_SCENE_LIST_REQ_VALUE, builder.build());
    }

    public void enterScene(ClientPlayer clientPlayer, int scene_id, int profession) {
        ProtoOldScene.OldEnterSceneReq.Builder builder = ProtoOldScene.OldEnterSceneReq.newBuilder();
        builder.setSceneId(scene_id);
        builder.setProfession(profession);
        clientPlayer.send(ProtoCommon.MsgId.OLD_ENTER_SCENE_REQ_VALUE, builder.build());
    }

    public void enterDefaultScene(ClientPlayer clientPlayer, int profession) {
        ProtoOldScene.OldEnterDefaultSceneReq.Builder builder = ProtoOldScene.OldEnterDefaultSceneReq.newBuilder();
        builder.setProfession(profession);
        clientPlayer.send(ProtoCommon.MsgId.OLD_ENTER_DEFAULT_SCENE_REQ_VALUE, builder.build());
    }

    public void leaveScene(ClientPlayer clientPlayer) {
        ProtoOldScene.OldLeaveSceneReq.Builder builder = ProtoOldScene.OldLeaveSceneReq.newBuilder();
        clientPlayer.send(ProtoCommon.MsgId.OLD_LEAVE_SCENE_REQ_VALUE, builder.build());
    }

    public void sceneMessagePush(ClientPlayer clientPlayer, int messageId, boolean filterSelf, byte[] content) {
        ProtoOldScene.OldSceneMessagePush.Builder builder = ProtoOldScene.OldSceneMessagePush.newBuilder();
        builder.setMessageId(messageId);
        builder.setFilterSelf(filterSelf);
        builder.setClientMessage(ProtoOldScene.ClientMessagePackage.newBuilder()
                        .setContent(ByteString.copyFrom(content))
                .build());
        clientPlayer.send(ProtoCommon.MsgId.OLD_SCENE_MESSAGE_PUSH_VALUE, builder.build());
    }

    public void sceneMessagePush(ClientPlayer clientPlayer, int messageId, boolean filterSelf, String content) {
        ProtoOldScene.OldSceneMessagePush.Builder builder = ProtoOldScene.OldSceneMessagePush.newBuilder();
        builder.setMessageId(messageId);
        builder.setFilterSelf(filterSelf);
        builder.setClientMessage(ProtoOldScene.ClientMessagePackage.newBuilder()
                .setContent(ByteString.copyFromUtf8(content))
                .build());
        clientPlayer.send(ProtoCommon.MsgId.OLD_SCENE_MESSAGE_PUSH_VALUE, builder.build());
    }

    public void clientSceneFinishLoading(ClientPlayer clientPlayer) {
        ProtoOldScene.OldClientSceneFinishLoadingReq.Builder builder = ProtoOldScene.OldClientSceneFinishLoadingReq.newBuilder();
        clientPlayer.send(ProtoCommon.MsgId.OLD_CLIENT_SCENE_FINISH_LOADING_REQ_VALUE, builder.build());
    }

    public void resetScene(ClientPlayer clientPlayer) {
        ProtoOldScene.OldResetSceneReq.Builder builder = ProtoOldScene.OldResetSceneReq.newBuilder();
        clientPlayer.send(ProtoCommon.MsgId.OLD_RESET_SCENE_REQ_VALUE, builder.build());
    }

    public void clientSyncMovePush(ClientPlayer clientPlayer) {
        ProtoOldScene.OldClientSyncMovePush.Builder builder = ProtoOldScene.OldClientSyncMovePush.newBuilder();
        builder.setTrs(ProtoOldScene.EntityTRS.newBuilder()
                .setEntityID(clientPlayer.getUId())
                        .setSpeed(10)
                .build());
        clientPlayer.send(ProtoCommon.MsgId.OLD_CLIENT_SYNC_MOVE_PUSH_VALUE, builder.build());
    }
    // OLD_SCENE end

    // SCENE begin
    public void enterSceneReady(ClientPlayer clientPlayer, int token) {
        ProtoScene.EnterSceneReadyReq.Builder builder = ProtoScene.EnterSceneReadyReq.newBuilder();
        builder.setEnterSceneToken(token);
        clientPlayer.send(ProtoCommon.MsgId.ENTER_SCENE_READY_REQ_VALUE, builder.build());
    }

    public void sceneInitFinish(ClientPlayer clientPlayer, int token) {
        ProtoScene.SceneInitFinishReq.Builder builder = ProtoScene.SceneInitFinishReq.newBuilder();
        builder.setEnterSceneToken(token);
        clientPlayer.send(ProtoCommon.MsgId.SCENE_INIT_FINISH_REQ_VALUE, builder.build());
    }

    public void enterSceneDone(ClientPlayer clientPlayer, int token) {
        ProtoScene.EnterSceneDoneReq.Builder builder = ProtoScene.EnterSceneDoneReq.newBuilder();
        builder.setEnterSceneToken(token);
        clientPlayer.send(ProtoCommon.MsgId.ENTER_SCENE_DONE_REQ_VALUE, builder.build());
    }

    public void postEnterScene(ClientPlayer clientPlayer, int token) {
        ProtoScene.PostEnterSceneReq.Builder builder = ProtoScene.PostEnterSceneReq.newBuilder();
        builder.setEnterSceneToken(token);
        clientPlayer.send(ProtoCommon.MsgId.POST_ENTER_SCENE_REQ_VALUE, builder.build());
    }

    public void avatarMove(ClientPlayer clientPlayer) {
        float x = ThreadLocalRandom.current().nextFloat(10_0000);

        ProtoScene.SceneEntitiesMovePush.Builder builder = ProtoScene.SceneEntitiesMovePush.newBuilder();
        ProtoScene.EntityMoveInfo.Builder moveInfo = ProtoScene.EntityMoveInfo.newBuilder();
        moveInfo.setEntityId(clientPlayer.getAvatarEntityId());
        moveInfo.setMotionInfo(ProtoScene.MotionInfo.newBuilder()
                        .setPos(ProtoCommon.Vector.newBuilder()
                                .setX(x)
                                .setY(x)
                                .setZ(x)
                                .build())
                        .setState(ProtoScene.MotionState.MOTION_STANDBY)
                .build());
        builder.addEntityMoveInfoList(moveInfo.build());
        clientPlayer.send(ProtoCommon.MsgId.SCENE_ENTITIES_MOVE_PUSH_VALUE, builder.build());
    }
    // SCENE end
}
