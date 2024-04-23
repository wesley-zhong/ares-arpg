package com.ares.game.network;

import com.ares.common.bean.ServerType;
import com.ares.core.annotation.MsgId;
import com.ares.core.bean.AresPacket;
import com.ares.core.service.AresController;
import com.ares.core.tcp.AresTKcpContext;
import com.ares.core.utils.AresContextThreadLocal;
import com.ares.discovery.DiscoveryService;
import com.ares.game.discovery.OnDiscoveryWatchService;
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
    @MsgId(ProtoInner.InnerMsgId.INNER_SERVER_HAND_SHAKE_REQ_VALUE)
    public void innerHandShake(long id, ProtoInner.InnerServerHandShakeReq innerLoginRequest) {
        AresTKcpContext aresTKcpContext = AresContextThreadLocal.get();

        ServerNodeInfo serverNodeInfo = new ServerNodeInfo();//discoveryService.getEtcdDiscovery().getServerList().get(innerLoginRequest.getServiceId());
        ServerType serverType = ServerType.from(innerLoginRequest.getServiceName());
        serverNodeInfo.setServerType(serverType.getValue());
        serverNodeInfo.setServiceName(innerLoginRequest.getServiceName());
        serverNodeInfo.setServiceId(innerLoginRequest.getServiceId());

        TcpConnServerInfo tcpConnServerInfo = peerConn.addPeerConn(serverNodeInfo, aresTKcpContext.getCtx());
        aresTKcpContext.cacheObj(tcpConnServerInfo);

        log.info("#### receive innerHandShake :{} from: {}  finish", innerLoginRequest, aresTKcpContext);
        ServerNodeInfo myselfNodeInfo = discoveryService.getEtcdRegister().getMyselfNodeInfo();
        ProtoInner.InnerServerHandShakeRes response = ProtoInner.InnerServerHandShakeRes.newBuilder().setAreaId(areaId)
                .setServiceName(myselfNodeInfo.getServiceName())
                .setServiceId(myselfNodeInfo.getServiceId()).build();
        AresPacket aresPacket = AresPacket.create(ProtoInner.InnerMsgId.INNER_SERVER_HAND_SHAKE_RES_VALUE, response);
        aresTKcpContext.send(aresPacket);
    }

    // as client receive from my handshake msg
    @MsgId(ProtoInner.InnerMsgId.INNER_SERVER_HAND_SHAKE_RES_VALUE)
    public void innerHandShakeRes(long id, ProtoInner.InnerServerHandShakeRes innerServerHandShakeRes) {
        AresTKcpContext aresTKcpContext = AresContextThreadLocal.get();
        ServerNodeInfo serverNodeInfo = onDiscoveryWatch.getServerNodeInfo(innerServerHandShakeRes.getServiceId());
        if (serverNodeInfo == null) {
            log.error("XXXXXXXXXXXXXXXX service id ={} not found", innerServerHandShakeRes.getServiceId());
            return;
        }
        peerConn.addPeerConn(serverNodeInfo, aresTKcpContext.getCtx());
        log.info("####  innerHandShakeRes from: {}  Response :{}  finish", aresTKcpContext, innerServerHandShakeRes);

        //can be remove
        TcpConnServerInfo tcpConnServerInfo = peerConn.addPeerConn(serverNodeInfo, aresTKcpContext.getCtx());
        aresTKcpContext.cacheObj(tcpConnServerInfo);
    }
}
