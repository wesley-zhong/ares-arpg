package com.ares.game.service;

import com.ares.game.scene.old.OldSceneMgr;
import com.game.protoGen.ProtoOldScene;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OldSceneService {
    @Autowired
    private OldSceneMgr oldSceneMgr;

    public ProtoOldScene.OldCreateSceneRes createSceneReq(long uid, ProtoOldScene.OldCreateSceneReq createSceneReq) {
        ProtoOldScene.OldCreateSceneRes.Builder builder = ProtoOldScene.OldCreateSceneRes.newBuilder();
        oldSceneMgr.createScene(createSceneReq.getSceneName(), uid, createSceneReq.getResetData(), builder);
        return builder.build();
    }

    public ProtoOldScene.OldSceneListRes sceneListReq(long uid, ProtoOldScene.OldSceneListReq sceneListReq) {
        ProtoOldScene.OldSceneListRes.Builder builder = ProtoOldScene.OldSceneListRes.newBuilder();
        List<OldSceneMgr.SceneInfo> sceneList = oldSceneMgr.getSceneList();
        for (OldSceneMgr.SceneInfo sceneInfo : sceneList) {
            builder.addSceneList(ProtoOldScene.SceneInfo.newBuilder()
                    .setSceneName(sceneInfo.getScene_name())
                    .setSceneId(sceneInfo.getScene_id())
                    .setPlayerCount(sceneInfo.getPlayer_count()));
        }
        return builder.build();
    }

    public ProtoOldScene.OldEnterSceneRes enterSceneReq(long uid, ProtoOldScene.OldEnterSceneReq enterSceneReq) {
        ProtoOldScene.OldEnterSceneRes.Builder builder = ProtoOldScene.OldEnterSceneRes.newBuilder();
        oldSceneMgr.enterScene(enterSceneReq.getSceneId(), uid, enterSceneReq.getProfession());
        return builder.build();
    }

    public ProtoOldScene.OldEnterDefaultSceneRes enterDefaultSceneReq(long uid, ProtoOldScene.OldEnterDefaultSceneReq enterDefaultSceneReq) {
        ProtoOldScene.OldEnterDefaultSceneRes.Builder builder = ProtoOldScene.OldEnterDefaultSceneRes.newBuilder();
        oldSceneMgr.enterDefaultScene(uid, enterDefaultSceneReq.getProfession());
        return builder.build();
    }

    public ProtoOldScene.OldLeaveSceneRes leaveSceneReq(long uid, ProtoOldScene.OldLeaveSceneReq leaveSceneReq) {
        ProtoOldScene.OldLeaveSceneRes.Builder builder = ProtoOldScene.OldLeaveSceneRes.newBuilder();
        oldSceneMgr.leaveScene(uid);
        return builder.build();
    }

    public void sceneMessagePush(long uid, ProtoOldScene.OldSceneMessagePush sceneMessagePush) {
        oldSceneMgr.sendSceneMessage(uid, sceneMessagePush.getClientMessage().getContent(), sceneMessagePush.getFilterSelf(), sceneMessagePush.getMessageId());
    }

    public ProtoOldScene.OldClientSceneFinishLoadingRes clientSceneFinishLoadingReq(long uid, ProtoOldScene.OldClientSceneFinishLoadingReq clientSceneFinishLoadingReq) {
        ProtoOldScene.OldClientSceneFinishLoadingRes.Builder builder = ProtoOldScene.OldClientSceneFinishLoadingRes.newBuilder();
        oldSceneMgr.clientFinishLoading(uid);
        return builder.build();
    }

    public ProtoOldScene.OldResetSceneRes resetSceneReq(long uid, ProtoOldScene.OldResetSceneReq resetSceneReq) {
        ProtoOldScene.OldResetSceneRes.Builder builder = ProtoOldScene.OldResetSceneRes.newBuilder();
        oldSceneMgr.leaveScene(uid);
        return builder.build();
    }

    public void clientSyncMovePush(long uid, ProtoOldScene.OldClientSyncMovePush clientSyncMovePush) {
        oldSceneMgr.clientSyncMove(uid, clientSyncMovePush);
    }
}
