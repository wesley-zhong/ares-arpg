package com.ares.transport.client;

import com.ares.transport.bean.ServerNodeInfo;
import com.ares.transport.bean.TcpConnServerInfo;

public interface AresTcpClient {

    void connect(ServerNodeInfo serverNodeInfo, int connCount);

    void close(ServerNodeInfo serverNodeInfo);

    TcpConnServerInfo getTcpConnServerInfo(String serviceName,String serviceId);

    void init();
}
