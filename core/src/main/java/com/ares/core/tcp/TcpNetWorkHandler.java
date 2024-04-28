package com.ares.core.tcp;


import com.ares.core.bean.AresPacket;
import com.game.protoGen.ProtoInner;
import io.netty.channel.Channel;

public interface TcpNetWorkHandler {
    void onClientConnected(AresTKcpContext aresTKcpContext);

    void onClientClosed(AresTKcpContext aresTKcpContext);

    void handleMsgRcv(AresPacket aresPacket);

    void handleMsgRcv(long pid, AresPacket aresPacket);

    void onServerConnected(Channel aresTKcpContext);

    void onServerClosed(Channel aresTKcpContext);

    boolean isChannelValidate(AresTKcpContext aresTKcpContext);
}
