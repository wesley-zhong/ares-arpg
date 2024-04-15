package com.ares.core.thread.task;

import com.ares.core.bean.AresMsgIdMethod;
import com.ares.core.bean.AresPacket;
import com.ares.core.excetion.LogicException;
import com.ares.core.tcp.AresTKcpContext;
import com.ares.core.thread.EventTask;
import com.ares.core.utils.AresContextThreadLocal;
import com.game.protoGen.ProtoCommon;
import com.google.protobuf.Message;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Setter
public class PacketEventTask implements EventTask {
    private AresTKcpContext aresTKcpContext;
    private AresMsgIdMethod method;
    private long param1;
    private Object param2;
    private ProtoCommon.MsgHeader msgHeader;

    public void clear() {
        method = null;
        param1 = 0L;
        param2 = null;
        aresTKcpContext = null;
    }

    @Override
    public void execute() {
        AresContextThreadLocal.cache(aresTKcpContext);
        ProtoCommon.MsgHeader.Builder sendHeader = msgHeader.toBuilder()
                .setRouterTo(0)
                .setCrc(0)
                .setSeqNo(0);
        try {
            Object rpcBody = method.getAresServiceProxy().callMethod(method, param1, param2);
            if (rpcBody instanceof Message body) {
                aresTKcpContext.send(AresPacket.create(sendHeader.build(), body));
            }
        } catch (LogicException e) {
            sendHeader.setErrCode(e.getErrCode());
            aresTKcpContext.send(AresPacket.create(sendHeader.build(), null));
            log.warn("msg={} exception={}", sendHeader, e);
        } catch (Exception e) {
            sendHeader.setErrCode(-1);
            aresTKcpContext.send(AresPacket.create(sendHeader.build(), null));
            log.error("XXXXX not catch exception", e);
        }
    }
}
