package com.ares.game.controller;

import com.ares.core.annotation.MsgId;
import com.ares.core.service.AresController;
import com.ares.game.service.OldSceneService;
import com.game.protoGen.ProtoCommon;
import com.game.protoGen.ProtoOldScene;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OldSceneController implements AresController {
    @Autowired
    private OldSceneService oldSceneService;

    @MsgId(ProtoCommon.MsgId.OLD_CREATE_SCENE_REQ_VALUE)
    public ProtoOldScene.OldCreateSceneRes createSceneReq(long uid, ProtoOldScene.OldCreateSceneReq createSceneReq) {
        return oldSceneService.createSceneReq(uid, createSceneReq);
    }

    @MsgId(ProtoCommon.MsgId.OLD_SCENE_LIST_REQ_VALUE)
    public ProtoOldScene.OldSceneListRes sceneListReq(long uid, ProtoOldScene.OldSceneListReq sceneListReq) {
        return oldSceneService.sceneListReq(uid, sceneListReq);
    }

    @MsgId(ProtoCommon.MsgId.OLD_ENTER_SCENE_REQ_VALUE)
    public ProtoOldScene.OldEnterSceneRes enterSceneReq(long uid, ProtoOldScene.OldEnterSceneReq enterSceneReq) {
        return oldSceneService.enterSceneReq(uid, enterSceneReq);
    }

    @MsgId(ProtoCommon.MsgId.OLD_ENTER_DEFAULT_SCENE_REQ_VALUE)
    public ProtoOldScene.OldEnterDefaultSceneRes enterDefaultSceneReq(long uid, ProtoOldScene.OldEnterDefaultSceneReq enterDefaultSceneReq) {
        return oldSceneService.enterDefaultSceneReq(uid, enterDefaultSceneReq);
    }

    @MsgId(ProtoCommon.MsgId.OLD_LEAVE_SCENE_REQ_VALUE)
    public ProtoOldScene.OldLeaveSceneRes leaveSceneReq(long uid, ProtoOldScene.OldLeaveSceneReq leaveSceneReq) {
        return oldSceneService.leaveSceneReq(uid, leaveSceneReq);
    }

    @MsgId(ProtoCommon.MsgId.OLD_SCENE_MESSAGE_PUSH_VALUE)
    public void sceneMessagePush(long uid, ProtoOldScene.OldSceneMessagePush sceneMessagePush) {
        oldSceneService.sceneMessagePush(uid, sceneMessagePush);
    }

    @MsgId(ProtoCommon.MsgId.OLD_CLIENT_SCENE_FINISH_LOADING_REQ_VALUE)
    public ProtoOldScene.OldClientSceneFinishLoadingRes clientSceneFinishLoadingReq(long uid, ProtoOldScene.OldClientSceneFinishLoadingReq clientSceneFinishLoadingReq) {
        return oldSceneService.clientSceneFinishLoadingReq(uid, clientSceneFinishLoadingReq);
    }

    @MsgId(ProtoCommon.MsgId.OLD_RESET_SCENE_REQ_VALUE)
    public ProtoOldScene.OldResetSceneRes resetSceneReq(long uid, ProtoOldScene.OldResetSceneReq resetSceneReq) {
        return oldSceneService.resetSceneReq(uid, resetSceneReq);
    }

    @MsgId(ProtoCommon.MsgId.OLD_CLIENT_SYNC_MOVE_PUSH_VALUE)
    public void clientSyncMovePush(long uid, ProtoOldScene.OldClientSyncMovePush clientSyncMovePush) {
        oldSceneService.clientSyncMovePush(uid, clientSyncMovePush);
    }
}
