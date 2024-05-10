package com.ares.game.controller;

import com.ares.core.annotation.MsgId;
import com.ares.core.service.AresController;
import com.ares.game.service.TeamService;
import com.game.protoGen.ProtoCommon;
import com.game.protoGen.ProtoInner;
import com.game.protoGen.ProtoTeam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GameTeamController implements AresController {
    @Autowired
    private TeamService teamService;

    @MsgId(ProtoCommon.MsgId.TEAM_CREATE_REQ_VALUE)
    public void teamCreateReq(long uid, ProtoTeam.CreateTeamReq createTeamReq) {
        teamService.teamCreateReq(uid, createTeamReq);
    }

    @MsgId(ProtoCommon.MsgId.TEAM_JOIN_REQ_VALUE)
    public void teamJoinReq(long uid, ProtoTeam.JoinTeamReq joinTeamReq) {
        teamService.teamJoinReq(uid, joinTeamReq);
    }

    @MsgId(ProtoInner.InnerMsgId.INNER_TEAM_START_GAME_VALUE)
    public void innerTeamStartGame(long uid, ProtoInner.InnerTeamMemberJoinTargetPlayerScene_NTF innerTeamStartGameNFC) {
        teamService.teamStartGame(uid, innerTeamStartGameNFC);
    }


//    @MsgId(ProtoInner.InnerMsgId.INNER_CREATE_TEAM_RES_VALUE)
//    public void innerCreateTeamRes(long uid, ProtoInner.InnerCreateTeamRes innerCreateTeamRes) {
//        teamService.innerTeamCreateRes(uid, innerCreateTeamRes);
//    }
}
