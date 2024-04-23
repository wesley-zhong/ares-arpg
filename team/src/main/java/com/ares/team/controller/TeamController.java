package com.ares.team.controller;

import com.ares.core.annotation.MsgId;
import com.ares.core.service.AresController;
import com.ares.team.network.PeerConn;
import com.ares.team.service.PlayerService;
import com.ares.team.service.TeamService;
import com.game.protoGen.ProtoCommon;
import com.game.protoGen.ProtoGame;
import com.game.protoGen.ProtoInner;
import com.game.protoGen.ProtoTeam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TeamController implements AresController {
    @Autowired
    private PlayerService playerService;
    @Autowired
    private PeerConn peerConn;
    @Autowired
    private TeamService teamService;

    @MsgId(ProtoCommon.MsgId.DIRECT_TO_TEAM_REQ_VALUE)
    public void directToTeamMsg(long uid, ProtoGame.DirectToWorldReq directToWorldReq) {
        log.info("XXXXXXXXXX  directToTeamMsg uid={}  body={} ", uid, directToWorldReq);
        ProtoGame.DirectToWorldRes fromWorld = ProtoGame.DirectToWorldRes.newBuilder()
                .setResBody("from world")
                .setSomeId(881)
                .setSomeIdAdd(9955599L).build();
        peerConn.routerToGame(uid, ProtoCommon.MsgId.DIRECT_TO_TEAM_RES_VALUE, fromWorld);
    }

    @MsgId(ProtoInner.InnerMsgId.INNER_CREATE_TEAM_REQ_VALUE)
    public void teamCreateReq(long uid, ProtoInner.InnerCreateTeamReq createTeamReq) {
        teamService.teamCreateReq(uid, createTeamReq);
    }

    @MsgId(ProtoCommon.MsgId.TEAM_DETAIL_REQ_VALUE)
    public void teamDetailReq(long uid, ProtoTeam.GetTeamDetailReq getTeamDetailReq) {
        teamService.teamDetailReq(uid, getTeamDetailReq);
    }

    @MsgId(ProtoCommon.MsgId.TEAM_LIST_REQ_VALUE)
    public void teamListReq(long uid, ProtoTeam.GetTeamListReq teamListReq) {
        teamService.teamListReq(uid, teamListReq);
    }

    @MsgId(ProtoInner.InnerMsgId.INNER_JOIN_TEAM_REQ_VALUE)
    public void joinTeam(long uid, ProtoInner.InnerJoinTeamReq joinTeamReq) {
        teamService.joinTeam(uid, joinTeamReq);
    }

    @MsgId(ProtoCommon.MsgId.TEAM_EXIT_REQ_VALUE)
    public void teamExit(long uid, ProtoTeam.ExitTeamReq exitTeamReq) {
        teamService.teamExit(uid);
    }

    @MsgId(ProtoCommon.MsgId.TEAM_DISMISS_VALUE)
    public void teamDismiss(long uid, ProtoTeam.DismissTeamReq dismissTeamReq) {
        teamService.teamDismiss(uid, dismissTeamReq);
    }
}
