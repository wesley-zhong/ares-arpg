package com.ares.team.service;

import com.ares.core.tcp.AresTKcpContext;
import com.ares.core.utils.AresContextThreadLocal;
import com.ares.team.bean.Team;
import com.ares.team.discovery.OnDiscoveryWatchService;
import com.ares.team.network.PeerConn;
import com.ares.team.player.TeamPlayer;
import com.ares.transport.bean.ServerNodeInfo;
import com.game.protoGen.ProtoCommon;
import com.game.protoGen.ProtoInner;
import com.game.protoGen.ProtoTeam;
import com.google.protobuf.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TeamService {
    @Autowired
    private PeerConn peerConn;
    @Autowired
    private OnDiscoveryWatchService onDiscoveryWatchService;
    private TeamPlayerMgr teamPlayerMgr = new TeamPlayerMgr();

    public void teamCreateReq(long uid, ProtoInner.InnerCreateTeamReq createTeamReq) {
        TeamPlayer player = teamPlayerMgr.getPlayer(uid);
        if (player != null) {
            log.error("uid ={} have in  team", uid);
            peerConn.routerErrToGame(uid, ProtoCommon.MsgId.TEAM_CREATE_RES_VALUE, ProtoCommon.ErrCode.TEAM_ERROR_ALREADY_IN_TEAM_VALUE);
            return;
        }
        Team team = teamPlayerMgr.createTeam(createTeamReq, uid);
        peerConn.routerToGame(uid, ProtoCommon.MsgId.TEAM_CREATE_RES_VALUE, createTeamInfo(team));
    }


    public void teamDetailReq(long uid, ProtoTeam.GetTeamDetailReq getTeamDetailReq) {
        long teamId = getTeamDetailReq.getTeamId();
        Team team = teamPlayerMgr.getTeam(teamId);
        ProtoTeam.TeamInfo teamInfo = createTeamInfo(team);
        peerConn.routerToGame(uid, ProtoCommon.MsgId.TEAM_DETAIL_RES_VALUE, teamInfo);
    }


    public void teamListReq(long uid, ProtoTeam.GetTeamListReq teamListReq) {
        ProtoTeam.GetTeamListRes.Builder builder = ProtoTeam.GetTeamListRes.newBuilder();
        for (Team team : teamPlayerMgr.getTeamList()) {
            ProtoTeam.TeamInfo teamInfo = createTeamInfo(team);
            builder.addTeamList(teamInfo);
        }
        peerConn.routerToGame(uid, ProtoCommon.MsgId.TEAM_LIST_RES_VALUE, builder.build());
    }

    public void joinTeam(long uid, ProtoInner.InnerJoinTeamReq joinTeamReq) {
        Team team = teamPlayerMgr.getTeam(joinTeamReq.getTeamId());
        if (team == null) {
            log.error(" uid = {} request teamId ={} not exist", uid, joinTeamReq.getTeamId());
            peerConn.routerErrToGame(uid, ProtoCommon.MsgId.TEAM_JOIN_RES_VALUE, ProtoCommon.ErrCode.TEAM_NOT_EXIST_VALUE);
            return;
        }
        if (team.isMax()) {
            log.error(" uid = {} request teamId ={} reach max", uid, joinTeamReq.getTeamId());
            peerConn.routerErrToGame(uid, ProtoCommon.MsgId.TEAM_JOIN_RES_VALUE, ProtoCommon.ErrCode.TEAM_REACH_MAX_VALUE);
        }
        TeamPlayer teamPlayer = teamPlayerMgr.getTeamPlayer(uid);
        if (teamPlayer != null) {
            if (teamPlayer.getTeam() != null) {
                teamExit(uid);
                log.error("uid ={} already in team  teamId ={} exit first", uid, teamPlayer.getTeam().getId());
                return;
            }
        }
        if (teamPlayer == null) {
            teamPlayer = new TeamPlayer(joinTeamReq.getTeamMember(), team);
        }

        //send to others
        ProtoTeam.TeamInfo teamInfoUpdateInfo = ProtoTeam.TeamInfo.newBuilder().setTeamId(team.getId()).setCurNum(team.getCurSize()).setMaxNum(team.getMaxNum()).setTeamName(team.getName()).setTeamLeader(team.getOwner().getName()).addMemberList(createTeamMemberInfo(teamPlayer)).build();
        teamBroadCast(team, ProtoCommon.MsgId.TEAM_INFO_NTF_VALUE, teamInfoUpdateInfo);

        team.addMember(teamPlayer);
        teamPlayerMgr.addTeamPlayer(teamPlayer);
        ProtoTeam.TeamInfo teamInfo = createTeamInfo(team);
        peerConn.routerToGame(uid, ProtoCommon.MsgId.TEAM_JOIN_RES_VALUE, teamInfo);
        checkTeamStartGame(team);
    }

    private void checkTeamStartGame(Team team) {
        if (!team.isMax()) {
            return;
        }
        //start game
        ServerNodeInfo serverNodeInfo = onDiscoveryWatchService.getServerNodeInfo(team.getOwner().getFromGsrId());
        for (TeamPlayer teamPlayer : team.getTeamMembers().values()) {
            ProtoInner.InnerTeamMemberJoinTargetPlayerScene_NTF.Builder builder = ProtoInner.InnerTeamMemberJoinTargetPlayerScene_NTF.newBuilder();
            builder.setTargetId(team.getOwner().getUid());
            builder.setGameServiceId(serverNodeInfo.getServiceId());
            peerConn.routerToGame(teamPlayer.getUid(), ProtoInner.InnerMsgId.INNER_TEAM_START_GAME_VALUE, builder.build());
        }
    }


    public void teamExit(long uid) {
        TeamPlayer teamPlayer = teamPlayerMgr.getTeamPlayer(uid);
        if (teamPlayer == null) {
            return;
        }
        Team team = teamPlayer.getTeam();
        team.deleteMember(teamPlayer);
        teamPlayerMgr.deleteTeamPlayer(uid);
        if (team.isEmpty()) {
            teamPlayerMgr.deleteTeam(team.getId());
            return;
        }

        ProtoTeam.TeamInfo teamInfo = ProtoTeam.TeamInfo.newBuilder()
                .setTeamId(team.getId())
                .setCurNum(team.getCurSize())
                .setMaxNum(team.getMaxNum())
                .setTeamName(team.getName())
                .setTeamLeader(team.getOwner().getName())
                .addDeleteMemberList(uid).build();
        teamBroadCast(team, ProtoCommon.MsgId.TEAM_INFO_NTF_VALUE, teamInfo);
    }

    private void teamBroadCast(Team team, int msgId, Message body) {
        for (TeamPlayer value : team.getTeamMembers().values()) {
            peerConn.routerToGame(value.getUid(), msgId, body);
        }
    }


    public void teamDismiss(long uid, ProtoTeam.DismissTeamReq dismissTeamReq) {


    }

    private ProtoTeam.TeamInfo createTeamInfo(Team team) {
        ProtoTeam.TeamInfo.Builder teamInfoBuilder = ProtoTeam.TeamInfo.newBuilder();
        if (team == null) {
            return teamInfoBuilder.build();
        }
        teamInfoBuilder.setTeamId(team.getId()).setCurNum(team.getCurSize()).setMaxNum(team.getMaxNum()).setTeamName(team.getName()).setTeamLeader(team.getOwner().getName());
        for (TeamPlayer value : team.getTeamMembers().values()) {
            ProtoTeam.TeamMemberInfo teamMemberInfo = createTeamMemberInfo(value);
            teamInfoBuilder.addMemberList(teamMemberInfo);
        }
        return teamInfoBuilder.build();
    }

    private ProtoTeam.TeamMemberInfo createTeamMemberInfo(TeamPlayer teamPlayer) {
        return ProtoTeam.TeamMemberInfo.newBuilder().setNickName(teamPlayer.getName()).setActorId(teamPlayer.getUid()).setLevel(teamPlayer.getLevel()).build();
    }
}
