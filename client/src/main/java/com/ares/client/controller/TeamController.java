package com.ares.client.controller;

import com.ares.client.bean.ClientPlayer;
import com.ares.client.bean.PlayerMgr;
import com.ares.client.performance.LogicService;
import com.ares.core.annotation.MsgId;
import com.ares.core.service.AresController;
import com.game.protoGen.ProtoMsgId;
import com.game.protoGen.ProtoTeam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TeamController implements AresController {
    @Autowired
    private LogicService logicService;
    @MsgId(ProtoMsgId.MsgId.TEAM_OWN_NTF_VALUE)
    public void teamOwnInfo(long uid, ProtoTeam.TeamInfoNtf teamInfoNtf) {
        ClientPlayer clientPlayer = PlayerMgr.Instance.getClientPlayer(uid);
        log.info("XXXXXXXXXXXXXXXXXXXXX  uid ={}  owner teamId ={}", uid, teamInfoNtf);
    }

    @MsgId(ProtoMsgId.MsgId.TEAM_CREATE_PUSH_VALUE)
    public void onTeamCreateResponse(long uid, ProtoTeam.TeamInfo teamInfo) {
        ClientPlayer clientPlayer = PlayerMgr.Instance.getClientPlayer(uid);
        log.info("TEAM_CREATE_PUSH_VALUE ={}", teamInfo);
        logicService.getTeamInfo(clientPlayer, teamInfo.getTeamId());
    }


    @MsgId(ProtoMsgId.MsgId.TEAM_DETAIL_PUSH_VALUE)
    public void onTeamDetailResponse(long uid, ProtoTeam.TeamInfo teamDetailRes) {
        log.info("---------- teamDetail ={}", teamDetailRes);
    }

    @MsgId(ProtoMsgId.MsgId.TEAM_JOIN_PUSH_VALUE)
    public void onTeamJoinRes(long uid, ProtoTeam.TeamInfo joinTeamRes) {
        log.info("======== team joinRes = {}", joinTeamRes);
    }

    @MsgId(ProtoMsgId.MsgId.TEAM_INFO_NTF_VALUE)
    public void onTeamUpdateNotify(long uid, ProtoTeam.TeamInfoNtf teamInfoNtf) {
        log.info("xxxxxxxxxxxxxx  team info ntf ={}", teamInfoNtf);
    }

    @MsgId(ProtoMsgId.MsgId.TEAM_LIST_NTF_VALUE)
    public void onTeamListRes(long uid, ProtoTeam.GetTeamListNtf res) {
        log.info("PPPPPPPp team list ={}", res);
    }

}
