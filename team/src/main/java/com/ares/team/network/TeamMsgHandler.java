package com.ares.team.network;

import com.ares.core.bean.AresMsgIdMethod;
import com.ares.core.bean.AresPacket;
import com.ares.core.service.ServiceMgr;
import com.ares.core.tcp.AresTKcpContext;
import com.ares.core.tcp.AresTcpHandler;
import com.ares.core.thread.AresThreadPool;
import com.ares.core.thread.LogicProcessThreadPool;
import com.ares.core.thread.LogicThreadPoolGroup;
import com.ares.discovery.DiscoveryService;
import com.ares.team.configuration.ThreadPoolType;
import com.ares.transport.bean.ServerNodeInfo;
import com.game.protoGen.ProtoCommon;
import com.game.protoGen.ProtoInner;
import io.netty.buffer.ByteBufInputStream;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;


@Slf4j
public class TeamMsgHandler implements AresTcpHandler {
    @Autowired
    private ServiceMgr serviceMgr;
    @Autowired
    private DiscoveryService discoveryService;

    @Override
    public void handleMsgRcv(AresTKcpContext aresTKcpContext) throws IOException {
        AresPacket aresPacket = aresTKcpContext.getRcvPackage();
        ProtoCommon.MsgHeader msgHeader = aresPacket.getRecvHeader();
        int msgId = msgHeader.getMsgId();
        AresMsgIdMethod calledMethod = serviceMgr.getCalledMethod(msgId);
        if (calledMethod == null) {
            log.error("msgId ={} not found call function", aresPacket.getMsgId());
            return;
        }
        int length = aresPacket.getRecvByteBuf().readableBytes();
        Object paraObj = calledMethod.getParser().parseFrom(new ByteBufInputStream(aresPacket.getRecvByteBuf(), length));
        AresThreadPool logicProcessThreadPool = LogicThreadPoolGroup.INSTANCE.selectThreadPool(ThreadPoolType.LOGIC.getValue());
        logicProcessThreadPool.execute(aresTKcpContext, calledMethod, msgHeader.getUid(), paraObj);
    }

    @Override
    public void onServerConnected(Channel aresTKcpContext) {
        ServerNodeInfo myselfNodeInfo = discoveryService.getEtcdRegister().getMyselfNodeInfo();
        ProtoInner.InnerServerHandShakeReq handleShake = ProtoInner.InnerServerHandShakeReq.newBuilder()
                .setServiceId(myselfNodeInfo.getServiceId())
                .setServiceName(myselfNodeInfo.getServiceName()).build();
        AresPacket aresPacket = AresPacket.create(ProtoInner.InnerMsgId.INNER_SERVER_HAND_SHAKE_REQ_VALUE, handleShake);
        aresTKcpContext.writeAndFlush(aresPacket);
        log.info("######  handshake send to {}  msg: {}", aresTKcpContext, handleShake);
    }


    @Override
    public void onClientConnected(AresTKcpContext aresTKcpContext) {
        log.info("---onClientConnected ={} ", aresTKcpContext);
    }

    @Override
    public void onClientClosed(AresTKcpContext aresTKcpContext) {
        log.info("-----onClientClosed={} ", aresTKcpContext);
    }

    @Override
    public boolean isChannelValidate(AresTKcpContext aresTKcpContext) {
        return true;
    }


    @Override
    public void onServerClosed(Channel aresTKcpContext) {

    }
}
