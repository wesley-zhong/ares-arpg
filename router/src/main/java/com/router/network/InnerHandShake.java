package com.router.network;

import com.ares.common.bean.ServerType;
import com.ares.core.annotation.MsgId;
import com.ares.core.bean.AresPacket;
import com.ares.core.service.AresController;
import com.ares.core.tcp.AresTKcpContext;
import com.ares.core.utils.AresContextThreadLocal;
import com.ares.discovery.DiscoveryService;
import com.ares.transport.bean.ServerNodeInfo;
import com.ares.transport.bean.TcpConnServerInfo;
import com.ares.transport.client.AresTcpClient;
import com.game.protoGen.ProtoInner;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


@Component
@Slf4j
public class InnerHandShake implements AresController {
    @Autowired
    private PeerConn peerConn;


    @Autowired
    private AresTcpClient aresTcpClient;
    @Autowired
    private DiscoveryService discoveryService;


    //as sever receive client handshake
    @MsgId(ProtoInner.InnerProtoCode.INNER_SERVER_HAND_SHAKE_REQ_VALUE)
    public void innerHandShake(long id, ProtoInner.InnerServerHandShakeReq innerLoginRequest) {
        ServerNodeInfo mySelfNode = discoveryService.getEtcdRegister().getMyselfNodeInfo();
        AresTKcpContext aresTKcpContext = AresContextThreadLocal.get();
        ServerType from = ServerType.from(innerLoginRequest.getServiceName());
        peerConn.addPeerConn(from.getValue(),  innerLoginRequest.getServiceId(), aresTKcpContext.getCtx());
        log.info("####  from: {} innerHandShake :{}  finish", aresTKcpContext, innerLoginRequest);
        ProtoInner.InnerServerHandShakeRes response = ProtoInner.InnerServerHandShakeRes.newBuilder()
                .setServiceId(mySelfNode.getServiceId())
                .setServiceName(mySelfNode.getServiceName()).build();
        AresPacket aresPacket = AresPacket.create(ProtoInner.InnerProtoCode.INNER_SERVER_HAND_SHAKE_RES_VALUE, response);
        aresTKcpContext.send(aresPacket);

        //to do rewrite
        ServerNodeInfo serverNodeInfo = new ServerNodeInfo();
        serverNodeInfo.setAreaId(innerLoginRequest.getAreaId());
        serverNodeInfo.setServiceName(innerLoginRequest.getServiceName());
        serverNodeInfo.setServiceId(innerLoginRequest.getServiceId());
        TcpConnServerInfo tcpConnServerInfo = new TcpConnServerInfo(aresTKcpContext.getCtx().channel(), serverNodeInfo);
        aresTKcpContext.cacheObj(tcpConnServerInfo);
    }
}
