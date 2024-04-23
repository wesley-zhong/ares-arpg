package com.ares.login.network;


import com.ares.common.bean.ServerType;
import com.ares.core.bean.AresPacket;
import com.ares.core.tcp.AresTKcpContext;
import com.ares.transport.peer.PeerConnBase;
import com.google.protobuf.Message;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.Map;


@Component
@Slf4j
public class PeerConn extends PeerConnBase {
    @Value("${area.id:0}")
    private int areaId;
    private final Map<Integer, Map<Integer, AresTKcpContext>> peerConns = new HashMap<>();

    public synchronized void addContext(int areaId, String serviceName, AresTKcpContext aresTKcpContext) {
        ServerType serverType = ServerType.from(serviceName);
        if (serverType == null) {
            log.error("service name == {} not be defined in ServerType enum", serviceName);
            return;
        }
        Map<Integer, AresTKcpContext> stringAresTcpContextMap = peerConns.get(areaId);
        if (stringAresTcpContextMap == null) {
            stringAresTcpContextMap = new HashMap<>();
            stringAresTcpContextMap.put(serverType.getValue(), aresTKcpContext);
            peerConns.put(areaId, stringAresTcpContextMap);
            return;
        }
        stringAresTcpContextMap.put(serverType.getValue(), aresTKcpContext);
    }


    public synchronized AresTKcpContext getAresTcpContext(ServerType serverType) {
        return getAresTcpContext(areaId, serverType);
    }

    public synchronized AresTKcpContext getAresTcpContext(int areaId, ServerType serverType) {
        Map<Integer, AresTKcpContext> stringAresTcpContextMap = peerConns.get(areaId);
        if (CollectionUtils.isEmpty(stringAresTcpContextMap)) {
            return null;
        }
        return stringAresTcpContextMap.get(serverType.getValue());
    }

    public void send(int areaId, ServerType serverType, long uid, int msgId, Message body) {
        AresTKcpContext channelHandlerContext = getAresTcpContext(areaId, serverType);
        if (channelHandlerContext == null) {
            log.error("areaId ={} sererType ={}  not found to send msgId ={}", areaId, serverType, msgId);
            return;
        }
        AresPacket aresPacket = AresPacket.create(msgId, body);
        channelHandlerContext.send(aresPacket);
    }


    public void routerToTeam(long uid, AresPacket aresPacket) {
        innerRedirectTo(ServerType.ROUTER, uid, aresPacket);
    }

    public void routerGame(long uid, AresPacket aresPacket) {
        innerRedirectTo(ServerType.GAME, uid, aresPacket);
    }

    @Override
    public Channel loadBalance(int serverType, long uid) {
        return null;
    }
}
