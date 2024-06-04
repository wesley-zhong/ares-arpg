package com.router.network;


import com.ares.common.bean.ServerType;
import com.ares.core.bean.AresPacket;
import com.ares.dal.game.UserOnlineService;
import com.ares.dal.game.UserOnlineStateDO;
import com.ares.transport.bean.ServerNodeInfo;
import com.ares.transport.bean.TcpConnServerInfo;
import com.ares.transport.peer.PeerConnBase;
import com.router.bean.RouterPlayerInterTransferInfo;
import com.router.discovery.OnDiscoveryWatchService;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Component
@Slf4j
public class PeerConn extends PeerConnBase {
    @Autowired
    private UserOnlineService userOnlineService;
    @Autowired
    private OnDiscoveryWatchService onDiscoveryWatchService;
    private int MAX_PLAYER_CACHE = 20000;
    private Map<Long, RouterPlayerInterTransferInfo> routerPlayerInterTransferInfoMap = new ConcurrentHashMap<>();


    public void directToTeam(long uid, AresPacket aresPacket) {
        innerRedirectTo(ServerType.TEAM, uid, aresPacket);
    }


    public void directToGame(long uid, AresPacket aresPacket) {
        innerRedirectTo(ServerType.GAME, uid, aresPacket);
    }


    @Override
    public Channel loadBalance(int serverType, long uid) {
        Channel channel = getExistChannel(uid, serverType);
        if (channel != null) {
            return channel;
        }
        //select  channel
        UserOnlineStateDO userOnlineStateDO = userOnlineService.getUserOnlineStateDO(uid);
        if (userOnlineStateDO == null) {
            log.error("error=====, loadBalance  uid={}", uid);
            return null;
        }
        String targetServId = userOnlineStateDO.getGmSrId();
        if (serverType == ServerType.TEAM.getValue()) {
            targetServId = userOnlineStateDO.getTmSrId();
        }

        TcpConnServerInfo serverTcpConnInfo = getServerTcpConnInfo(targetServId);
        if (serverTcpConnInfo == null) {
            //玩家所在的game server 不在了(宕机），玩家还未重新登录
            if (serverType == ServerType.GAME.getValue()) {
                log.error("uid = {} serverId ={} not found server instance", uid, targetServId);
                return null;
            }

            //玩家上次所在的  team server 宕机了，重新选择一个可以用的 team server
            ServerNodeInfo lowerLoadServerNodeInfo = onDiscoveryWatchService.getLowerLoadServerNodeInfo(serverType);
            serverTcpConnInfo = getTcpConnServerInfo(lowerLoadServerNodeInfo);
            userOnlineService.resetTeamServId(uid, serverTcpConnInfo.getServerNodeInfo().getServiceId());
        }

        Channel selectedChannel = serverTcpConnInfo.roubinChannel();
        saveChannel(uid, serverTcpConnInfo.getServerNodeInfo(), selectedChannel);
        return selectedChannel;
    }

    public void removePlayerCacheChannel(long uid) {
        routerPlayerInterTransferInfoMap.remove(uid);
    }

    public Channel resetPlayerTeamChannel(long uid, String teamSrvId) {
        RouterPlayerInterTransferInfo routerPlayerInterTransferInfo = routerPlayerInterTransferInfoMap.get(uid);
        if (routerPlayerInterTransferInfo == null) {
            log.error("XXXXXXXXXXXXXXXXXX   uid = {} reset player team channel ={} failed", uid, teamSrvId);
            return null;
        }
        if (routerPlayerInterTransferInfo.getTeamCtx().getServiceId().equals(teamSrvId)) {
            return null;
        }

        Channel lastChanel = routerPlayerInterTransferInfo.getTeamCtx().getChannel();
        TcpConnServerInfo serverTcpConnInfo = getServerTcpConnInfo(teamSrvId);
        if (serverTcpConnInfo == null) {
            log.error("XXXXXX  team serverId ={} not found1", teamSrvId);
            return null;
        }
        Channel channel = serverTcpConnInfo.roubinChannel();
        if (channel == null) {
            log.error("XXXXXX  team serverId ={} not found2", teamSrvId);
            return null;
        }
        saveChannel(uid, serverTcpConnInfo.getServerNodeInfo(), channel);
        return lastChanel;
    }


    private Channel getExistChannel(long uid, int serverType) {
        RouterPlayerInterTransferInfo routerPlayerInterTransferInfo = routerPlayerInterTransferInfoMap.get(uid);
        if (routerPlayerInterTransferInfo != null) {
            Channel channel = routerPlayerInterTransferInfo.getContextByType(serverType);
            if (channel != null && channel.isActive()) {
                return channel;
            }
        }
        return null;
    }

    public void saveChannel(long uid, ServerNodeInfo serverNodeInfo, Channel channel) {
        RouterPlayerInterTransferInfo routerPlayerInterTransferInfo = routerPlayerInterTransferInfoMap.get(uid);//computeIfAbsent(uid, (value) -> new RouterPlayerInterTransferInfo());
        if (routerPlayerInterTransferInfo == null) {
            routerPlayerInterTransferInfo = new RouterPlayerInterTransferInfo();
            routerPlayerInterTransferInfoMap.put(uid, routerPlayerInterTransferInfo);
        }
        routerPlayerInterTransferInfo.setContext(serverNodeInfo, channel);
    }
}
