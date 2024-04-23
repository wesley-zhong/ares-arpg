package com.ares.game.network;

import com.ares.common.bean.ServerType;
import com.ares.core.bean.AresMsgIdMethod;
import com.ares.core.bean.AresPacket;
import com.ares.core.service.ServiceMgr;
import com.ares.core.tcp.AresServerTcpHandler;
import com.ares.core.tcp.AresTKcpContext;
import com.ares.core.thread.AresThreadPool;
import com.ares.core.thread.LogicThreadPoolGroup;
import com.ares.core.thread.VirtualThreadPool;
import com.ares.discovery.DiscoveryService;
import com.ares.game.configuration.ThreadPoolType;
import com.ares.game.configuration.VirtualThreadPoolType;
import com.ares.game.service.PlayerRoleService;
import com.ares.game.service.PlayerSceneMap;
import com.ares.transport.bean.ServerNodeInfo;
import com.ares.transport.bean.TcpConnServerInfo;
import com.game.protoGen.ProtoCommon;
import com.game.protoGen.ProtoInner;
import io.netty.buffer.ByteBufInputStream;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

@Slf4j
public class GameMsgHandler implements AresServerTcpHandler {
    @Autowired
    protected ServiceMgr serviceMgr;
    @Autowired
    private DiscoveryService discoveryService;

    @Autowired
    private PeerConn peerConn;

    @Autowired
    private PlayerRoleService playerRoleService;
    @Autowired
    private PlayerSceneMap playerSceneMap;


    protected static final String UTF8 = "UTF-8";

    @Override
    public void handleMsgRcv(AresTKcpContext aresTKcpContext) throws IOException {
        AresPacket aresPacket = aresTKcpContext.getRcvPackage();
        ProtoCommon.MsgHeader msgHeader = aresPacket.getRecvHeader();
        AresMsgIdMethod calledMethod = serviceMgr.getCalledMethod(aresPacket.getMsgId());
        long uid = msgHeader.getUid();
        // no msg method call should proxy to others
        if (calledMethod == null) {
            int fromServerType = fromServerType(aresTKcpContext);
            if (fromServerType == ServerType.GATEWAY.getValue()) {
                peerConn.redirectRouterToTeam(uid, aresPacket);
                return;
            } //this should be from router server
            if (fromServerType == ServerType.ROUTER.getValue()) {
                peerConn.redirectToGateway(uid, aresPacket);
                return;
            }
            log.error("XXX error from={}  msgHeader ={}", aresPacket, msgHeader);
            return;
        }
        int length = aresPacket.getRecvByteBuf().readableBytes();
        Object paraObj = calledMethod.getParser().parseFrom(new ByteBufInputStream(aresPacket.getRecvByteBuf(), length));

        // player login with multi thread
        if (msgHeader.getMsgId() == ProtoInner.InnerMsgId.INNER_TO_GAME_LOGIN_REQ_VALUE) {
            AresThreadPool logicProcessThreadPool = LogicThreadPoolGroup.INSTANCE.selectThreadPool(ThreadPoolType.PLAYER_LOGIN.getValue());
            logicProcessThreadPool.execute(uid, aresTKcpContext, calledMethod, uid, paraObj);
            return;
        }
        //按player 所在的scene 分线程处理
      //  AresThreadPool logicProcessThreadPool = LogicThreadPoolGroup.INSTANCE.INSTANCE.selectVirtualThreadPool(VirtualThreadPoolType.LOGIC.getValue());
        AresThreadPool logicProcessThreadPool = LogicThreadPoolGroup.INSTANCE.INSTANCE.selectThreadPool(ThreadPoolType.LOGIC.getValue());
        long playerSceneId = playerSceneMap.getPlayerSceneId(uid);
        logicProcessThreadPool.execute(uid, aresTKcpContext, calledMethod, uid, paraObj);
    }

    private int fromServerType(AresTKcpContext aresTKcpContext) {
        TcpConnServerInfo tcpConnServerInfo = (TcpConnServerInfo) aresTKcpContext.getCacheObj();
        return tcpConnServerInfo.getServerNodeInfo().getServerType();
    }

    @Override
    public void onServerConnected(Channel aresTKcpContext) {
        ServerNodeInfo myselfNodeInfo = discoveryService.getEtcdRegister().getMyselfNodeInfo();
        ProtoInner.InnerServerHandShakeReq handleShake = ProtoInner.InnerServerHandShakeReq.newBuilder()
                .setServiceId(myselfNodeInfo.getServiceId())
                .setServiceName(myselfNodeInfo.getServiceName()).build();
        AresPacket aresPacket = AresPacket.create(ProtoInner.InnerMsgId.INNER_SERVER_HAND_SHAKE_REQ_VALUE, handleShake);
        aresTKcpContext.writeAndFlush(aresPacket);
        log.info("###### send handshake send to {}  msg: {}", aresTKcpContext, handleShake);
    }

    //work as server when client connected  me (this server)
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
