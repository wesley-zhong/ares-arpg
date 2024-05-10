package com.ares.gateway.controller;

import com.ares.core.annotation.MsgId;
import com.ares.core.service.AresController;
import com.ares.dal.game.UserOnlineService;
import com.ares.gateway.service.SessionService;
import com.game.protoGen.ProtoInner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SceneChangeController implements AresController {
    @Autowired
    private SessionService  sessionService;

    @MsgId(ProtoInner.InnerMsgId.INNER_CHANGE_SCENE_REQ_VALUE)
    public void playerChangeScene(long uid, ProtoInner.InnerSceneChangeReq innerSceneChangeReq) {
        sessionService.playerChangeScene(uid, innerSceneChangeReq);
    }
}
