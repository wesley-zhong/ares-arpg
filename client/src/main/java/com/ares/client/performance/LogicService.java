package com.ares.client.performance;

import com.ares.client.bean.ClientPlayer;
import com.ares.core.bean.AresPacket;
import com.game.protoGen.ProtoCommon;
import com.game.protoGen.ProtoGame;
import com.game.protoGen.ProtoScene;
import com.game.protoGen.ProtoTeam;
import com.google.protobuf.ByteString;
import org.springframework.stereotype.Component;

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

    // scene begin
    public void createScene(ClientPlayer clientPlayer, String sceneName, boolean resetData) {
        ProtoScene.CreateSceneReq.Builder builder = ProtoScene.CreateSceneReq.newBuilder();
        builder.setSceneName(sceneName);
        builder.setResetData(resetData);
        builder.setActorId(clientPlayer.getUId());
        clientPlayer.send(ProtoCommon.MsgId.CREATE_SCENE_REQ_VALUE, builder.build());
    }

    public void getSceneList(ClientPlayer clientPlayer) {
        ProtoScene.SceneListReq.Builder builder = ProtoScene.SceneListReq.newBuilder();
        clientPlayer.send(ProtoCommon.MsgId.SCENE_LIST_REQ_VALUE, builder.build());
    }

    public void enterScene(ClientPlayer clientPlayer, int scene_id, int profession) {
        ProtoScene.EnterSceneReq.Builder builder = ProtoScene.EnterSceneReq.newBuilder();
        builder.setSceneId(scene_id);
        builder.setProfession(profession);
        clientPlayer.send(ProtoCommon.MsgId.ENTER_SCENE_REQ_VALUE, builder.build());
    }

    public void enterDefaultScene(ClientPlayer clientPlayer, int profession) {
        ProtoScene.EnterDefaultSceneReq.Builder builder = ProtoScene.EnterDefaultSceneReq.newBuilder();
        builder.setProfession(profession);
        clientPlayer.send(ProtoCommon.MsgId.ENTER_DEFAULT_SCENE_REQ_VALUE, builder.build());
    }

    public void leaveScene(ClientPlayer clientPlayer) {
        ProtoScene.LeaveSceneReq.Builder builder = ProtoScene.LeaveSceneReq.newBuilder();
        clientPlayer.send(ProtoCommon.MsgId.LEAVE_SCENE_REQ_VALUE, builder.build());
    }

    public void sceneMessagePush(ClientPlayer clientPlayer, int messageId, boolean filterSelf, byte[] content) {
        ProtoScene.SceneMessagePush.Builder builder = ProtoScene.SceneMessagePush.newBuilder();
        builder.setMessageId(messageId);
        builder.setFilterSelf(filterSelf);
        builder.setClientMessage(ProtoScene.ClientMessagePackage.newBuilder()
                        .setContent(ByteString.copyFrom(content))
                .build());
        clientPlayer.send(ProtoCommon.MsgId.SCENE_MESSAGE_PUSH_VALUE, builder.build());
    }

    public void sceneMessagePush(ClientPlayer clientPlayer, int messageId, boolean filterSelf, String content) {
        ProtoScene.SceneMessagePush.Builder builder = ProtoScene.SceneMessagePush.newBuilder();
        builder.setMessageId(messageId);
        builder.setFilterSelf(filterSelf);
        builder.setClientMessage(ProtoScene.ClientMessagePackage.newBuilder()
                .setContent(ByteString.copyFromUtf8(content))
                .build());
        clientPlayer.send(ProtoCommon.MsgId.SCENE_MESSAGE_PUSH_VALUE, builder.build());
    }

    public void clientSceneFinishLoading(ClientPlayer clientPlayer) {
        ProtoScene.ClientSceneFinishLoadingReq.Builder builder = ProtoScene.ClientSceneFinishLoadingReq.newBuilder();
        clientPlayer.send(ProtoCommon.MsgId.CLIENT_SCENE_FINISH_LOADING_REQ_VALUE, builder.build());
    }

    public void resetScene(ClientPlayer clientPlayer) {
        ProtoScene.ResetSceneReq.Builder builder = ProtoScene.ResetSceneReq.newBuilder();
        clientPlayer.send(ProtoCommon.MsgId.RESET_SCENE_REQ_VALUE, builder.build());
    }

    public void clientSyncMovePush(ClientPlayer clientPlayer) {
        ProtoScene.ClientSyncMovePush.Builder builder = ProtoScene.ClientSyncMovePush.newBuilder();
        builder.setTrs(ProtoScene.EntityTRS.newBuilder()
                .setEntityID(clientPlayer.getUId())
                        .setSpeed(10)
                .build());
        clientPlayer.send(ProtoCommon.MsgId.CLIENT_SYNC_MOVE_PUSH_VALUE, builder.build());
    }
    // scene end
}
