package com.router.network;

import com.ares.common.bean.ServerType;
import com.ares.core.bean.AresMsgIdMethod;
import com.ares.core.bean.AresPacket;
import com.ares.core.service.ServiceMgr;
import com.ares.core.tcp.AresTKcpContext;
import com.ares.core.tcp.AresTcpHandler;
import com.ares.core.thread.AresThreadPool;
import com.ares.core.thread.LogicProcessThreadPool;
import com.ares.core.thread.LogicThreadPoolGroup;
import com.game.protoGen.ProtoCommon;
import com.router.config.ThreadPoolType;
import io.netty.buffer.ByteBufInputStream;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;


@Slf4j
public class RouterMsgHandler implements AresTcpHandler {
    @Autowired
    protected ServiceMgr serviceMgr;

    @Autowired
    private PeerConn peerConn;

    protected static final String UTF8 = "UTF-8";

    @Override
    public void handleMsgRcv(AresTKcpContext aresTKcpContext) throws IOException {
        AresPacket aresPacket = aresTKcpContext.getRcvPackage();
        ProtoCommon.MsgHeader msgHeader = aresPacket.getRecvHeader();
        int msgId = msgHeader.getMsgId();
        AresMsgIdMethod calledMethod = serviceMgr.getCalledMethod(msgId);
        long uid = msgHeader.getUid();
        if (calledMethod == null) {
            int toServerType = msgHeader.getRouterTo();
            if (toServerType == ServerType.TEAM.getValue()) {
                peerConn.sendToTeam(uid, aresPacket);
                return;
            }
            if (toServerType == ServerType.GAME.getValue()) {
                peerConn.sendToGame(uid, aresPacket);
                return;
            }
            log.error("XXXXXXXXXXXXXXX from server ={}  msgId ={} to server type ={} error", aresTKcpContext, msgId, toServerType);
            return;
        }
        int length = aresPacket.getRecvByteBuf().readableBytes();
        Object paraObj = calledMethod.getParser().parseFrom(new ByteBufInputStream(aresPacket.getRecvByteBuf(), length));

        AresThreadPool logicProcessThreadPool = LogicThreadPoolGroup.INSTANCE.selectThreadPool(ThreadPoolType.LOGIC.getValue());
        logicProcessThreadPool.execute(aresTKcpContext, calledMethod, uid, paraObj);
    }

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
    }

    @Override
    public boolean isChannelValidate(AresTKcpContext aresTKcpContext) {
        return true;
    }


    @Override
    public void onServerClosed(Channel aresTKcpContext) {
    }
}
