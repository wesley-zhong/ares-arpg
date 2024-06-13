package com.ares.team.service;

import com.ares.common.bean.ServerType;
import com.ares.discovery.DiscoveryService;
import com.ares.team.bean.Team;
import com.ares.team.discovery.OnDiscoveryWatchService;
import com.ares.team.enums.TeamStateEnum;
import com.ares.team.network.PeerConn;
import com.ares.team.player.TeamPlayer;
import com.ares.transport.bean.ServerNodeInfo;
import com.game.protoGen.ProtoErrorCode;
import com.game.protoGen.ProtoInner;
import com.game.protoGen.ProtoMsgId;
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
    private DiscoveryService discoveryService;
    @Autowired
    private OnDiscoveryWatchService onDiscoveryWatchService;
    private TeamPlayerMgr teamPlayerMgr = new TeamPlayerMgr();

    public void onPlayerLogin(long uid) {
        TeamPlayer player = teamPlayerMgr.getPlayer(uid);
        if (player == null) {
            return;
        }
        Team team = player.getTeam();
        ProtoTeam.TeamInfoNtf teamInfoNtf = createTeamInfoNtf(team);
        peerConn.routerToGame(uid, ProtoMsgId.MsgId.TEAM_OWN_NTF_VALUE, teamInfoNtf);
    }

    public void onPlayerMoveOut(long uid) {
        teamExit(uid);
    }

    public void teamCreateReq(long uid, ProtoInner.InnerCreateTeamReq createTeamReq) {
        TeamPlayer player = teamPlayerMgr.getPlayer(uid);
        if (player != null) {
            log.error("uid ={} have in  team", uid);
            peerConn.routerErrToGame(uid, ProtoMsgId.MsgId.TEAM_CREATE_NTF_VALUE, ProtoErrorCode.ErrCode.TEAM_ERROR_ALREADY_IN_TEAM_VALUE);
            return;
        }
        Team team = teamPlayerMgr.createTeam(createTeamReq, uid);
        peerConn.routerToGame(uid, ProtoMsgId.MsgId.TEAM_CREATE_NTF_VALUE, createTeamInfoNtf(team));
        log.info("uid ={} create team success teamId ={}", uid, team.getId());
    }


    public void teamDetailReq(long uid, ProtoTeam.GetTeamDetailPush getTeamDetailReq) {
        long teamId = getTeamDetailReq.getTeamId();
        Team team = teamPlayerMgr.getTeam(teamId);
        ProtoTeam.TeamInfoNtf teamInfo = createTeamInfoNtf(team);
        peerConn.routerToGame(uid, ProtoMsgId.MsgId.TEAM_DETAIL_NTF_VALUE, teamInfo);
    }


    public void teamListReq(long uid, ProtoTeam.GetTeamListPush teamListReq) {
        ProtoTeam.GetTeamListNtf.Builder builder = ProtoTeam.GetTeamListNtf.newBuilder();
        for (Team team : teamPlayerMgr.getTeamList()) {
            ProtoTeam.TeamInfo teamInfo = createTeamInfo(team);
            builder.addTeamList(teamInfo);
        }
        peerConn.routerToGame(uid, ProtoMsgId.MsgId.TEAM_LIST_NTF_VALUE, builder.build());
    }

    public void joinTeam(long uid, ProtoInner.InnerJoinTeamReq joinTeamReq) {
        Team team = teamPlayerMgr.getTeam(joinTeamReq.getTeamId());
        if (team == null) {
            log.error(" uid = {} request teamId ={} not exist", uid, joinTeamReq.getTeamId());
            peerConn.routerErrToGame(uid, ProtoMsgId.MsgId.TEAM_JOIN_NTF_VALUE, ProtoErrorCode.ErrCode.TEAM_NOT_EXIST_VALUE);
            return;
        }
        if (team.isMax()) {
            log.error(" uid = {} request teamId ={} reach max", uid, joinTeamReq.getTeamId());
            peerConn.routerErrToGame(uid, ProtoMsgId.MsgId.TEAM_JOIN_NTF_VALUE, ProtoErrorCode.ErrCode.TEAM_REACH_MAX_VALUE);
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
        ProtoTeam.TeamInfo teamInfoUpdateInfo = ProtoTeam.TeamInfo.newBuilder()
                .setTeamId(team.getId())
                .setCurNum(team.getCurSize())
                .setMaxNum(team.getMaxNum())
                .setTeamName(team.getName())
                .setTeamLeader(team.getOwner().getName())
                .addMemberList(createTeamMemberInfo(teamPlayer)).build();
        teamBroadCast(team, ProtoMsgId.MsgId.TEAM_INFO_NTF_VALUE, teamInfoUpdateInfo);

        team.addMember(teamPlayer);
        teamPlayerMgr.addTeamPlayer(teamPlayer);
        ProtoTeam.TeamInfoNtf teamInfo = createTeamInfoNtf(team);
        peerConn.routerToGame(uid, ProtoMsgId.MsgId.TEAM_JOIN_NTF_VALUE, teamInfo);
        log.info("uid ={} join team id ={} success", uid, team.getId());
        sendRouterPlayerMoved(uid, joinTeamReq.getLastTeamServiceId());
        checkTeamStartGame(team);
    }

    private void sendRouterPlayerMoved(long uid, String lastTeamServiceId) {
        ServerNodeInfo myselfNodeInfo = discoveryService.getEtcdRegister().getMyselfNodeInfo();
        if (myselfNodeInfo.getServiceId().equals(lastTeamServiceId)) {
            return;
        }
        ProtoInner.InnerTeamMoveToReq build = ProtoInner.InnerTeamMoveToReq.newBuilder()
                .setMoveToTeamSrvId(myselfNodeInfo.getServiceId())
                .build();
        peerConn.send(ServerType.ROUTER, uid, ProtoInner.InnerMsgId.INNER_PLAYER_MOVED_IN_TO_TEAM_SERVER_VALUE, build);
        log.info("send uid ={} move team service from ={} to me ={}", uid, lastTeamServiceId, myselfNodeInfo.getServiceId());
    }

    public void startTeam(long uid, ProtoTeam.TeamStartGamePush teamStartGamePush) {
        TeamPlayer teamPlayer = teamPlayerMgr.getTeamPlayer(uid);
        if (teamPlayer == null) {
            log.error("startTeam uid = {} not found", uid);
            return;
        }
        Team team = teamPlayer.getTeam();
        if (team == null) {
            log.error("startTeam uid = {} no team", uid);
            return;
        }
        if (team.getState() != TeamStateEnum.CREATED) {
            log.error(" uid ={} teamId={} state ={} not valid", uid, team.getId(), team.getState());
            return;
        }
        startGame(team);
    }


    private void checkTeamStartGame(Team team) {
        if (!team.isMax()) {
            return;
        }
        //start game
        startGame(team);
    }

    private void startGame(Team team) {
        team.start();
        String gameServiceId = selectGameServer(team);
        log.info("team id ={}  start game select game server ={}", team.getId(), gameServiceId);
        for (TeamPlayer teamPlayer : team.getTeamMembers().values()) {
            //房主不切换
            if (teamPlayer.getUid() == team.getOwnerId()) {
                continue;
            }
            ProtoInner.InnerTeamMemberJoinTargetPlayerScene_NTF.Builder builder = ProtoInner.InnerTeamMemberJoinTargetPlayerScene_NTF.newBuilder();
            builder.setTargetId(team.getOwner().getUid());
            builder.setGameServiceId(gameServiceId);
            builder.setTeamId(team.getId());
            peerConn.routerToGame(teamPlayer.getUid(), ProtoInner.InnerMsgId.INNER_TEAM_START_GAME_VALUE, builder.build());
        }
    }

    /**
     * 选择一个game server , 需要一些策略1：gameserver 的负载， 2： 大部分玩家所在的game server (减少玩家迁移数据）
     *
     * @param team
     * @return
     */
    private String selectGameServer(Team team) {
        ServerNodeInfo serverNodeInfo = onDiscoveryWatchService.getServerNodeInfo(team.getOwner().getFromGsrId());
        return serverNodeInfo.getServiceId();
    }


    public void teamExit(long uid) {
        TeamPlayer teamPlayer = teamPlayerMgr.getTeamPlayer(uid);
        if (teamPlayer == null) {
            return;
        }
        Team team = teamPlayer.getTeam();
        team.deleteMember(teamPlayer);
        //send to current player
        ProtoTeam.ExistTeamNtf exitTeamNtf = ProtoTeam.ExistTeamNtf.newBuilder().build();
        peerConn.routerToGame(uid, ProtoMsgId.MsgId.TEAM_EXIT_NTF_VALUE, exitTeamNtf);
        teamPlayerMgr.deleteTeamPlayer(uid);
        log.info(" uid ={} exit team ={}", uid, team.getId());
        if (team.isEmpty()) {
            teamPlayerMgr.deleteTeam(team.getId());
            log.info(" uid ={} exit team ={}  destroyed ", uid, team.getId());
            return;
        }

        //send to others
        ProtoTeam.TeamInfo teamInfoUpdateInfo = ProtoTeam.TeamInfo.newBuilder()
                .setTeamId(team.getId())
                .setCurNum(team.getCurSize())
                .setMaxNum(team.getMaxNum())
                .setTeamName(team.getName())
                .setTeamLeader(team.getOwner().getName())
                .addDeleteMemberList(teamPlayer.getUid()).build();
        ProtoTeam.TeamInfoNtf teamInfoNtf = ProtoTeam.TeamInfoNtf.newBuilder().setTeamInfo(teamInfoUpdateInfo).build();
        teamBroadCast(team, ProtoMsgId.MsgId.TEAM_INFO_NTF_VALUE, teamInfoNtf);
    }

    public void teamInvitePlayer(long uid, ProtoTeam.TeamInvitePush invitePush) {
        TeamPlayer teamPlayer = teamPlayerMgr.getTeamPlayer(uid);
        if (teamPlayer == null) {
            log.error("========== teamInvite player uid ={} not found", uid);
            return;
        }
        ProtoTeam.TeamInviteNtf inviteNtf = ProtoTeam.TeamInviteNtf.newBuilder()
                .setTeamId(teamPlayer.getTeam().getId())
                .setInvitorId(uid)
                .build();
        peerConn.routerToGame(invitePush.getUid(), ProtoMsgId.MsgId.TEAM_INVITE_NTF_VALUE, inviteNtf);
    }

    private void teamBroadCast(Team team, int msgId, Message body) {
        for (TeamPlayer value : team.getTeamMembers().values()) {
            peerConn.routerToGame(value.getUid(), msgId, body);
        }
    }


    public void teamDismiss(long uid, ProtoTeam.DismissTeamPush dismissTeamReq) {
        TeamPlayer teamPlayer = teamPlayerMgr.getTeamPlayer(uid);
        if (teamPlayer == null) {
            return;
        }
        Team team = teamPlayer.getTeam();
        ProtoTeam.DismissTeamNtf dismissTeamNtf = ProtoTeam.DismissTeamNtf.newBuilder().build();
        teamBroadCast(team, ProtoMsgId.MsgId.TEAM_DISMISS_NTF_VALUE, dismissTeamNtf);
        teamPlayerMgr.destroyTeam(team);
    }


    private ProtoTeam.TeamInfoNtf createTeamInfoNtf(Team team) {
        return ProtoTeam.TeamInfoNtf.newBuilder().setTeamInfo(createTeamInfo(team)).build();
    }

    public ProtoTeam.TeamInfo createTeamInfo(Team team) {
        ProtoTeam.TeamInfo.Builder teamInfoBuilder = ProtoTeam.TeamInfo.newBuilder();
        if (team == null) {
            return ProtoTeam.TeamInfo.newBuilder().build();
        }
        teamInfoBuilder.setTeamId(team.getId())
                .setCurNum(team.getCurSize())
                .setMaxNum(team.getMaxNum())
                .setTeamName(team.getName())
                .setTeamLeader(team.getOwner().getName());
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
