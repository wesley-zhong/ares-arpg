package com.router.network;


import com.ares.common.bean.ServerType;
import com.ares.core.bean.AresPacket;
import com.ares.dal.game.UserOnlineService;
import com.ares.dal.game.UserOnlineStateDO;
import com.ares.transport.bean.ServerNodeInfo;
import com.ares.transport.peer.PeerConnBase;
import com.router.discovery.OnDiscoveryWatchService;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Map;


@Component
@Slf4j
public class PeerConn extends PeerConnBase {
    @Autowired
    private UserOnlineService userOnlineService;
    @Autowired
    private OnDiscoveryWatchService onDiscoveryWatchService;
    //   private final Map<Long, RouterPlayerInterTransferInfo> routerPlayerInterTransferInfoMap = new ConcurrentHashMap<>();


    public void sendToTeam(long uid, AresPacket aresPacket) {
        innerRedirectTo(ServerType.TEAM, uid, aresPacket);
    }


    public void sendToGame(long uid, AresPacket aresPacket) {
        innerRedirectTo(ServerType.GAME, uid, aresPacket);
    }


    @Override
    public ChannelHandlerContext loadBalance(int serverType, long uid, Map<String, ChannelHandlerContext> channelConMap) {
        if (CollectionUtils.isEmpty(channelConMap)) {
            log.error("loadBalance  uid ={}  serverType={} not found ", uid, serverType);
            return null;
        }
        UserOnlineStateDO userOnlineStateDO = userOnlineService.getUserOnlineStateDO(uid);
        if (userOnlineStateDO == null) {
            log.error("error=====, loadBalance  uid={}", uid);
            return null;
        }

        String targetServId = userOnlineStateDO.getGsrId();
        if (serverType == ServerType.TEAM.getValue()) {
            targetServId = userOnlineStateDO.getTsrId();
        }
        ChannelHandlerContext channelHandlerContext = channelConMap.get(targetServId);
        if (channelHandlerContext == null
                || !channelHandlerContext.channel().isActive()) {
            ServerNodeInfo lowerLoadServerNodeInfo = onDiscoveryWatchService.getLowerLoadServerNodeInfo(serverType);
            userOnlineStateDO.setServerId(lowerLoadServerNodeInfo.getServiceId(), serverType);
            userOnlineService.saveUserOnlineDo(userOnlineStateDO);
            return channelConMap.get(lowerLoadServerNodeInfo.getServiceId());
        }
        return channelHandlerContext;
    }
}
