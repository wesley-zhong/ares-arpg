package com.ares.game.service;

import com.ares.game.network.PeerConn;
import com.ares.game.scene.SceneMgr;
import com.game.protoGen.ProtoScene;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SceneService {
    @Autowired
    private SceneMgr sceneMgr;
    @Autowired
    private PeerConn peerConn;

    public ProtoScene.CreateSceneRes createSceneReq(long uid, ProtoScene.CreateSceneReq createSceneReq) {
        ProtoScene.CreateSceneRes.Builder builder = ProtoScene.CreateSceneRes.newBuilder();
        sceneMgr.createScene(createSceneReq.getSceneName(), uid, createSceneReq.getResetData(), builder);
        return builder.build();
    }

    public ProtoScene.SceneListRes sceneListReq(long uid, ProtoScene.SceneListReq sceneListReq) {
        ProtoScene.SceneListRes.Builder builder = ProtoScene.SceneListRes.newBuilder();
        List<SceneMgr.SceneInfo> sceneList = sceneMgr.getSceneList();
        for (SceneMgr.SceneInfo sceneInfo : sceneList) {
            builder.addSceneList(ProtoScene.SceneInfo.newBuilder()
                    .setSceneName(sceneInfo.getScene_name())
                    .setSceneId(sceneInfo.getScene_id())
                    .setPlayerCount(sceneInfo.getPlayer_count()));
        }
        return builder.build();
    }

    public ProtoScene.EnterSceneRes enterSceneReq(long uid, ProtoScene.EnterSceneReq enterSceneReq) {
        ProtoScene.EnterSceneRes.Builder builder = ProtoScene.EnterSceneRes.newBuilder();
        sceneMgr.enterScene(enterSceneReq.getSceneId(), uid, enterSceneReq.getProfession());
        return builder.build();
    }

    public ProtoScene.EnterDefaultSceneRes enterDefaultSceneReq(long uid, ProtoScene.EnterDefaultSceneReq enterDefaultSceneReq) {
        ProtoScene.EnterDefaultSceneRes.Builder builder = ProtoScene.EnterDefaultSceneRes.newBuilder();
        sceneMgr.enterDefaultScene(uid, enterDefaultSceneReq.getProfession());
        return builder.build();
    }

    public ProtoScene.LeaveSceneRes leaveSceneReq(long uid, ProtoScene.LeaveSceneReq leaveSceneReq) {
        ProtoScene.LeaveSceneRes.Builder builder = ProtoScene.LeaveSceneRes.newBuilder();
        sceneMgr.leaveScene(uid);
        return builder.build();
    }

    public void sceneMessagePush(long uid, ProtoScene.SceneMessagePush sceneMessagePush) {
        sceneMgr.sendSceneMessage(uid, sceneMessagePush.getClientMessage().getContent(), sceneMessagePush.getFilterSelf(), sceneMessagePush.getMessageId());
    }

    public ProtoScene.ClientSceneFinishLoadingRes clientSceneFinishLoadingReq(long uid, ProtoScene.ClientSceneFinishLoadingReq clientSceneFinishLoadingReq) {
        ProtoScene.ClientSceneFinishLoadingRes.Builder builder = ProtoScene.ClientSceneFinishLoadingRes.newBuilder();
        sceneMgr.clientFinishLoading(uid);
        return builder.build();
    }

    public ProtoScene.ResetSceneRes resetSceneReq(long uid, ProtoScene.ResetSceneReq resetSceneReq) {
        ProtoScene.ResetSceneRes.Builder builder = ProtoScene.ResetSceneRes.newBuilder();
        sceneMgr.leaveScene(uid);
        return builder.build();
    }

    public void clientSyncMovePush(long uid, ProtoScene.ClientSyncMovePush clientSyncMovePush) {
        sceneMgr.clientSyncMove(uid, clientSyncMovePush);
    }
}
