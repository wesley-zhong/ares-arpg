package com.ares.game.service;

import com.ares.game.discovery.OnDiscoveryWatchService;
import com.ares.game.network.PeerConn;
import com.ares.game.player.Player;
import com.ares.transport.bean.ServerNodeInfo;
import com.game.protoGen.ProtoInner;
import com.game.protoGen.ProtoMsgId;
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

    public void teamCreateReq(long uid, ProtoTeam.CreateTeamPush createTeamReq) {
        Player player = playerRoleService.getPlayer(uid);
        ProtoTeam.TeamMemberInfo owner = ProtoTeam.TeamMemberInfo.newBuilder()
                .setActorId(uid)
                .setNickName(player.getBasicModule().getNickName())
                .setFromGsrId(onDiscoveryWatchService.getMySelfNodeInfo().getServiceId())
                .build();

        ProtoInner.InnerCreateTeamReq innerCreateTeamReq = ProtoInner.InnerCreateTeamReq.newBuilder()
                .setTeamMember(owner)
                .setTeamName(createTeamReq.getTeamName())
                .setTeamDes(createTeamReq.getDes())
                .build();
        peerConn.routerToTeam(uid, ProtoInner.InnerMsgId.INNER_CREATE_TEAM_REQ_VALUE, innerCreateTeamReq);
    }

    public void onPlayerLogin(long uid) {
        peerConn.routerToTeam(uid, ProtoInner.InnerMsgId.INNER_TO_GAME_LOGIN_REQ_VALUE, null);
    }

    public void teamJoinReq(long uid, ProtoTeam.JoinTeamPush joinTeamReq) {
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
        if (!targetServiceId.equals(mySelfNodeInfo.getServiceId())) {//需要迁移game server
            //change to another game server
            //local server should do something to clear data
            peerConn.removePlayerContext(uid);
            log.info("*******************  send msg move uid={} to game server ={}", uid, targetServiceId);
            ProtoInner.InnerSceneChangeReq build = ProtoInner.InnerSceneChangeReq.newBuilder()
                    .setTargetId(innerTeamStartGameNFC.getTargetId())
                    .setGameSrvId(innerTeamStartGameNFC.getGameServiceId())
                    .build();
            peerConn.sendGateWayMsg(uid, ProtoInner.InnerMsgId.INNER_PLAYER_MOVED_GAME_SERVER_VALUE, build);
            peerConn.sendRouter(uid, ProtoInner.InnerMsgId.INNER_PLAYER_MOVED_GAME_SERVER_VALUE, build);
        }
        //send to player start game
        log.info("*************  send uid ={} to start game ", uid);
        ProtoTeam.TeamStartGameNtf teamStartNtf = ProtoTeam.TeamStartGameNtf.newBuilder()
                .setSceneId("scene_1")
                .setTeamId(innerTeamStartGameNFC.getTeamId())
                .build();
        peerConn.sendGateWayMsg(uid, ProtoMsgId.MsgId.TEAM_START_GAME_NTF_VALUE, teamStartNtf);

        //player enter scene
    }
}
