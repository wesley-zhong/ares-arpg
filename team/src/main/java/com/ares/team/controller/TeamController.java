package com.ares.team.controller;

import com.ares.core.annotation.MsgId;
import com.ares.core.service.AresController;
import com.ares.team.network.PeerConn;
import com.ares.team.service.PlayerService;
import com.ares.team.service.TeamPlayerMgr;
import com.game.protoGen.ProtoCommon;
import com.game.protoGen.ProtoGame;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TeamController implements AresController {
    @Autowired
    private PlayerService playerService;

    @Autowired
    private TeamPlayerMgr teamPlayerMgr;
    @Autowired
    private PeerConn peerConn;

    @MsgId(ProtoCommon.ProtoCode.DIRECT_TO_TEAM_REQ_VALUE)
    public void directToTeamMsg(long uid, ProtoGame.DirectToWorldReq directToWorldReq) {
        log.info("XXXXXXXXXX  directToTeamMsg uid={}  body={} ", uid, directToWorldReq);
        ProtoGame.DirectToWorldRes fromWorld = ProtoGame.DirectToWorldRes.newBuilder()
                .setResBody("from world")
                .setSomeId(881)
                .setSomeIdAdd(9955599L).build();
        peerConn.routerToGame(uid, ProtoCommon.ProtoCode.DIRECT_TO_TEAM_RES_VALUE, fromWorld);
    }
}
