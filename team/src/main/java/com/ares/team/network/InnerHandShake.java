package com.ares.team.network;

import com.ares.common.bean.ServerType;
import com.ares.core.annotation.MsgId;
import com.ares.core.bean.AresPacket;
import com.ares.core.service.AresController;
import com.ares.core.tcp.AresTKcpContext;
import com.ares.core.utils.AresContextThreadLocal;
import com.ares.discovery.DiscoveryService;

import com.ares.team.discovery.OnDiscoveryWatchService;
import com.ares.transport.bean.ServerNodeInfo;
import com.ares.transport.bean.TcpConnServerInfo;
import com.ares.transport.client.AresTcpClient;
import com.game.protoGen.ProtoCommon;
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

    @Value("${area.id:0}")
    private int areaId;
    @Value("${spring.application.name}")
    private String appName;

    @Autowired
    private AresTcpClient aresTcpClient;
    @Autowired
    private DiscoveryService discoveryService;
    @Autowired
    private OnDiscoveryWatchService onDiscoveryWatch;


    //as sever receive client handshake
    @MsgId(ProtoInner.InnerProtoCode.INNER_SERVER_HAND_SHAKE_REQ_VALUE)
    public void innerHandShake(long id, ProtoInner.InnerServerHandShakeReq innerLoginRequest) {
        AresTKcpContext aresTKcpContext = AresContextThreadLocal.get();

        //peerConn.addContext(innerLo, innerLoginRequest.getServiceName(), aresTKcpContext);
        int serverType = ServerType.from(innerLoginRequest.getServiceName()).getValue();
        peerConn.addPeerConn(serverType, innerLoginRequest.getServiceId(), aresTKcpContext.getCtx());

        log.info("#### receive innerHandShake :{} from: {}  finish",innerLoginRequest, aresTKcpContext);
        ServerNodeInfo myselfNodeInfo = discoveryService.getEtcdRegister().getMyselfNodeInfo();
        ProtoInner.InnerServerHandShakeRes response = ProtoInner.InnerServerHandShakeRes.newBuilder().setAreaId(areaId)
                .setServiceName(myselfNodeInfo.getServiceName())
                .setServiceId(myselfNodeInfo.getServiceId()).build();
        ProtoCommon.MsgHeader header = ProtoCommon.MsgHeader.newBuilder()
                .setMsgId(ProtoInner.InnerProtoCode.INNER_SERVER_HAND_SHAKE_RES_VALUE).setUid(id).build();
        AresPacket aresPacket = AresPacket.create( header, response);
        aresTKcpContext.send(aresPacket);


        //for send
        ServerNodeInfo serverNodeInfo = new ServerNodeInfo();
        serverNodeInfo.setAreaId(innerLoginRequest.getAreaId());
        serverNodeInfo.setServiceName(innerLoginRequest.getServiceName());
        serverNodeInfo.setServiceId(innerLoginRequest.getServiceId());
        TcpConnServerInfo tcpConnServerInfo = new TcpConnServerInfo(aresTKcpContext.getCtx().channel(), serverNodeInfo);
        aresTKcpContext.cacheObj(tcpConnServerInfo);
    }

    // as client receive from my handshake msg
    @MsgId(ProtoInner.InnerProtoCode.INNER_SERVER_HAND_SHAKE_RES_VALUE)
    public void innerHandShakeRes(long id, ProtoInner.InnerServerHandShakeRes innerServerHandShakeRes) {
        AresTKcpContext aresTKcpContext = AresContextThreadLocal.get();
        ServerNodeInfo serverNodeInfo = onDiscoveryWatch.getServerNodeInfo(innerServerHandShakeRes.getServiceId());
        peerConn.addPeerConn(serverNodeInfo,aresTKcpContext.getCtx());
        log.info("####  innerHandShakeRes from: {}  Response :{}  finish", aresTKcpContext, innerServerHandShakeRes);

        //can be remove
        TcpConnServerInfo tcpConnServerInfo = aresTcpClient.getTcpConnServerInfo(innerServerHandShakeRes.getAreaId(), innerServerHandShakeRes.getServiceName());
        if (tcpConnServerInfo == null) {
            log.error("server connect error  service name ={} areaId ={}", innerServerHandShakeRes.getServiceName(), innerServerHandShakeRes.getAreaId());
            return;
        }
        aresTKcpContext.cacheObj(tcpConnServerInfo);
    }
}
