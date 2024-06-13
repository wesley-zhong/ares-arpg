package com.ares.gateway.network;


import com.ares.common.bean.ServerType;
import com.ares.common.util.LRUCache;
import com.ares.core.bean.AresPacket;
import com.ares.gateway.discovery.OnDiscoveryWatchService;
import com.ares.transport.bean.ServerNodeInfo;
import com.ares.transport.bean.TcpConnServerInfo;
import com.ares.transport.peer.PeerConnBase;
import com.game.protoGen.ProtoCommon;
import com.google.protobuf.Message;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.CompositeByteBuf;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


@Component
@Slf4j
public class PeerConn extends PeerConnBase implements InitializingBean {
    @Autowired
    private OnDiscoveryWatchService onDiscoveryWatchService;
    @Value("${server.max-player-count:20000}")
    private int maxPlayerCount;
    private LRUCache<Long, Channel> playerPeerContext;

    public void sendToGameMsg(long uid, int msgId, Message body) {
        send(ServerType.GAME, uid, msgId, body);
    }

    public void recordPlayerPeerChannel(long uid, Channel channel) {
        playerPeerContext.put(uid, channel);
    }

    public void removePlayerConn(long uid) {
        playerPeerContext.remove(uid);
    }

    @Override
    public Channel loadBalance(int serverType, long uid) {
        Channel channelHandlerContext = playerPeerContext.get(uid);
        if (channelHandlerContext != null) {
            return channelHandlerContext;
        }
        if (channelHandlerContext.isActive()) {
            return channelHandlerContext;
        }

        ServerNodeInfo lowerLoadServerNodeInfo = onDiscoveryWatchService.getLowerLoadServerNodeInfo(serverType);
        TcpConnServerInfo tcpConnServerInfo = getServerConnByServerInfo(lowerLoadServerNodeInfo);
        if (tcpConnServerInfo == null) {
            return null;
        }

        Channel channel = tcpConnServerInfo.roubinChannel();
        playerPeerContext.put(uid, channel);
        return channel;
    }

    @Override
    protected void doInnerRedirectTo(int serveType, Channel channelHandlerContext, long uid, AresPacket aresPacket) {
        ProtoCommon.MsgHeader innerMsgHeader = aresPacket.getRecvHeader().toBuilder().setUid(uid).build();
        int readableBytes = 0;
        if (aresPacket.getRecvByteBuf() != null) {
            readableBytes = aresPacket.getRecvByteBuf().readableBytes();
        }

        byte[] header = innerMsgHeader.toByteArray();
        int totalLen = 1 + readableBytes + header.length;

        CompositeByteBuf byteBufs = ByteBufAllocator.DEFAULT.compositeDirectBuffer();
        ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer(header.length + 1 + 4);
        buffer.writeInt(totalLen)
                .writeByte(header.length)
                .writeBytes(header);

        byteBufs.addComponents(true, buffer, aresPacket.getRecvByteBuf());
        channelHandlerContext.writeAndFlush(byteBufs);
    }

    @Override
    public void afterPropertiesSet() {
        playerPeerContext = new LRUCache<>(maxPlayerCount);
    }
}
