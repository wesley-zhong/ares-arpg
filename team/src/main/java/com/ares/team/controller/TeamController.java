package com.ares.team.controller;

import com.ares.core.annotation.MsgId;
import com.ares.core.service.AresController;
import com.ares.core.tcp.AresTKcpContext;
import com.ares.core.utils.AresContextThreadLocal;
import com.ares.team.network.PeerConn;
import com.ares.team.service.PlayerService;
import com.ares.team.service.TeamService;
import com.game.protoGen.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
public class TeamController implements AresController {
    @Autowired
    private PlayerService playerService;
    @Autowired
    private PeerConn peerConn;
    @Autowired
    private TeamService teamService;

    private static AtomicInteger atomicCount = new AtomicInteger(0);
    private volatile long start = System.currentTimeMillis();

    //  test  msg
    @MsgId(ProtoMsgId.MsgId.DIRECT_TO_TEAM_REQ_VALUE)
    public void directToTeamMsg(long uid, ProtoGame.DirectToWorldReq directToWorldReq) {
        AresTKcpContext aresTKcpContext = AresContextThreadLocal.get();
        peerConn.recordPlayerRouterContext(uid, aresTKcpContext.getCtx());
        //  log.info("XXXXXXXXXX  directToTeamMsg uid={}  body={} ", uid, directToWorldReq);
        ProtoGame.DirectToWorldRes fromWorld = ProtoGame.DirectToWorldRes.newBuilder()
                .setResBody("from world")
                .setSomeId(881)
                .setSomeIdAdd(9955599L).build();
        peerConn.routerToGame(uid, ProtoMsgId.MsgId.DIRECT_TO_TEAM_RES_VALUE, fromWorld);

        int nowCount = atomicCount.incrementAndGet();
        long now = System.currentTimeMillis();
        long dis = now - start;
        if (dis >= 10000) {
            atomicCount.set(0);
            start = now;
            log.info("================ time ={}  count ={}", dis, nowCount);
        }
    }

    @MsgId(ProtoInner.InnerMsgId.INNER_PLAYER_MOVED_OUT_TEAM_SERVER_VALUE)
    public void onPlayerMoveToOtherTeamSrv(long uid, ProtoCommon.EmptyMsg emptyMsg) {
        teamService.onPlayerMoveOut(uid);
    }

    @MsgId(ProtoInner.InnerMsgId.INNER_TO_GAME_LOGIN_REQ_VALUE)
    public void onPlayerLogin(long uid, ProtoCommon.EmptyMsg emptyMsg) {
        teamService.onPlayerLogin(uid);
    }

    @MsgId(ProtoInner.InnerMsgId.INNER_CREATE_TEAM_REQ_VALUE)
    public void teamCreateReq(long uid, ProtoInner.InnerCreateTeamReq createTeamReq) {
        teamService.teamCreateReq(uid, createTeamReq);
    }

    @MsgId(ProtoMsgId.MsgId.TEAM_DETAIL_PUSH_VALUE)
    public void teamDetailReq(long uid, ProtoTeam.GetTeamDetailPush getTeamDetailReq) {
        teamService.teamDetailReq(uid, getTeamDetailReq);
    }

    @MsgId(ProtoMsgId.MsgId.TEAM_LIST_PUSH_VALUE)
    public void teamListReq(long uid, ProtoTeam.GetTeamListPush teamListReq) {
        teamService.teamListReq(uid, teamListReq);
    }

    @MsgId(ProtoInner.InnerMsgId.INNER_JOIN_TEAM_REQ_VALUE)
    public void joinTeam(long uid, ProtoInner.InnerJoinTeamReq joinTeamReq) {
        teamService.joinTeam(uid, joinTeamReq);
    }

    @MsgId(ProtoMsgId.MsgId.TEAM_EXIT_PUSH_VALUE)
    public void teamExit(long uid, ProtoTeam.ExitTeamPush exitTeamReq) {
        teamService.teamExit(uid);
    }

    @MsgId(ProtoMsgId.MsgId.TEAM_DISMISS_PUSH_VALUE)
    public void teamDismiss(long uid, ProtoTeam.DismissTeamPush dismissTeamReq) {
        teamService.teamDismiss(uid, dismissTeamReq);
    }

    @MsgId(ProtoMsgId.MsgId.TEAM_START_GAME_PUSH_VALUE)
    public void teamStartGame(long uid, ProtoTeam.TeamStartGamePush teamStartGamePush) {
        teamService.startTeam(uid, teamStartGamePush);
    }

    @MsgId(ProtoMsgId.MsgId.TEAM_INVITE_PUSH_VALUE)
    public void invitePlayer(long uid, ProtoTeam.TeamInvitePush invitePush){
        teamService.teamInvitePlayer(uid, invitePush);
    }
}
