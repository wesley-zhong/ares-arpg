package com.ares.game.service;

import com.ares.game.player.Player;
import com.game.protoGen.ProtoScene;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SceneService {
    @Autowired
    PlayerRoleService playerRoleService;

    public ProtoScene.EnterSceneReadyRes enterSceneReady(long uid, ProtoScene.EnterSceneReadyReq req) {
        Player player = playerRoleService.getPlayer(uid);
        return player.enterSceneReady(req);
    }

    public ProtoScene.SceneInitFinishRes sceneInitFinish(long uid, ProtoScene.SceneInitFinishReq req) {
        Player player = playerRoleService.getPlayer(uid);
        return player.sceneInitFinish(req);
    }

    public ProtoScene.EnterSceneDoneRes enterSceneDone(long uid, ProtoScene.EnterSceneDoneReq req) {
        Player player = playerRoleService.getPlayer(uid);
        return player.enterSceneDone(req);
    }

    public ProtoScene.PostEnterSceneRes postEnterScene(long uid, ProtoScene.PostEnterSceneReq req) {
        Player player = playerRoleService.getPlayer(uid);
        return player.postEnterScene(req);
    }

    public ProtoScene.LeaveSceneRes leaveScene(long uid, ProtoScene.LeaveSceneReq req) {
        Player player = playerRoleService.getPlayer(uid);
        return null;
    }

    public ProtoScene.SceneTransToPointRes sceneTransToPoint(long uid, ProtoScene.SceneTransToPointReq req) {
        Player player = playerRoleService.getPlayer(uid);
        return null;
    }

    public ProtoScene.SceneCreateEntityRes sceneCreateEntity(long uid, ProtoScene.SceneCreateEntityReq req) {
        Player player = playerRoleService.getPlayer(uid);
        return null;
    }

    public ProtoScene.SceneDestroyEntityRes sceneDestroyEntity(long uid, ProtoScene.SceneDestroyEntityReq req) {
        Player player = playerRoleService.getPlayer(uid);
        return null;
    }

    public ProtoScene.EntityForceSyncRes entityForceSync(long uid, ProtoScene.EntityForceSyncReq req) {
        Player player = playerRoleService.getPlayer(uid);
        return null;
    }

    public ProtoScene.ClientTransmitRes clientTransmit(long uid, ProtoScene.ClientTransmitReq req) {
        Player player = playerRoleService.getPlayer(uid);
        return null;
    }

    public ProtoScene.PersonalSceneJumpRes personalSceneJump(long uid, ProtoScene.PersonalSceneJumpReq req) {
        Player player = playerRoleService.getPlayer(uid);
        return null;
    }

    public ProtoScene.JoinPlayerSceneRes joinPlayerScene(long uid, ProtoScene.JoinPlayerSceneReq req) {
        Player player = playerRoleService.getPlayer(uid);
        return null;
    }

    public ProtoScene.SceneKickPlayerRes sceneKickPlayer(long uid, ProtoScene.SceneKickPlayerReq req) {
        Player player = playerRoleService.getPlayer(uid);
        return null;
    }

    public ProtoScene.BackMyWorldRes backMyWorld(long uid, ProtoScene.BackMyWorldReq req) {
        Player player = playerRoleService.getPlayer(uid);
        return null;
    }

    public ProtoScene.SceneEntitiesMoveRes sceneEntitiesMove(long uid, ProtoScene.SceneEntitiesMoveReq req) {
        Player player = playerRoleService.getPlayer(uid);
        return null;
    }
}
