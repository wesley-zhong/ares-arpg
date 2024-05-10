package com.ares.client.network;

import com.ares.client.bean.ClientPlayer;
import com.ares.client.controller.ReceiveService;
import com.ares.core.bean.AresMsgIdMethod;
import com.ares.core.bean.AresPacket;
import com.ares.core.service.ServiceMgr;
import com.ares.core.tcp.AresTKcpContext;
import com.ares.core.tcp.AresTcpHandler;
import com.ares.core.utils.AresContextThreadLocal;
import com.game.protoGen.ProtoCommon;
import io.netty.buffer.ByteBufInputStream;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@Slf4j
public class ClientMsgHandler implements AresTcpHandler {

    @Autowired
    private ServiceMgr serviceMgr;
    private ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();
    @Autowired
    private ReceiveService receiveService;


    @Override
    public void handleMsgRcv(AresTKcpContext aresTKcpContext) throws IOException {
        AresPacket aresPacket = aresTKcpContext.getRcvPackage();
        ProtoCommon.MsgHeader msgHeader = aresPacket.getRecvHeader();
        log.info("-------------------  receive header  ={}", msgHeader);
        AresMsgIdMethod calledMethod = serviceMgr.getCalledMethod(msgHeader.getMsgId());
        if (calledMethod == null) {
            log.error("msgId ====== {} not register", msgHeader.getMsgId());
            return;
        }
        int length = aresPacket.getRecvByteBuf().readableBytes();
        Object paraObj = calledMethod.getParser().parseFrom(new ByteBufInputStream(aresPacket.getRecvByteBuf(), length));
        if (msgHeader.getMsgId() == ProtoCommon.MsgId.GAME_LOGIN_RES_VALUE) {
            AresContextThreadLocal.cache(aresTKcpContext);
            calledMethod.getAresServiceProxy().callMethod(calledMethod, msgHeader.getUid(), paraObj);
            return;
        }
        ClientPlayer clientPlayer = (ClientPlayer) aresTKcpContext.getCacheObj();
        //   AresContextThreadLocal.cache(aresTKcpContext);
        executorService.submit(() -> {
            calledMethod.getAresServiceProxy().callMethod(calledMethod, clientPlayer.getUId(), paraObj);
        });
    }

    @Override
    public void onServerConnected(Channel aresTKcpContext) {
        log.info("----- {} connected", aresTKcpContext);


    }

    @Override
    public void onClientConnected(AresTKcpContext aresTKcpContext) {
        log.info("---onClientConnected ={} ", aresTKcpContext);
    }

    @Override
    public void onClientClosed(AresTKcpContext aresTKcpContext) {
    }

    @Override
    public boolean isChannelValidate(AresTKcpContext aresTKcpContext) {
        return true;
    }


    @Override
    public void onServerClosed(AresTKcpContext aresTKcpContext) {
        receiveService.onConnectLost(aresTKcpContext);
    }
}
