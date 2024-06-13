package com.ares.client.controller;

import com.ares.client.bean.ClientPlayer;
import com.ares.client.bean.PlayerMgr;
import com.ares.client.performance.LogicService;
import com.ares.client.performance.PerformanceTestService;
import com.ares.core.annotation.MsgId;
import com.game.protoGen.ProtoGame;
import com.game.protoGen.ProtoMsgId;
import com.game.protoGen.ProtoTeam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TeamController implements BaseController {
    @Autowired
    private LogicService logicService;
    @Autowired
    private PerformanceTestService performanceTestService;

    @Override
    public void onPlayerLoginFinished(ClientPlayer clientPlayer, ProtoGame.GameLoginNtf response) {

        logicService.createTeam(clientPlayer, "team_name_1");
      //  logicService.joinTeam(clientPlayer, 203479558754664960L);

           // logicService.startGame(clientPlayer);
        // dismissTeam(clientPlayer);


        //\

        //   logicService.exitTeam(clientPlayer, 1713345874421L);
        //logicService.getAllTeamList(clientPlayer);
        // logicService.getAllTeamList(clientPlayer);
        //   logicService.enterDefaultScene(clientPlayer, 1);
//        logicService.sendRpcTest(clientPlayer);

        performanceTestService.sendAllPlayerToTeamPerformanceMsg();

    }


    @MsgId(ProtoMsgId.MsgId.TEAM_OWN_NTF_VALUE)
    public void teamOwnInfo(long uid, ProtoTeam.TeamInfoNtf teamInfoNtf) {
        ClientPlayer clientPlayer = PlayerMgr.Instance.getClientPlayer(uid);
        log.info("XXXXXXXXXXXXXXXXXXXXX  uid ={}  owner teamId ={}", uid, teamInfoNtf);
    }

    @MsgId(ProtoMsgId.MsgId.TEAM_CREATE_NTF_VALUE)
    public void onTeamCreateResponse(long uid, ProtoTeam.TeamInfo teamInfo) {
        ClientPlayer clientPlayer = PlayerMgr.Instance.getClientPlayer(uid);
        log.info("TEAM_CREATE_PUSH_VALUE ={}", teamInfo);
        logicService.getTeamInfo(clientPlayer, teamInfo.getTeamId());
    }


    @MsgId(ProtoMsgId.MsgId.TEAM_DETAIL_NTF_VALUE)
    public void onTeamDetailResponse(long uid, ProtoTeam.TeamInfo teamDetailRes) {
        log.info("---------- teamDetail ={}", teamDetailRes);
    }

    @MsgId(ProtoMsgId.MsgId.TEAM_JOIN_NTF_VALUE)
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
