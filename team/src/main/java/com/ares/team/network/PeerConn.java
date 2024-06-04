package com.ares.team.network;


import com.ares.common.bean.ServerType;
import com.ares.discovery.DiscoveryService;
import com.ares.team.discovery.OnDiscoveryWatchService;
import com.ares.transport.bean.ServerNodeInfo;
import com.ares.transport.peer.PeerConnBase;
import com.google.protobuf.Message;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Component
@Slf4j
public class PeerConn extends PeerConnBase {
    @Autowired
    private OnDiscoveryWatchService onDiscoveryWatchService;
    private final Map<Long, ChannelHandlerContext> playerIdContext = new ConcurrentHashMap<>();

    public void recordPlayerRouterContext(long uid, ChannelHandlerContext channelHandlerContext){
        playerIdContext.put(uid, channelHandlerContext);
    }

    public void routerToGame(long uid, int msgId, Message body) {
        routerTo(ServerType.GAME, uid, msgId, body);
    }

    public void routerErrToGame(long uid, int msgId, int errCode) {
        routeTo(ServerType.GAME, uid, msgId, errCode);
    }

    @Override
    public Channel loadBalance(int serverType, long uid) {
        ChannelHandlerContext channelHandlerContext = playerIdContext.get(uid);
        if (channelHandlerContext != null) {
            return channelHandlerContext.channel();
        }
        return  null;

  //      ServerNodeInfo serverNodeInfo = discoveryService.getEtcdDiscovery().getServerList().get(serverType);
        //rewrite

//        if (channelHandlerContext == null || !channelHandlerContext.channel().isActive()) {
//            if (CollectionUtils.isEmpty(channelConMap)) {
//                log.error(" loadBalance serverType ={} uid ={} not found", serverType, uid);
//                return null;
//            }
//            ChannelHandlerContext routerContext = channelConMap.values().iterator().next();
//            playerIdContext.put(uid, routerContext);
//            return routerContext;
//        }
        //  return channelConMap.values().iterator().next();
    }
}
