package com.ares.client.controller;

import com.ares.client.bean.ClientPlayer;
import com.ares.client.bean.PlayerMgr;
import com.ares.client.performance.LogicService;
import com.ares.core.annotation.MsgId;
import com.ares.core.service.AresController;
import com.game.protoGen.ProtoMsgId;
import com.game.protoGen.ProtoScene;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PlayerSceneController implements AresController {
    @Autowired
    private LogicService logicService;

    @MsgId(ProtoMsgId.MsgId.PLAYER_ENTER_SCENE_NTF_VALUE)
    public void onPlayerEnterSceneNtf(long uid, ProtoScene.PlayerEnterSceneNtf ntf) {
        log.info("---------- PlayerEnterSceneNtf ={}", ntf);
        ClientPlayer clientPlayer = PlayerMgr.Instance.getClientPlayer(uid);
        logicService.enterSceneReady(clientPlayer, ntf.getEnterSceneToken());
    }

    @MsgId(ProtoMsgId.MsgId.ENTER_SCENE_READY_RES_VALUE)
    public void onEnterSceneReadyRes(long uid, ProtoScene.EnterSceneReadyRes ntf) {
        log.info("---------- EnterSceneReadyRes ={}", ntf);
        ClientPlayer clientPlayer = PlayerMgr.Instance.getClientPlayer(uid);
        logicService.sceneInitFinish(clientPlayer, ntf.getEnterSceneToken());
    }

    @MsgId(ProtoMsgId.MsgId.SCENE_INIT_FINISH_RES_VALUE)
    public void onSceneInitFinishRes(long uid, ProtoScene.SceneInitFinishRes ntf) {
        log.info("---------- SceneInitFinishRes ={}", ntf);
        ClientPlayer clientPlayer = PlayerMgr.Instance.getClientPlayer(uid);
        logicService.enterSceneDone(clientPlayer, ntf.getEnterSceneToken());
    }

    @MsgId(ProtoMsgId.MsgId.ENTER_SCENE_DONE_RES_VALUE)
    public void onEnterSceneDoneRes(long uid, ProtoScene.EnterSceneDoneRes ntf) {
        log.info("---------- EnterSceneDoneRes ={}", ntf);
        ClientPlayer clientPlayer = PlayerMgr.Instance.getClientPlayer(uid);
        logicService.postEnterScene(clientPlayer, ntf.getEnterSceneToken());
    }

    @MsgId(ProtoMsgId.MsgId.POST_ENTER_SCENE_RES_VALUE)
    public void onPostEnterSceneRes(long uid, ProtoScene.PostEnterSceneRes ntf) {
        log.info("---------- PostEnterSceneRes ={}", ntf);
    }

    @MsgId(ProtoMsgId.MsgId.SCENE_ENTITY_APPEAR_NTF_VALUE)
    public void onSceneEntityAppearNtf(long uid, ProtoScene.SceneEntityAppearNtf ntf) {
        log.info("---------- SceneEntityAppearNtf ={}", ntf);
        ClientPlayer clientPlayer = PlayerMgr.Instance.getClientPlayer(uid);
        for (ProtoScene.SceneEntityInfo entityInfo : ntf.getEntityListList()) {
            if (entityInfo.getEntityType() == ProtoScene.ProtEntityType.PROT_ENTITY_AVATAR) {
                clientPlayer.setAvatarEntityId(entityInfo.getEntityId());
                logicService.avatarMove(clientPlayer);
            }
        }
    }

    @MsgId(ProtoMsgId.MsgId.SCENE_ENTITY_DISAPPEAR_NTF_VALUE)
    public void onSceneEntityDisappearNtf(long uid, ProtoScene.SceneEntityDisappearNtf ntf) {
        log.info("---------- SceneEntityDisappearNtf ={}", ntf);
    }

    @MsgId(ProtoMsgId.MsgId.SCENE_ENTITIES_MOVE_NTF_VALUE)
    public void onSceneEntitiesMoveNtf(long uid, ProtoScene.SceneEntitiesMoveNtf ntf) {
        log.info("---------- SceneEntitiesMoveNtf ={}", ntf);
    }
}
