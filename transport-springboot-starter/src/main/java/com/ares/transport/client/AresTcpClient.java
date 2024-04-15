package com.ares.transport.client;

import com.ares.transport.bean.ServerNodeInfo;
import com.ares.core.bean.AresPacket;
import com.ares.transport.bean.TcpConnServerInfo;
import com.google.protobuf.Message;
import io.netty.channel.Channel;

public interface AresTcpClient {
    void send(int areaId, String serverName, int msgId, Message body);

    void send(int areaId, String serverName, AresPacket... packets);

    void send(int areaId, String serviceName, AresPacket packet);

    void send(TcpConnServerInfo serverInfo, int msgId, Message body);


    Channel connect(ServerNodeInfo serverNodeInfo);
    void close(ServerNodeInfo serverNodeInfo);

    TcpConnServerInfo getTcpConnServerInfo(int areaId, String gameServiceName);

    void init();
}
