package com.ares.team.network;


import com.ares.common.bean.ServerType;
import com.ares.transport.peer.PeerConnBase;
import com.google.protobuf.Message;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Component
@Slf4j
public class PeerConn extends PeerConnBase {
    private final Map<Long, ChannelHandlerContext> playerIdContext = new ConcurrentHashMap<>();

    public void routerToGame(long uid, int msgId, Message body) {
        routerTo(ServerType.GAME, uid, msgId, body);
    }

    @Override
    public ChannelHandlerContext loadBalance(int serverType, long uid, Map<String, ChannelHandlerContext> channelConMap) {
        //rewrite
        ChannelHandlerContext channelHandlerContext = playerIdContext.get(uid);
        if (channelHandlerContext == null || !channelHandlerContext.channel().isActive()) {
            if (CollectionUtils.isEmpty(channelConMap)) {
                log.error(" loadBalance serverType ={} uid ={} not found", serverType, uid);
                return null;
            }
            ChannelHandlerContext routerContext = channelConMap.values().iterator().next();
            playerIdContext.put(uid, routerContext);
            return routerContext;
        }
        return channelConMap.values().iterator().next();
    }
}
