package com.ares.transport.client;

import com.ares.transport.bean.ServerNodeInfo;
import com.ares.transport.bean.TcpConnServerInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.Map;


@Slf4j
public class AresTcpClientImpl extends AresTcpClientBase {
    private final AresTcpClientConn aresTcpClientConn;

    public AresTcpClientImpl(AresTcpClientConn aresTcpClientConn) {
        this.aresTcpClientConn = aresTcpClientConn;
    }

    public void init() {
        super.init();
    }

    @Override
    public void connect(ServerNodeInfo serverNodeInfo, int connCount) {
        Map<String, TcpConnServerInfo> serviceIdConnMap = serviceNameTcpConnServerInfoMap.get(serverNodeInfo.getServiceName());
        TcpConnServerInfo tcpConnServerInfo;
        if (serviceIdConnMap == null) {
            tcpConnServerInfo = new TcpConnServerInfo(serverNodeInfo, connCount);
            serviceIdConnMap = new HashMap<>();
            serviceIdConnMap.put(serverNodeInfo.getServiceId(), tcpConnServerInfo);
            serviceNameTcpConnServerInfoMap.put(serverNodeInfo.getServiceName(), serviceIdConnMap);
            doConnect(tcpConnServerInfo, connCount);
            return;
        }
        tcpConnServerInfo = serviceIdConnMap.get(serverNodeInfo.getServiceId());
        if (tcpConnServerInfo == null) {
            tcpConnServerInfo = new TcpConnServerInfo(serverNodeInfo, connCount);
            serviceIdConnMap.put(serverNodeInfo.getServiceId(), tcpConnServerInfo);
            doConnect(tcpConnServerInfo, connCount);
            return;
        }
       // doConnect(tcpConnServerInfo, connCount);
    }

    @Override
    protected void doConnect(TcpConnServerInfo tcpConnServerInfo, int count) {
        for (int i = 0; i < count; ++i) {
            ServerNodeInfo serverNodeInfo = tcpConnServerInfo.getServerNodeInfo();
            aresTcpClientConn.asynConnect(serverNodeInfo.getIp(), serverNodeInfo.getPort());
        }
    }

    @Override
    public void close(ServerNodeInfo serverNodeInfo) {
        log.warn("------------ remove serverNode={}", serverNodeInfo);
        Map<String, TcpConnServerInfo> serviceIdInstances = serviceNameTcpConnServerInfoMap.get(serverNodeInfo.getServiceName());
        if (CollectionUtils.isEmpty(serviceIdInstances)) {
            log.info("close server ={} not found connection", serverNodeInfo);
            serviceNameTcpConnServerInfoMap.remove(serverNodeInfo.getServiceName());
            return;
        }

        TcpConnServerInfo tcpConnServerInfo = serviceIdInstances.get(serverNodeInfo.getServiceId());
        if (tcpConnServerInfo == null) {
            return;
        }
        tcpConnServerInfo.close();
        serviceIdInstances.remove(serverNodeInfo.getServiceId());
        log.info("close and remove server ={} ", serverNodeInfo);
    }

    @Override
    public TcpConnServerInfo getTcpConnServerInfo(String serviceName, String serviceId) {
        Map<String, TcpConnServerInfo> serviceIdConnMap = serviceNameTcpConnServerInfoMap.get(serviceName);
        if (CollectionUtils.isEmpty(serviceIdConnMap)) {
            return null;
        }
        return serviceIdConnMap.get(serviceId);
    }
}
