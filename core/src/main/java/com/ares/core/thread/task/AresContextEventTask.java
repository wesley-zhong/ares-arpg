package com.ares.core.thread.task;

import com.ares.core.bean.AresMsgIdMethod;
import com.ares.core.bean.AresPacket;
import com.ares.core.excetion.FyLogicException;
import com.ares.core.tcp.AresTKcpContext;
import com.ares.core.thread.EventTask;
import com.game.protoGen.ProtoCommon;
import com.google.protobuf.Message;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Setter
@Slf4j
public class AresContextEventTask implements EventTask {
    private AresMsgIdMethod method;
    private AresTKcpContext aresTKcpContext;
    private Object p2;
    private ProtoCommon.MsgHeader msgHeader;

    @Override
    public void execute() {
        try {
            Object rpcBody = method.getAresServiceProxy().callMethod(method, aresTKcpContext, p2);
            if (rpcBody instanceof Message body) {
                ProtoCommon.MsgHeader.Builder sendHeader = msgHeader.toBuilder()
                        .setRouterTo(0)
                        .setCrc(0)
                        .setSeqNo(0);
                aresTKcpContext.send(AresPacket.create(sendHeader.build(), body));
            }
        } catch (FyLogicException e) {
            ProtoCommon.MsgHeader.Builder sendHeader = msgHeader.toBuilder()
                    .setRouterTo(0)
                    .setCrc(0)
                    .setSeqNo(0);
            sendHeader.setErrCode(e.getErrCode());
            aresTKcpContext.send(AresPacket.create(sendHeader.build(), null));
            log.warn("msg={} body={} exception={}", sendHeader, p2, e);
        } catch (Throwable e) {
            ProtoCommon.MsgHeader.Builder sendHeader = msgHeader.toBuilder()
                    .setRouterTo(0)
                    .setCrc(0)
                    .setSeqNo(0);
            sendHeader.setErrCode(-1);
            aresTKcpContext.send(AresPacket.create(sendHeader.build(), null));
            log.error("XXXXX not catch exception msg={} body={}", sendHeader, p2, e);
        }
    }

    @Override
    public void clear() {
        method = null;
        p2 = null;
        aresTKcpContext = null;
    }
}
