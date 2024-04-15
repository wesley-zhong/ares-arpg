package com.ares.core.tcp;

import io.netty.channel.Channel;

import java.io.IOException;

public interface AresTcpHandler {

    void handleMsgRcv(AresTKcpContext aresPacket) throws IOException;

    void onClientConnected(AresTKcpContext aresTKcpContext);

    void onClientClosed(AresTKcpContext aresTKcpContext);

    boolean isChannelValidate(AresTKcpContext aresTKcpContext);

    void onServerConnected(Channel aresTKcpContext);

    void onServerClosed(Channel aresTKcpContext);
}

