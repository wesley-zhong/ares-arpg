package com.ares.game.network;


import com.ares.common.bean.ServerType;
import com.ares.core.bean.AresPacket;
import com.ares.game.discovery.OnDiscoveryWatchService;
import com.ares.transport.bean.ServerNodeInfo;
import com.ares.transport.bean.TcpConnServerInfo;
import com.ares.transport.peer.PeerConnBase;
import com.game.protoGen.ProtoCommon;
import com.google.protobuf.Message;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.CompositeByteBuf;
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
    private final Map<Long, GamePlayerInterTransferInfo> playerIdContext = new ConcurrentHashMap<>();

    public long getPlayerThreadHash(long uid) {
        GamePlayerInterTransferInfo gamePlayerInterTransferInfo = playerIdContext.get(uid);
        if (gamePlayerInterTransferInfo == null) {
            return 0L;
        }
        return gamePlayerInterTransferInfo.getThreadHashCode();
    }

    //send msg to team by router server
    public void routerToTeam(long uid, int msgId, Message body) {
        routerTo(ServerType.TEAM, uid, msgId, body);
    }

    //send to msg to other game server by router server
    public void routerToOtherGame(long uid, int msgId, Message body) {
        routerTo(ServerType.GAME, uid, msgId, body);
    }

    //send msg to gateway
    public void sendGateWayMsg(long uid, int msgId, Message body) {
        send(ServerType.GATEWAY, uid, msgId, body);
    }

    public void sendGateWayErrorMsg(long uid, int msgId, int errCode) {
        send(ServerType.GATEWAY, uid, msgId, errCode);
    }

    public GamePlayerInterTransferInfo recordPlayerFromContext(ServerType serverType, long uid, long gSid, ChannelHandlerContext channelHandlerContext) {
        GamePlayerInterTransferInfo gamePlayerInterTransferInfo = playerIdContext.get(uid);
        if (gamePlayerInterTransferInfo == null) {
            gamePlayerInterTransferInfo = new GamePlayerInterTransferInfo();
            gamePlayerInterTransferInfo.setContext(serverType.getValue(), channelHandlerContext.channel());
            playerIdContext.put(uid, gamePlayerInterTransferInfo);
        }
        gamePlayerInterTransferInfo.setGateSid(gSid);
        gamePlayerInterTransferInfo.setContext(serverType.getValue(), channelHandlerContext.channel());
        return gamePlayerInterTransferInfo;
    }

    public GamePlayerInterTransferInfo getPLayerInnerTransInfo(long uid) {
        return playerIdContext.get(uid);
    }

    public GamePlayerInterTransferInfo getPlayerLastGatewayChannel(long uid) {
        GamePlayerInterTransferInfo gamePlayerInterTransferInfo = playerIdContext.get(uid);
        if (gamePlayerInterTransferInfo == null) {
            return null;
        }
        return gamePlayerInterTransferInfo;
    }

    @Override
    public Channel loadBalance(int serverType, long uid) {
        GamePlayerInterTransferInfo channelHandlerContext = playerIdContext.get(uid);
        if (channelHandlerContext != null) {
            Channel channel = channelHandlerContext.getContextByType(serverType);
            if (channel != null) {
                return channel;
            }
        }
        // channelHandlerContext it should be first create when player login
        // if not exist only one case
        if (channelHandlerContext == null) {
            log.warn(" server type={} uid = {} something error", serverType, uid);
            return null;
        }
        ServerNodeInfo lowerLoadServerNodeInfo = onDiscoveryWatchService.getLowerLoadServerNodeInfo(serverType);
        TcpConnServerInfo tcpConnServerInfo = getServerConnByServerInfo(lowerLoadServerNodeInfo);
        if (tcpConnServerInfo == null) {
            return null;
        }
        Channel roubinChannel = tcpConnServerInfo.roubinChannel();
        channelHandlerContext.setContext(serverType, roubinChannel);
        return roubinChannel;
    }

    /**
     * 从gateway 接收到的消息，直接转发 那么只有一个目的地  team server
     *
     * @param uid
     * @param aresPacket
     */

    //   the msg to team  from network io thread and then proxy the msg to router server and proxy it to the team server.
    //  this can not be called by logic
    public void redirectRouterToTeam(long uid, AresPacket aresPacket) {
        ProtoCommon.MsgHeader innerHeader = aresPacket.getRecvHeader().toBuilder()
                .setRouterTo(ServerType.TEAM.getValue())
                .setUid(uid).build();
        //|body|
        int readableBytes = 0;
        if (aresPacket.getRecvByteBuf() != null) {
            readableBytes = aresPacket.getRecvByteBuf().readableBytes();
        }

        byte[] header = innerHeader.toByteArray();
        int totalLen = 1 + readableBytes + header.length;

        CompositeByteBuf byteBufs = ByteBufAllocator.DEFAULT.compositeDirectBuffer();
        ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer(5 + header.length);
        buffer.writeInt(totalLen)
                .writeByte(header.length)
                .writeBytes(header);

        byteBufs.addComponents(true, buffer, aresPacket.getRecvByteBuf().retain());
        innerRedirectTo(ServerType.ROUTER, uid, byteBufs);
    }


    //  the msg to gateway  from network io thread proxy the msg to gateway then to send to client .
    // this can not be called by logic
    public void redirectToGateway(long uid, AresPacket aresPacket) {
        innerRedirectTo(ServerType.GATEWAY, uid, aresPacket);
    }
}
