package com.ares.gateway.network;

import com.ares.core.bean.AresMsgIdMethod;
import com.ares.core.bean.AresPacket;
import com.ares.core.service.ServiceMgr;
import com.ares.core.tcp.AresClientTcpHandler;
import com.ares.core.tcp.AresTKcpContext;
import com.ares.core.thread.AresThreadPool;
import com.ares.core.thread.LogicThreadPoolGroup;
import com.ares.discovery.DiscoveryService;
import com.ares.gateway.configuration.ThreadPoolType;
import com.ares.gateway.service.SessionService;
import com.ares.transport.bean.ServerNodeInfo;
import com.ares.transport.bean.TcpConnServerInfo;
import com.ares.transport.client.AresTcpClient;
import com.game.protoGen.ProtoCommon;
import com.game.protoGen.ProtoInner;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.CompositeByteBuf;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

@Slf4j
public class InnerMsgHandler implements AresClientTcpHandler {
    @Autowired
    private ServiceMgr serviceMgr;
    @Autowired
    private AresTcpClient aresTcpClient;
    @Autowired
    private PeerConn peerConn;

    @Autowired
    private SessionService sessionService;
    @Autowired
    private DiscoveryService discoveryService;

    @Override
    public void handleMsgRcv(AresTKcpContext aresTKcpContext) throws IOException {
        AresPacket aresPacket = aresTKcpContext.getRcvPackage();
        ProtoCommon.MsgHeader msgHeader = aresPacket.getRecvHeader();
        int msgId = msgHeader.getMsgId();
        AresMsgIdMethod calledMethod = serviceMgr.getCalledMethod(msgId);
        long uid = msgHeader.getUid();
        if (calledMethod != null) {
            int length = aresPacket.getRecvByteBuf().readableBytes();
            Object paraObj = calledMethod.getParser().parseFrom(new ByteBufInputStream(aresPacket.getRecvByteBuf(), length));
            AresThreadPool logicProcessThreadPool = LogicThreadPoolGroup.INSTANCE.selectThreadPool(ThreadPoolType.LOGIN.getValue());
            logicProcessThreadPool.execute(uid, aresTKcpContext, calledMethod, uid, paraObj);
            return;
        }
        directSendToClient(uid, aresPacket);
    }

    @Override
    public void onClientConnected(AresTKcpContext aresTKcpContext) {

    }

    @Override
    public void onClientClosed(AresTKcpContext aresTKcpContext) {
    }

    @Override
    public boolean isChannelValidate(AresTKcpContext aresTKcpContext) {
        return false;
    }

    @Override
    public void onServerConnected(Channel aresTKcpContext) {
        ServerNodeInfo myNodeInfo = discoveryService.getEtcdRegister().getMyselfNodeInfo();
        ProtoInner.InnerServerHandShakeReq handleShake = ProtoInner.InnerServerHandShakeReq.newBuilder()
                .setServiceName(myNodeInfo.getServiceId()).build();

        ProtoCommon.MsgHeader header = ProtoCommon.MsgHeader.newBuilder()
                .setMsgId(ProtoInner.InnerMsgId.INNER_SERVER_HAND_SHAKE_REQ_VALUE).build();
        AresPacket aresPacket = AresPacket.create(header, handleShake);
        aresTKcpContext.writeAndFlush(aresPacket);
        log.info("######  handshake send to {}  msg: {}", aresTKcpContext, handleShake);
    }

    @Override
    public void onServerClosed(AresTKcpContext aresTKcpContext) {

    }


    private void directSendToClient(long uid, AresPacket aresPacket) {
        ProtoCommon.MsgHeader innerMsgHeader = aresPacket.getRecvHeader().toBuilder().setUid(0).build();
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

        sessionService.sendPlayerMsg(uid, byteBufs);
        //  log.info("------- direct send to client msg roleId ={} msgId={}", roleId, aresPacket.getMsgId());
    }
}
