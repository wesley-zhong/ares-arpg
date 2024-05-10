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
import com.ares.gateway.localservice.PlayerLocalService;
import com.ares.gateway.service.SessionService;
import com.game.protoGen.ProtoCommon;
import com.game.protoGen.ProtoGame;
import com.game.protoGen.ProtoInner;
import io.netty.buffer.ByteBufInputStream;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

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

    @Value("${useCoroutine:false}")
    private boolean useCoroutine;

    private volatile long start = System.currentTimeMillis();
    private AtomicInteger atomicInteger = new AtomicInteger(0);

    @Override
    public boolean isChannelValidate(AresTKcpContext aresTKcpContext) {
        PlayerSession playerSession = playerSessionFromClient(aresTKcpContext);
        AresPacket aresPacket = aresTKcpContext.getRcvPackage();
        ProtoCommon.MsgHeader msgHeader = aresPacket.getRecvHeader();
        if (msgHeader.getMsgId() != ProtoCommon.MsgId.GAME_LOGIN_REQ_VALUE) {
            if (playerSession == null || !playerSession.isValid()) {
                log.error("msgId ={} not valid from ={}", aresTKcpContext.getCtx(), msgHeader.getMsgId());
                aresTKcpContext.close();
                return false;
            }
        }
        return true;
    }

    @Override
    public void handleMsgRcv(AresTKcpContext aresTKcpContext) throws IOException {
        PlayerSession playerSession = playerSessionFromClient(aresTKcpContext);
        AresPacket aresPacket = aresTKcpContext.getRcvPackage();
        ProtoCommon.MsgHeader msgHeader = aresPacket.getRecvHeader();
        AresMsgIdMethod calledMethod = serviceMgr.getCalledMethod(msgHeader.getMsgId());
        //process by myself
        if (calledMethod != null) {
            int length = aresPacket.getRecvByteBuf().readableBytes();
            long uid = 0L;
            if (playerSession != null) {
                uid = playerSession.getUid();
            }
            Object paraObj = calledMethod.getParser().parseFrom(new ByteBufInputStream(aresPacket.getRecvByteBuf(), length));
            if (useCoroutine) {
                PlayerLocalService.getInstance().processNetworkMessage(uid, aresTKcpContext, calledMethod, uid, paraObj, msgHeader);
                return;
            }
            if (uid == 0) {
                if (paraObj instanceof ProtoGame.GameLoginReq gameLoginReq) {
                    uid = gameLoginReq.getUid();
                } else {
                    log.error("msgId ={} not valid from {} should be closed", msgHeader.getMsgId(), aresTKcpContext.getRemoteAddr());
                    aresTKcpContext.close();
                    return;
                }
            }
            AresThreadPool logicProcessThreadPool = LogicThreadPoolGroup.INSTANCE.selectThreadPool(ThreadPoolType.LOGIN.getValue());
            logicProcessThreadPool.execute(uid, aresTKcpContext, calledMethod, uid, paraObj);
            return;
        }
        if (playerSession != null) {
            peerConn.innerRedirectTo(ServerType.GAME, playerSession.getUid(), aresPacket);
            return;
        }
        log.error("客户端没有登录，但发了别的包");
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
        if (cacheObj instanceof PlayerSession disConnectedPlayerSession) {
            log.info("----- playerSession={} ", aresTKcpContext, disConnectedPlayerSession);
            PlayerSession playerSession = sessionService.getPlayerSession(disConnectedPlayerSession.getUid());
            if (playerSession.getSid() != disConnectedPlayerSession.getSid()) {
                log.warn("onClientClosed uid = {} have re-login", disConnectedPlayerSession.getUid());
                return;
            }
            ProtoInner.InnerPlayerDisconnectRequest disconnectRequest = ProtoInner.InnerPlayerDisconnectRequest.newBuilder()
                    .setSid(playerSession.getSid()).build();
            peerConn.sendToGameMsg(disConnectedPlayerSession.getUid(), ProtoInner.InnerMsgId.INNER_PLAYER_DISCONNECT_REQ_VALUE, disconnectRequest);
            AresThreadPool logicProcessThreadPool = LogicThreadPoolGroup.INSTANCE.selectThreadPool(ThreadPoolType.LOGIN.getValue());
            logicProcessThreadPool.execute(disConnectedPlayerSession.getUid(), disConnectedPlayerSession, sessionService::playerDisconnect);
        }
    }


    @Override
    public void onServerClosed(AresTKcpContext aresTKcpContext) {

    }
}
