package com.ares.gateway.network;

import com.ares.common.bean.ServerType;
import com.ares.core.bean.AresMsgIdMethod;
import com.ares.core.bean.AresPacket;
import com.ares.core.service.ServiceMgr;
import com.ares.core.tcp.AresServerTcpHandler;
import com.ares.core.tcp.AresTKcpContext;
import com.ares.core.thread.AresThreadPool;
import com.ares.core.thread.LogicThreadPoolGroup;
import com.ares.gateway.bean.PlayerSession;
import com.ares.gateway.configuration.ThreadPoolType;
import com.ares.gateway.service.SessionService;
import com.game.protoGen.ProtoCommon;
import com.game.protoGen.ProtoInner;
import io.netty.buffer.ByteBufInputStream;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;


@Slf4j
public class GateWayMsgHandler implements AresServerTcpHandler {
    @Autowired
    private ServiceMgr serviceMgr;
    @Autowired
    private PeerConn peerConn;
    @Autowired
    private SessionService sessionService;

    private volatile long start = System.currentTimeMillis();
    private AtomicInteger atomicInteger = new AtomicInteger(0);

    @Override
    public void handleMsgRcv(AresTKcpContext aresTKcpContext) throws IOException {
        PlayerSession playerSession = playerSessionFromClient(aresTKcpContext);
        AresPacket aresPacket = aresTKcpContext.getRcvPackage();
        ProtoCommon.MsgHeader msgHeader = aresPacket.getRecvHeader();
        AresMsgIdMethod calledMethod = serviceMgr.getCalledMethod(msgHeader.getMsgId());
        //process by myself
        if (calledMethod != null) {
            int length = aresPacket.getRecvByteBuf().readableBytes();
            long uid = 0;
            if (playerSession != null) {
                uid = playerSession.getUid();
            }
            //player first connect
            long hashCode = uid;
            if (hashCode == 0) {
                hashCode = Math.abs(aresTKcpContext.getCtx().channel().id().asShortText().hashCode());
            }
            Object paraObj = calledMethod.getParser().parseFrom(new ByteBufInputStream(aresPacket.getRecvByteBuf(), length));
            AresThreadPool logicProcessThreadPool = LogicThreadPoolGroup.INSTANCE.selectThreadPool(ThreadPoolType.IO.getValue());
            logicProcessThreadPool.execute(hashCode, aresTKcpContext, calledMethod, uid, paraObj);
            return;
        }
        peerConn.innerRedirectTo(ServerType.GAME, playerSession.getUid(), aresPacket);
    }


    private PlayerSession playerSessionFromClient(AresTKcpContext aresTKcpContext) {
        if (aresTKcpContext.getCacheObj() instanceof PlayerSession playerSession) {
            return playerSession;
        }
        return null;
    }


    //connect to the server call back
    @Override
    public void onServerConnected(Channel aresTKcpContext) {
    }

    @Override
    public void onClientConnected(AresTKcpContext aresTKcpContext) {
        log.info("---onClientConnected ={} ", aresTKcpContext);
    }

    @Override
    public void onClientClosed(AresTKcpContext aresTKcpContext) {
        log.info("-----onClientClosed={} ", aresTKcpContext);
        Object cacheObj = aresTKcpContext.getCacheObj();
        if (cacheObj instanceof PlayerSession playerSession) {
            ProtoInner.InnerPlayerDisconnectRequest disconnectRequest = ProtoInner.InnerPlayerDisconnectRequest.newBuilder()
                    .setUid(playerSession.getUid()).build();
            peerConn.sendToGameMsg(playerSession.getUid(), ProtoInner.InnerMsgId.INNER_PLAYER_DISCONNECT_REQ_VALUE, disconnectRequest);
            AresThreadPool logicProcessThreadPool = LogicThreadPoolGroup.INSTANCE.selectThreadPool(ThreadPoolType.IO.getValue());
            logicProcessThreadPool.execute(playerSession.getUid(), playerSession, sessionService::playerDisconnect);
        }
    }

    @Override
    public boolean isChannelValidate(AresTKcpContext aresTKcpContext) {
        return true;
    }


    @Override
    public void onServerClosed(Channel aresTKcpContext) {

    }
}
