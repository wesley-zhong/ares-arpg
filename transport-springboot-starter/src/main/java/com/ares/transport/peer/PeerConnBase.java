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
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public abstract class PeerConnBase {
    /**
     * Integer key : server type
     * every server process has one connection, there may be many server processes with the same server type
     * String key : service_Id
     */
    private final Map<Integer, Map<String, TcpConnServerInfo>> serverTypeConnMap = new ConcurrentHashMap<>();

    protected TcpConnServerInfo getTcpConnServerInfo(ServerNodeInfo serverNodeInfo) {
        Map<String, TcpConnServerInfo> serviceIdTcpConnServerInfoMap = serverTypeConnMap.get(serverNodeInfo.getServerType());
        if (CollectionUtils.isEmpty(serviceIdTcpConnServerInfoMap)) {
            return null;
        }
        return serviceIdTcpConnServerInfoMap.get(serverNodeInfo.getServiceId());
    }

    protected TcpConnServerInfo getServerTcpConnInfo(int serverType, String serviceId){
        Map<String, TcpConnServerInfo> serviceIdTcpConnServerInfoMap = serverTypeConnMap.get(serverType);
        if(CollectionUtils.isEmpty(serviceIdTcpConnServerInfoMap)){
            return null;
        }
        return serviceIdTcpConnServerInfoMap.get(serviceId);
    }


    public abstract Channel loadBalance(int serverType, long uid);

    public void send(ServerType serverType, long uid, int msgId, Message body) {
        send(serverType.getValue(), uid, msgId, body);
    }

    public void send(ServerType serverType, long uid, int msgId, int errCode) {
        send(serverType.getValue(), uid, msgId, errCode);
    }

    public void innerRedirectTo(ServerType serverType, long uid, AresPacket aresPacket) {
        innerRedirectTo(serverType.getValue(), uid, aresPacket);
    }

//    public void innerRedirectTo(ServerType serverType, long uid, List<AresPacket> aresPackets) {
//        Map<String, ChannelHandlerContext> channelHandlerContextMap = serverTypeConnMap.get(serverType);
//        ChannelHandlerContext channelHandlerContext = loadBalance(serverType.getValue(), uid, channelHandlerContextMap);
//        if (channelHandlerContext == null) {
//            log.error("=====error  serverType ={} no connection  ={}", serverType, uid);
//            return;
//        }
//        for (AresPacket aresPacket : aresPackets) {
//            channelHandlerContext.write(aresPacket);
//        }
//        channelHandlerContext.flush();
//    }

    public void innerRedirectTo(ServerType serverType, long uid, ByteBuf byteBuf) {
        innerRedirectTo(serverType.getValue(), uid, byteBuf);
    }

    public TcpConnServerInfo addPeerConn(ServerNodeInfo serverNodeInfo, ChannelHandlerContext context) {
        return addPeerConn(serverNodeInfo.getServerType(), serverNodeInfo.getServiceId(), context);
    }

    public TcpConnServerInfo addPeerConn(TcpConnServerInfo tcpConnServerInfo) {
        ServerNodeInfo serverNodeInfo = tcpConnServerInfo.getServerNodeInfo();
        Map<String, TcpConnServerInfo> serviceIdConnMap = serverTypeConnMap.computeIfAbsent(serverNodeInfo.getServerType(), (key) -> new HashMap<>());
        if (serviceIdConnMap.containsKey(serverNodeInfo.getServiceId())) {
            return tcpConnServerInfo;
        }
        serviceIdConnMap.put(serverNodeInfo.getServiceId(), tcpConnServerInfo);
        return tcpConnServerInfo;
    }

    public TcpConnServerInfo addPeerConn(int serverType, String serviceId, ChannelHandlerContext context) {
        Map<String, TcpConnServerInfo> typeConnMap = serverTypeConnMap.computeIfAbsent(serverType, (key) -> new HashMap<>());
        TcpConnServerInfo tcpConnServerInfo = typeConnMap.computeIfAbsent(serviceId, (key) -> new TcpConnServerInfo());
        tcpConnServerInfo.addTcpConn(context.channel());
        return tcpConnServerInfo;
    }


    public TcpConnServerInfo getServerConnByServerInfo(ServerNodeInfo serverNodeInfo) {
        if (serverNodeInfo == null) {
            return null;
        }
        Map<String, TcpConnServerInfo> serverTypeConnMap = this.serverTypeConnMap.get(serverNodeInfo.getServerType());
        if (serverTypeConnMap == null) {
            return null;
        }
        return serverTypeConnMap.get(serverNodeInfo.getServiceId());
    }

    public void delete(ServerNodeInfo serverNodeInfo) {
        Map<String, TcpConnServerInfo> serverTypeConnMaps = serverTypeConnMap.get(serverNodeInfo.getServerType());
        if (serverTypeConnMaps == null) {
            log.error("serverNodeInfo ={} not found connection", serverNodeInfo);
            return;
        }
        TcpConnServerInfo tcpConnServerInfo = serverTypeConnMaps.remove(serverNodeInfo.getServiceId());
        if (tcpConnServerInfo == null) {
            log.error("serverNodeInfo ={} not found connection", serverNodeInfo);
            return;
        }
        tcpConnServerInfo.close();
        if (serverTypeConnMaps.isEmpty()) {
            serverTypeConnMap.remove(serverNodeInfo.getServerType());
        }
    }

    private void send(int serverType, long uid, int msgId, Message body) {
        Channel channelHandlerContext = loadBalance(serverType, uid);
        if (channelHandlerContext == null) {
            log.error("===== error  serverType ={} no connection  sendMsgId ={} uid ={}", serverType, msgId, uid);
            return;
        }
        ProtoCommon.MsgHeader header = ProtoCommon.MsgHeader.newBuilder()
                .setMsgId(msgId)
                .setUid(uid).build();
        AresPacket aresPacket = AresPacket.create(header, body);
        channelHandlerContext.writeAndFlush(aresPacket);
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

        byteBufs.addComponents(true, buffer, aresPacket.getRecvByteBuf().retain());
        channelHandlerContext.writeAndFlush(byteBufs);
    }
}
