package com.ares.game.controller;

import com.ares.core.annotation.MsgId;
import com.ares.core.service.AresController;
import com.ares.game.network.PeerConn;
import com.ares.game.service.PlayerLoginService;
import com.ares.game.service.PlayerRoleService;
import com.game.protoGen.ProtoInner;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class GameInnerLoginController implements AresController {
    @Autowired
    private PlayerRoleService playerRoleService;
    @Autowired
    private PeerConn peerConn;

    @Autowired
    private PlayerLoginService playerLoginService;

    @MsgId(ProtoInner.InnerMsgId.INNER_TO_GAME_LOGIN_REQ_VALUE)
    public void gameInnerLoginRequest(long pid, ProtoInner.InnerGameLoginRequest gameInnerLoginRequest) {
        playerLoginService.gamePlayerInnerLogin(pid, gameInnerLoginRequest);
    }

    @MsgId(ProtoInner.InnerMsgId.INNER_PLAYER_DISCONNECT_REQ_VALUE)
    public void playerDisconnected(long pid, ProtoInner.InnerPlayerDisconnectRequest innerLoginRequest) {
        log.info("======== playerDisconnected  ={}", innerLoginRequest);
    }
}
