package com.ares.game.controller;

import com.ares.core.annotation.MsgId;
import com.ares.core.service.AresController;
import com.ares.game.service.TeamService;
import com.game.protoGen.ProtoInner;
import com.game.protoGen.ProtoMsgId;
import com.game.protoGen.ProtoTeam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GameTeamController implements AresController {
    @Autowired
    private TeamService teamService;

    @MsgId(ProtoMsgId.MsgId.TEAM_CREATE_PUSH_VALUE)
    public void teamCreateReq(long uid, ProtoTeam.CreateTeamPush createTeamReq) {
        teamService.teamCreateReq(uid, createTeamReq);
    }

    @MsgId(ProtoMsgId.MsgId.TEAM_JOIN_PUSH_VALUE)
    public void teamJoinReq(long uid, ProtoTeam.JoinTeamPush joinTeamReq) {
        teamService.teamJoinReq(uid, joinTeamReq);
    }

    @MsgId(ProtoInner.InnerMsgId.INNER_TEAM_START_GAME_VALUE)
    public void innerTeamStartGame(long uid, ProtoInner.InnerTeamMemberJoinTargetPlayerScene_NTF innerTeamStartGameNFC) {
        teamService.teamStartGame(uid, innerTeamStartGameNFC);
    }
}
