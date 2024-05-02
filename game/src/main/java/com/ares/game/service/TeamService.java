package com.ares.game.service;

import com.ares.game.network.PeerConn;
import com.ares.game.player.GamePlayer;
import com.game.protoGen.ProtoInner;
import com.game.protoGen.ProtoTeam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TeamService {
    @Autowired
    private PlayerRoleService playerRoleService;
    @Autowired
    private PeerConn peerConn;

    public void teamCreateReq(long uid, ProtoTeam.CreateTeamReq createTeamReq) {
        GamePlayer player = playerRoleService.getPlayer(uid);
        ProtoTeam.TeamMemberInfo owner = ProtoTeam.TeamMemberInfo.newBuilder().setActorId(uid)
                .setNickName(player.getBasicModule().getNickName()).build();

        ProtoInner.InnerCreateTeamReq innerCreateTeamReq = ProtoInner.InnerCreateTeamReq.newBuilder()
                .setTeamMember(owner)
                .setTeamName("team-name")
                .setTeamDes("teamDes")
                .build();
        peerConn.routerToTeam(uid, ProtoInner.InnerMsgId.INNER_CREATE_TEAM_REQ_VALUE, innerCreateTeamReq);
    }

    public void teamJoinReq(long uid, ProtoTeam.JoinTeamReq joinTeamReq) {
        GamePlayer player = playerRoleService.getPlayer(uid);
        ProtoTeam.TeamMemberInfo newMember = ProtoTeam.TeamMemberInfo.newBuilder().setActorId(uid)
                .setNickName(player.getBasicModule().getNickName()).build();

        ProtoInner.InnerJoinTeamReq innerJoinTeamReq = ProtoInner.InnerJoinTeamReq.newBuilder()
                .setTeamId(joinTeamReq.getTeamId())
                .setTeamMember(newMember).build();
        peerConn.routerToTeam(uid, ProtoInner.InnerMsgId.INNER_JOIN_TEAM_REQ_VALUE, innerJoinTeamReq);
    }

}
