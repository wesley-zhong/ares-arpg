package com.router.contoller;

import com.ares.common.bean.ServerType;
import com.ares.core.annotation.MsgId;
import com.ares.core.service.AresController;
import com.ares.core.utils.SnowFlake;
import com.ares.dal.game.UserOnlineService;
import com.ares.dal.game.UserOnlineStateDO;
import com.ares.transport.bean.TcpConnServerInfo;
import com.game.protoGen.ProtoInner;
import com.router.network.PeerConn;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RouterController implements AresController {
    private static final Logger log = LoggerFactory.getLogger(RouterController.class);
    @Autowired
    private PeerConn peerConn;
    @Autowired
    private UserOnlineService userOnlineService;

    @MsgId(ProtoInner.InnerMsgId.INNER_PLAYER_MOVED_GAME_SERVER_VALUE)
    public void playerChangeGameServer(long uid, ProtoInner.InnerSceneChangeReq innerSceneChangeReq) {
        peerConn.removePlayerCacheChannel(uid);
        log.info("uid ={} move to other scene ", uid);
    }

    @MsgId(ProtoInner.InnerMsgId.INNER_PLAYER_MOVED_IN_TO_TEAM_SERVER_VALUE)
    public void playerMoveToTeamServer(long uid, ProtoInner.InnerTeamMoveToReq innerTeamMoveToReq) {
        Channel lastChannel = peerConn.resetPlayerTeamChannel(uid, innerTeamMoveToReq.getMoveToTeamSrvId());
        if (lastChannel == null) {
            log.error("uid={} move to team service={} not found", uid, innerTeamMoveToReq.getMoveToTeamSrvId());
            return;
        }
        log.info("uid ={} from team server ={} move to new team service ={}", uid, lastChannel, innerTeamMoveToReq.getMoveToTeamSrvId());
        peerConn.send(lastChannel, uid, ProtoInner.InnerMsgId.INNER_PLAYER_MOVED_OUT_TEAM_SERVER_VALUE, null);
    }

    @MsgId(ProtoInner.InnerMsgId.INNER_JOIN_TEAM_REQ_VALUE)
    public void playerJoinTeam(long uid, ProtoInner.InnerJoinTeamReq joinTeamReq) {
        long teamId = joinTeamReq.getTeamId();
        int serverId = SnowFlake.parseWorkId(teamId);
        TcpConnServerInfo tcpConnServerInfo = peerConn.getServerConnByServiceId(ServerType.TEAM.getValue(), serverId);
        if (tcpConnServerInfo == null) {
            log.error("XXXXXXX  player join team id ={}  type ={} not found server ", teamId, serverId);
            return;
        }

        UserOnlineStateDO userOnlineStateDO = userOnlineService.getUserOnlineStateDO(uid);
        ProtoInner.InnerJoinTeamReq newJoinTeamReq = joinTeamReq.toBuilder().setLastTeamServiceId(userOnlineStateDO.getTmSrId()).build();
        peerConn.send(ServerType.TEAM.getValue(), serverId, uid, ProtoInner.InnerMsgId.INNER_JOIN_TEAM_REQ_VALUE, newJoinTeamReq);
        log.info("uid ={} join team  ={}  target team service ={} cur team service={}", uid, joinTeamReq.getTeamId(), tcpConnServerInfo.getServerNodeInfo().getServiceId(), userOnlineStateDO.getGmSrId());
    }
}
