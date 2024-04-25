package com.ares.login.network;

import com.ares.core.annotation.MsgId;
import com.ares.core.service.AresController;
import com.ares.core.tcp.AresTKcpContext;
import com.ares.core.utils.AresContextThreadLocal;
import com.ares.discovery.DiscoveryService;
import com.ares.login.discovery.OnDiscoveryWatchService;
import com.ares.transport.bean.ServerNodeInfo;
import com.ares.transport.bean.TcpConnServerInfo;
import com.ares.transport.client.AresTcpClient;
import com.game.protoGen.ProtoInner;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
    @Autowired
    private OnDiscoveryWatchService onDiscoveryWatch;


    //as sever receive client handshake
    @MsgId(ProtoInner.InnerMsgId.INNER_SERVER_HAND_SHAKE_RES_VALUE)
    public void innerHandShakeRes(long pid, ProtoInner.InnerServerHandShakeRes innerServerHandShakeRes) {
        AresTKcpContext aresTKcpContext = AresContextThreadLocal.get();

        ServerNodeInfo serverNodeInfo = onDiscoveryWatch.getServerNodeInfo(innerServerHandShakeRes.getServiceId());
        peerConn.addPeerConn(serverNodeInfo, aresTKcpContext.getCtx());

        log.info("#### innerHandShakeRes   Response :{}  finish  from: {}", innerServerHandShakeRes, aresTKcpContext);
        TcpConnServerInfo tcpConnServerInfo = aresTcpClient.getTcpConnServerInfo(innerServerHandShakeRes.getServiceName(), innerServerHandShakeRes.getServiceId());
        if (tcpConnServerInfo == null) {
            log.error("server connect error  service name ={} serviceName ={}", innerServerHandShakeRes.getServiceId(), innerServerHandShakeRes.getServiceName());
            return;
        }
        aresTKcpContext.cacheObj(tcpConnServerInfo);
    }
}
