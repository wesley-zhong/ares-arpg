package com.ares.game.service;

import com.ares.game.discovery.OnDiscoveryWatchService;
import com.ares.game.network.PeerConn;
import com.ares.game.player.Player;
import com.ares.transport.bean.ServerNodeInfo;
import com.game.protoGen.ProtoInner;
import com.game.protoGen.ProtoTeam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TeamService {
    @Autowired
    private PlayerRoleService playerRoleService;
    @Autowired
    private PeerConn peerConn;
    @Autowired
    private OnDiscoveryWatchService onDiscoveryWatchService;

    public void teamCreateReq(long uid, ProtoTeam.CreateTeamReq createTeamReq) {
        Player player = playerRoleService.getPlayer(uid);
        ProtoTeam.TeamMemberInfo owner = ProtoTeam.TeamMemberInfo.newBuilder()
                .setActorId(uid)
                .setNickName(player.getBasicModule().getNickName())
                .setFromGsrId(onDiscoveryWatchService.getMySelfNodeInfo().getServiceId())
                .build();

        ProtoInner.InnerCreateTeamReq innerCreateTeamReq = ProtoInner.InnerCreateTeamReq.newBuilder()
                .setTeamMember(owner)
                .setTeamName("team-name")
                .setTeamDes("teamDes")
                .build();
        peerConn.routerToTeam(uid, ProtoInner.InnerMsgId.INNER_CREATE_TEAM_REQ_VALUE, innerCreateTeamReq);
    }

    public void teamJoinReq(long uid, ProtoTeam.JoinTeamReq joinTeamReq) {
        Player player = playerRoleService.getPlayer(uid);
        ProtoTeam.TeamMemberInfo newMember = ProtoTeam.TeamMemberInfo.newBuilder()
                .setActorId(uid)
                .setNickName(player.getBasicModule().getNickName())
                .setFromGsrId(onDiscoveryWatchService.getMySelfNodeInfo().getServiceId())
                .build();

        ProtoInner.InnerJoinTeamReq innerJoinTeamReq = ProtoInner.InnerJoinTeamReq.newBuilder()
                .setTeamId(joinTeamReq.getTeamId())
                .setTeamMember(newMember).build();
        peerConn.routerToTeam(uid, ProtoInner.InnerMsgId.INNER_JOIN_TEAM_REQ_VALUE, innerJoinTeamReq);
    }

    public void teamStartGame(long uid, ProtoInner.InnerTeamMemberJoinTargetPlayerScene_NTF innerTeamStartGameNFC) {
        Player player = playerRoleService.getPlayer(uid);
        if (player == null) {
            log.error(" pid = {} not found");
            return;
        }
        ServerNodeInfo mySelfNodeInfo = onDiscoveryWatchService.getMySelfNodeInfo();

        String targetServiceId = innerTeamStartGameNFC.getGameServiceId();
        if (targetServiceId.equals(mySelfNodeInfo.getServiceId())) {
            return;
        }

        //change to another gameserver
        //local server should do some thing
        ProtoInner.InnerSceneChangeReq build = ProtoInner.InnerSceneChangeReq.newBuilder()
                .setTargetId(innerTeamStartGameNFC.getTargetId())
                .setGameSrvId(innerTeamStartGameNFC.getGameServiceId())
                .build();
        peerConn.sendGateWayMsg(uid, ProtoInner.InnerMsgId.INNER_CHANGE_SCENE_REQ_VALUE, build);

    }
}
