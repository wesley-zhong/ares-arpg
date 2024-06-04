package com.ares.login.network;

import com.ares.common.bean.ServerType;
import com.ares.core.bean.AresMsgIdMethod;
import com.ares.core.bean.AresPacket;
import com.ares.core.service.ServiceMgr;
import com.ares.core.tcp.AresTKcpContext;
import com.ares.core.tcp.AresTcpHandler;
import com.ares.core.thread.AresThreadPool;
import com.ares.core.thread.LogicProcessThreadPool;
import com.ares.core.thread.LogicThreadPoolGroup;
import com.ares.login.configuration.ThreadPoolType;
import com.ares.transport.bean.ServerNodeInfo;
import com.ares.transport.bean.TcpConnServerInfo;
import com.game.protoGen.ProtoCommon;
import com.game.protoGen.ProtoInner;
import io.netty.buffer.ByteBufInputStream;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;


@Slf4j
public class LoginMsgHandler implements AresTcpHandler {
    @Autowired
    protected ServiceMgr serviceMgr;
    @Value("${spring.application.name}")
    private String appName;

    @Autowired
    private PeerConn peerConn;

    protected static final String UTF8 = "UTF-8";

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
        logicProcessThreadPool.execute(aresTKcpContext, calledMethod, msgHeader.getMsgId(), paraObj);
    }


    @Override
    public void onServerConnected(Channel aresTKcpContext) {
        ProtoInner.InnerServerHandShakeReq handleShake = ProtoInner.InnerServerHandShakeReq.newBuilder().setServiceName(appName).build();
        AresPacket aresPacket = AresPacket.create(ProtoInner.InnerMsgId.INNER_SERVER_HAND_SHAKE_REQ_VALUE, handleShake);
        aresTKcpContext.writeAndFlush(aresPacket);
        log.info("###### handshake send to {}  msg: {}", aresTKcpContext, handleShake);
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
    public void onServerClosed(AresTKcpContext aresTKcpContext) {
    }
}
