package com.ares.game.controller;

import com.ares.core.annotation.MsgId;
import com.ares.game.service.SceneService;
import com.game.protoGen.ProtoCommon;
import com.game.protoGen.ProtoScene;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SceneController {
    @Autowired
    private SceneService sceneService;

    @MsgId(ProtoCommon.MsgId.ENTER_SCENE_READY_REQ_VALUE)
    public ProtoScene.EnterSceneReadyRes enterSceneReady(long uid, ProtoScene.EnterSceneReadyReq req) {
        return sceneService.enterSceneReady(uid, req);
    }

    @MsgId(ProtoCommon.MsgId.SCENE_INIT_FINISH_REQ_VALUE)
    public ProtoScene.SceneInitFinishRes sceneInitFinish(long uid, ProtoScene.SceneInitFinishReq req) {
        return sceneService.sceneInitFinish(uid, req);
    }

    @MsgId(ProtoCommon.MsgId.ENTER_SCENE_DONE_REQ_VALUE)
    public ProtoScene.EnterSceneDoneRes enterSceneDone(long uid, ProtoScene.EnterSceneDoneReq req) {
        return sceneService.enterSceneDone(uid, req);
    }

    @MsgId(ProtoCommon.MsgId.POST_ENTER_SCENE_REQ_VALUE)
    public ProtoScene.PostEnterSceneRes postEnterScene(long uid, ProtoScene.PostEnterSceneReq req) {
        return sceneService.postEnterScene(uid, req);
    }

    @MsgId(ProtoCommon.MsgId.LEAVE_SCENE_REQ_VALUE)
    public ProtoScene.LeaveSceneRes leaveScene(long uid, ProtoScene.LeaveSceneReq req) {
        return sceneService.leaveScene(uid, req);
    }

    @MsgId(ProtoCommon.MsgId.SCENE_TRANS_TO_POINT_REQ_VALUE)
    public ProtoScene.SceneTransToPointRes sceneTransToPoint(long uid, ProtoScene.SceneTransToPointReq req) {
        return sceneService.sceneTransToPoint(uid, req);
    }

    @MsgId(ProtoCommon.MsgId.SCENE_CREATE_ENTITY_REQ_VALUE)
    public ProtoScene.SceneCreateEntityRes sceneCreateEntity(long uid, ProtoScene.SceneCreateEntityReq req) {
        return sceneService.sceneCreateEntity(uid, req);
    }

    @MsgId(ProtoCommon.MsgId.SCENE_DESTROY_ENTITY_REQ_VALUE)
    public ProtoScene.SceneDestroyEntityRes sceneDestroyEntity(long uid, ProtoScene.SceneDestroyEntityReq req) {
        return sceneService.sceneDestroyEntity(uid, req);
    }

    @MsgId(ProtoCommon.MsgId.ENTITY_FORCE_SYNC_REQ_VALUE)
    public ProtoScene.EntityForceSyncRes entityForceSync(long uid, ProtoScene.EntityForceSyncReq req) {
        return sceneService.entityForceSync(uid, req);
    }

    @MsgId(ProtoCommon.MsgId.CLIENT_TRANSMIT_REQ_VALUE)
    public ProtoScene.ClientTransmitRes clientTransmit(long uid, ProtoScene.ClientTransmitReq req) {
        return sceneService.clientTransmit(uid, req);
    }

    @MsgId(ProtoCommon.MsgId.PERSONAL_SCENE_JUMP_REQ_VALUE)
    public ProtoScene.PersonalSceneJumpRes personalSceneJump(long uid, ProtoScene.PersonalSceneJumpReq req) {
        return sceneService.personalSceneJump(uid, req);
    }

    @MsgId(ProtoCommon.MsgId.JOIN_PLAYER_SCENE_REQ_VALUE)
    public ProtoScene.JoinPlayerSceneRes joinPlayerScene(long uid, ProtoScene.JoinPlayerSceneReq req) {
        return sceneService.joinPlayerScene(uid, req);
    }

    @MsgId(ProtoCommon.MsgId.SCENE_KICK_PLAYER_REQ_VALUE)
    public ProtoScene.SceneKickPlayerRes sceneKickPlayer(long uid, ProtoScene.SceneKickPlayerReq req) {
        return sceneService.sceneKickPlayer(uid, req);
    }

    @MsgId(ProtoCommon.MsgId.BACK_MY_WORLD_REQ_VALUE)
    public ProtoScene.BackMyWorldRes backMyWorld(long uid, ProtoScene.BackMyWorldReq req) {
        return sceneService.backMyWorld(uid, req);
    }

    @MsgId(ProtoCommon.MsgId.SCENE_ENTITIES_MOVE_REQ_VALUE)
    public ProtoScene.SceneEntitiesMoveRes sceneEntitiesMove(long uid, ProtoScene.SceneEntitiesMoveReq req) {
        return sceneService.sceneEntitiesMove(uid, req);
    }
}
