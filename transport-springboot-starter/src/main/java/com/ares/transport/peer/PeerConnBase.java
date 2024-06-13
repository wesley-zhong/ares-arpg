package com.ares.transport.peer;

import com.ares.common.bean.ServerType;
import com.ares.core.bean.AresPacket;
import com.ares.transport.bean.ServerNodeInfo;
import com.ares.transport.bean.TcpConnServerInfo;
import com.game.protoGen.ProtoCommon;
import com.google.protobuf.Message;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.CompositeByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class PeerConnBase {
    /**
     * Integer key : server type
     * every server process has one connection, there may be many server processes with the same server type
     * String key : service_Id
     */
    private PeerMgr peerMgr = new PeerMgr();

    protected TcpConnServerInfo getTcpConnServerInfo(ServerNodeInfo serverNodeInfo) {
        return peerMgr.getServerTcpConnInfo(serverNodeInfo.getServiceId());
    }

    protected TcpConnServerInfo getServerTcpConnInfo(String serviceId) {
        if (serviceId == null) {
            return null;
        }
        return peerMgr.getServerTcpConnInfo(serviceId);
    }

    public abstract Channel loadBalance(int serverType, long uid);

    public void send(ServerType serverType, long uid, int msgId, Message body) {
        send(serverType.getValue(), uid, msgId, body);
    }

    public void send(String serviceId, long uid, int msgId, Message body) {
        TcpConnServerInfo serverTcpConnInfo = getServerTcpConnInfo(serviceId);
        if (serverTcpConnInfo == null) {
            log.error("service = {} not found1", serviceId);
            return;
        }
        Channel channel = serverTcpConnInfo.hashChannel(uid);
        if (channel == null) {
            log.error("service = {} not found2", serviceId);
            return;
        }
        send(channel, uid, msgId, body);
    }


    public void send(ServerType serverType, long uid, int msgId, int errCode) {
        send(serverType.getValue(), uid, msgId, errCode);
    }

    public void innerRedirectTo(ServerType serverType, long uid, AresPacket aresPacket) {
        innerRedirectTo(serverType.getValue(), uid, aresPacket);
    }

    public void innerRedirectTo(ServerType serverType, long uid, ByteBuf byteBuf) {
        innerRedirectTo(serverType.getValue(), uid, byteBuf);
    }

    public TcpConnServerInfo addPeerConn(ServerNodeInfo serverNodeInfo, ChannelHandlerContext context) {
        return peerMgr.addPeerConn(serverNodeInfo, context);
    }

    public TcpConnServerInfo addPeerConn(TcpConnServerInfo tcpConnServerInfo) {
        return peerMgr.addPeerConn(tcpConnServerInfo);
    }

    public TcpConnServerInfo getServerConnByServerInfo(ServerNodeInfo serverNodeInfo) {
        return peerMgr.getServerTcpConnInfo(serverNodeInfo);
    }

    public TcpConnServerInfo getServerConnByServiceId(String serviceId) {
        if (serviceId == null) {
            return null;
        }
        return peerMgr.getServerTcpConnInfo(serviceId);
    }

    public TcpConnServerInfo getServerConnByServiceId(int serverType, int serverId) {
        return peerMgr.getServerTcpConnInfo(serverType, serverId);
    }

    public void delete(ServerNodeInfo serverNodeInfo) {
        peerMgr.delete(serverNodeInfo);
    }

    private void send(int serverType, long uid, int msgId, Message body) {
        Channel channelHandlerContext = loadBalance(serverType, uid);
        if (channelHandlerContext == null) {
            log.error("===== error  serverType ={} no connection  sendMsgId ={} uid ={}", serverType, msgId, uid);
            return;
        }
        send(channelHandlerContext, uid, msgId, body);
    }

    public void send(Channel channel, long uid, int msgId, Message body) {
        ProtoCommon.MsgHeader header = ProtoCommon.MsgHeader.newBuilder()
                .setMsgId(msgId)
                .setUid(uid).build();
        AresPacket aresPacket = AresPacket.create(header, body);
        channel.writeAndFlush(aresPacket);
    }

    public void send(int serverType, int serverId, long uid, int msgId, Message body) {
        TcpConnServerInfo tcpConnServerInfo = getServerConnByServiceId(serverType, serverId);
        if (tcpConnServerInfo == null) {
            log.error("serverId = {} not connected", serverId);
            return;
        }
        Channel channel = tcpConnServerInfo.hashChannel(uid);
        send(channel, uid, msgId, body);
    }

    private void send(int serverType, long uid, int msgId, int errCode) {
        Channel channelHandlerContext = loadBalance(serverType, uid);
        if (channelHandlerContext == null) {
            log.error("=====  error  serverType ={} no connection  sendMsgId ={} uid ={}", serverType, msgId, uid);
            return;
        }
        ProtoCommon.MsgHeader header = ProtoCommon.MsgHeader.newBuilder()
                .setMsgId(msgId)
                .setErrCode(errCode)
                .setUid(uid).build();
        AresPacket aresPacket = AresPacket.create(header, null);
        channelHandlerContext.writeAndFlush(aresPacket);
    }


    /**
     * send the msg to the router server to router to the serverType
     *
     * @param serverType to the target server
     * @param uid        uid
     * @param msgId      msgId
     * @param body       body
     */
    protected void routerTo(ServerType serverType, long uid, int msgId, Message body) {
        Channel channelHandlerContext = loadBalance(ServerType.ROUTER.getValue(), uid);
        if (channelHandlerContext == null) {
            log.error("=== ==error  serverType ={} no connection  sendMsgId ={} uid ={}", ServerType.ROUTER, msgId, uid);
            return;
        }
        ProtoCommon.MsgHeader header = ProtoCommon.MsgHeader.newBuilder()
                .setUid(uid)
                .setMsgId(msgId)
                .setRouterTo(serverType.getValue()).build();
        AresPacket aresPacket = AresPacket.create(header, body);
        channelHandlerContext.writeAndFlush(aresPacket);
    }

    protected void routeTo(ServerType serverType, long uid, int msgId, int errCode) {
        Channel channelHandlerContext = loadBalance(ServerType.ROUTER.getValue(), uid);
        if (channelHandlerContext == null) {
            log.error("=== =error  serverType ={} no connection  sendMsgId ={} uid ={}", ServerType.ROUTER, msgId, uid);
            return;
        }
        ProtoCommon.MsgHeader header = ProtoCommon.MsgHeader.newBuilder()
                .setUid(uid)
                .setMsgId(msgId)
                .setErrCode(errCode)
                .setRouterTo(serverType.getValue()).build();
        AresPacket aresPacket = AresPacket.create(header, null);
        channelHandlerContext.writeAndFlush(aresPacket);
    }

    private void innerRedirectTo(int serverType, long uid, AresPacket aresPacket) {
        Channel channelHandlerContext = loadBalance(serverType, uid);
        if (channelHandlerContext == null) {
            log.error("=====error  serverType ={} no connection  sendMsgId ={} uid ={}", serverType, aresPacket.getMsgId(), uid);
            return;
        }
        doInnerRedirectTo(serverType, channelHandlerContext, uid, aresPacket);
    }

    private void innerRedirectTo(int serverType, long uid, ByteBuf body) {
        Channel channelHandlerContext = loadBalance(serverType, uid);
        if (channelHandlerContext == null) {
            log.error("=====error  serverType ={} no connection  uid ={}", serverType, uid);
            return;
        }
        channelHandlerContext.writeAndFlush(body);
    }

    //This may be overwritten by gateway  only called in io thread
    //note: this function only be called directly by the io thread (receive io thread )
    protected void doInnerRedirectTo(int serverType, Channel channelHandlerContext, long uid, AresPacket aresPacket) {
        ProtoCommon.MsgHeader innerMsgHeader = aresPacket.getRecvHeader().toBuilder().setUid(uid).build();
        //|body|
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
}
