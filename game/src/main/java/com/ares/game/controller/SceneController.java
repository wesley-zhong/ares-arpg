package com.ares.game.controller;

import com.ares.core.annotation.MsgId;
import com.ares.core.service.AresController;
import com.ares.game.service.SceneService;
import com.game.protoGen.ProtoCommon;
import com.game.protoGen.ProtoScene;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SceneController implements AresController {
    @Autowired
    private SceneService sceneService;

    @MsgId(ProtoCommon.MsgId.CREATE_SCENE_REQ_VALUE)
    public ProtoScene.CreateSceneRes createSceneReq(long uid, ProtoScene.CreateSceneReq createSceneReq) {
        return sceneService.createSceneReq(uid, createSceneReq);
    }

    @MsgId(ProtoCommon.MsgId.SCENE_LIST_REQ_VALUE)
    public ProtoScene.SceneListRes sceneListReq(long uid, ProtoScene.SceneListReq sceneListReq) {
        return sceneService.sceneListReq(uid, sceneListReq);
    }

    @MsgId(ProtoCommon.MsgId.ENTER_SCENE_REQ_VALUE)
    public ProtoScene.EnterSceneRes enterSceneReq(long uid, ProtoScene.EnterSceneReq enterSceneReq) {
        return sceneService.enterSceneReq(uid, enterSceneReq);
    }

    @MsgId(ProtoCommon.MsgId.ENTER_DEFAULT_SCENE_REQ_VALUE)
    public ProtoScene.EnterDefaultSceneRes enterDefaultSceneReq(long uid, ProtoScene.EnterDefaultSceneReq enterDefaultSceneReq) {
        return sceneService.enterDefaultSceneReq(uid, enterDefaultSceneReq);
    }

    @MsgId(ProtoCommon.MsgId.LEAVE_SCENE_REQ_VALUE)
    public ProtoScene.LeaveSceneRes leaveSceneReq(long uid, ProtoScene.LeaveSceneReq leaveSceneReq) {
        return sceneService.leaveSceneReq(uid, leaveSceneReq);
    }

    @MsgId(ProtoCommon.MsgId.SCENE_MESSAGE_PUSH_VALUE)
    public void sceneMessagePush(long uid, ProtoScene.SceneMessagePush sceneMessagePush) {
        sceneService.sceneMessagePush(uid, sceneMessagePush);
    }

    @MsgId(ProtoCommon.MsgId.CLIENT_SCENE_FINISH_LOADING_REQ_VALUE)
    public ProtoScene.ClientSceneFinishLoadingRes clientSceneFinishLoadingReq(long uid, ProtoScene.ClientSceneFinishLoadingReq clientSceneFinishLoadingReq) {
        return sceneService.clientSceneFinishLoadingReq(uid, clientSceneFinishLoadingReq);
    }

    @MsgId(ProtoCommon.MsgId.RESET_SCENE_REQ_VALUE)
    public ProtoScene.ResetSceneRes resetSceneReq(long uid, ProtoScene.ResetSceneReq resetSceneReq) {
        return sceneService.resetSceneReq(uid, resetSceneReq);
    }

    @MsgId(ProtoCommon.MsgId.CLIENT_SYNC_MOVE_PUSH_VALUE)
    public void clientSyncMovePush(long uid, ProtoScene.ClientSyncMovePush clientSyncMovePush) {
        sceneService.clientSyncMovePush(uid, clientSyncMovePush);
    }
}
