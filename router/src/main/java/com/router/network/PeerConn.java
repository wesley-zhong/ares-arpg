package com.router.network;


import com.ares.common.bean.ServerType;
import com.ares.common.util.LRUCache;
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


@Component
@Slf4j
public class PeerConn extends PeerConnBase {
    @Autowired
    private UserOnlineService userOnlineService;
    @Autowired
    private OnDiscoveryWatchService onDiscoveryWatchService;
    private int MAX_PLAYER_CACHE = 20000;
    private LRUCache<Long, RouterPlayerInterTransferInfo> routerPlayerInterTransferInfoMap = new LRUCache<>(MAX_PLAYER_CACHE);
    //  private Map<Long, RouterPlayerInterTransferInfo>  routerPlayerInterTransferInfoMap = new ConcurrentHashMap<>();

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

        //select new channel
        UserOnlineStateDO userOnlineStateDO = userOnlineService.getUserOnlineStateDO(uid);
        if (userOnlineStateDO == null) {
            log.error("error=====, loadBalance  uid={}", uid);
            return null;
        }
        String targetServId = userOnlineStateDO.getGmSrId();
        if (serverType == ServerType.TEAM.getValue()) {
            targetServId = userOnlineStateDO.getTmSrId();
        }

        TcpConnServerInfo serverTcpConnInfo = null;
        if (targetServId != null) {
            serverTcpConnInfo = getServerTcpConnInfo(targetServId);
        }
        //re select
        if (serverTcpConnInfo == null) {
            //玩家所在的game server 不在了，玩家还未重新登录
            if (serverType == ServerType.GAME.getValue()) {
                log.error("uid = {} serverId ={} not found server instance", uid, targetServId);
                return null;
            }
            ServerNodeInfo lowerLoadServerNodeInfo = onDiscoveryWatchService.getLowerLoadServerNodeInfo(serverType);
            serverTcpConnInfo = getTcpConnServerInfo(lowerLoadServerNodeInfo);
        }
        if (serverType == ServerType.TEAM.getValue()) {
            userOnlineService.resetTeamServId(uid, serverTcpConnInfo.getServerNodeInfo().getServiceId());
        }
        Channel selectedChannel = serverTcpConnInfo.roubinChannel();
        saveChannel(uid, serverType, selectedChannel);
        return selectedChannel;
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

    private void saveChannel(long uid, int serverType, Channel channel) {
        RouterPlayerInterTransferInfo routerPlayerInterTransferInfo = routerPlayerInterTransferInfoMap.get(uid);//computeIfAbsent(uid, (value) -> new RouterPlayerInterTransferInfo());
        if (routerPlayerInterTransferInfo == null) {
            routerPlayerInterTransferInfo = new RouterPlayerInterTransferInfo();
            routerPlayerInterTransferInfoMap.put(uid, routerPlayerInterTransferInfo);
        }
        routerPlayerInterTransferInfo.setContext(serverType, channel);
    }
}
