package com.ares.gateway.network;


import com.ares.common.bean.ServerType;
import com.ares.core.bean.AresPacket;
import com.ares.gateway.discovery.OnDiscoveryWatchService;
import com.ares.transport.bean.ServerNodeInfo;
import com.ares.transport.peer.PeerConnBase;
import com.game.protoGen.ProtoCommon;
import com.google.protobuf.Message;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.CompositeByteBuf;
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

    public void sendToGameMsg(long uid, int msgId, Message body) {
        send(ServerType.GAME, uid, msgId, body);
    }

    public void redirectToGameMsg(long uid, AresPacket aresPacket) {
        innerRedirectTo(ServerType.GAME, uid, aresPacket);
    }

    @Override
    public ChannelHandlerContext loadBalance(int serverType, long uid, Map<String, ChannelHandlerContext> channelConMap) {
        ChannelHandlerContext channelHandlerContext = playerIdContext.get(uid);
        if (channelHandlerContext != null) {
            return channelHandlerContext;
        }
        ServerNodeInfo lowerLoadServerNodeInfo = onDiscoveryWatchService.getLowerLoadServerNodeInfo(serverType);
        ChannelHandlerContext context = getServerConnByServerInfo(lowerLoadServerNodeInfo);
        playerIdContext.put(uid, context);
        return context;
    }

    @Override
    protected void doInnerRedirectTo(int serveType, ChannelHandlerContext channelHandlerContext, long uid, AresPacket aresPacket) {
        ProtoCommon.MsgHeader innerMsgHeader = aresPacket.getRecvHeader().toBuilder().setUid(uid).build();
        //|body|
        int readableBytes = 0;
        if (aresPacket.getRecvByteBuf() != null) {
            readableBytes = aresPacket.getRecvByteBuf().readableBytes();
        }

        byte[] header = innerMsgHeader.toByteArray();
        //send body |msgLen->4|msgId->2|headerLen->2|headerBody|body
        //totalLen  do not include 4bytes msgLen
        int totalLen = 1 + readableBytes + header.length;

        CompositeByteBuf byteBufs = ByteBufAllocator.DEFAULT.compositeDirectBuffer();
        ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer(header.length + 1 + 4);
        buffer.writeInt(totalLen)
                .writeByte(header.length)
                .writeBytes(header);

        byteBufs.addComponents(true, buffer, aresPacket.getRecvByteBuf().retain());
        channelHandlerContext.writeAndFlush(byteBufs);
    }
}
