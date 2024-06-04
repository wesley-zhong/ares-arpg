package com.ares.transport.peer;

import com.ares.common.bean.ServerType;
import com.ares.core.annotation.MsgId;
import com.ares.core.bean.AresPacket;
import com.ares.core.service.AresController;
import com.ares.core.tcp.AresTKcpContext;
import com.ares.core.utils.AresContextThreadLocal;
import com.ares.transport.bean.ServerNodeInfo;
import com.ares.transport.bean.TcpConnServerInfo;
import com.ares.transport.client.AresTcpClient;
import com.game.protoGen.ProtoInner;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;


@Slf4j
public abstract class InnerHandShake implements AresController {
    @Autowired
    private PeerConnBase peerConn;

    @Autowired
    private AresTcpClient aresTcpClient;


    //as sever receive client handshake
    @MsgId(ProtoInner.InnerMsgId.INNER_SERVER_HAND_SHAKE_REQ_VALUE)
    public void innerHandShake(long id, ProtoInner.InnerServerHandShakeReq innerLoginRequest) {
        AresTKcpContext aresTKcpContext = AresContextThreadLocal.get();
        ServerNodeInfo serverNodeInfo = new ServerNodeInfo();
        ServerType serverType = ServerType.from(innerLoginRequest.getServiceName());
        serverNodeInfo.setServerType(serverType.getValue());
        serverNodeInfo.setServiceName(innerLoginRequest.getServiceName());
        serverNodeInfo.setServiceId(innerLoginRequest.getServiceId());
        serverNodeInfo.setId(innerLoginRequest.getId());

        TcpConnServerInfo tcpConnServerInfo = peerConn.addPeerConn(serverNodeInfo, aresTKcpContext.getCtx());
        aresTKcpContext.cacheObj(tcpConnServerInfo);

        log.info("#### receive innerHandShake :{} from: {}  finish", innerLoginRequest, aresTKcpContext);
        ServerNodeInfo myselfNodeInfo = getMyselfNodeInfo();//discoveryService.getEtcdRegister().getMyselfNodeInfo();
        ProtoInner.InnerServerHandShakeRes response = ProtoInner.InnerServerHandShakeRes.newBuilder()
                .setServiceName(myselfNodeInfo.getServiceName())
                .setServiceId(myselfNodeInfo.getServiceId()).build();
        AresPacket aresPacket = AresPacket.create(ProtoInner.InnerMsgId.INNER_SERVER_HAND_SHAKE_RES_VALUE, response);
        aresTKcpContext.send(aresPacket);
    }

    // as client receive from my handshake msg
    @MsgId(ProtoInner.InnerMsgId.INNER_SERVER_HAND_SHAKE_RES_VALUE)
    public void innerHandShakeRes(long id, ProtoInner.InnerServerHandShakeRes innerServerHandShakeRes) {
        AresTKcpContext aresTKcpContext = AresContextThreadLocal.get();
        TcpConnServerInfo tcpConnServerInfo = aresTcpClient.getTcpConnServerInfo(innerServerHandShakeRes.getServiceName(), innerServerHandShakeRes.getServiceId());
        if (tcpConnServerInfo == null) {
            log.error("server connect error  service name ={} serviceName ={}", innerServerHandShakeRes.getServiceId(), innerServerHandShakeRes.getServiceName());
            return;
        }
        log.info("#### receive innerHandShake ShakeRes :{} from: {}  finish", innerServerHandShakeRes, aresTKcpContext);
        tcpConnServerInfo.addTcpConn(aresTKcpContext.getCtx().channel());
        peerConn.addPeerConn(tcpConnServerInfo);
        aresTKcpContext.cacheObj(tcpConnServerInfo);
    }

    abstract protected ServerNodeInfo getMyselfNodeInfo();
}
