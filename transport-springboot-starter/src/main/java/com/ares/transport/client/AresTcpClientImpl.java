package com.ares.transport.client;

import com.ares.core.bean.AresPacket;
import com.ares.transport.bean.ServerNodeInfo;
import com.ares.transport.bean.TcpConnServerInfo;
import com.google.protobuf.Message;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.util.*;


@Slf4j
public class AresTcpClientImpl extends AresTcpClientBase {
    private final AresTcpClientConn aresTcpClientConn;

    public AresTcpClientImpl(List<ServerNodeInfo> serverNodeInfos, AresTcpClientConn aresTcpClientConn) {
        super(serverNodeInfos);
        this.aresTcpClientConn = aresTcpClientConn;
    }

    public AresTcpClientImpl(AresTcpClientConn aresTcpClientConn) {
        super(new ArrayList<>());
        this.aresTcpClientConn = aresTcpClientConn;
    }

    public void init() {
        super.init();
    }

    @Override
    public Channel connect(ServerNodeInfo serverNodeInfo) {
        Map<String, List<TcpConnServerInfo>> stringListMap = tcpConnServerInfoMap.get(serverNodeInfo.getAreaId());
        if (CollectionUtils.isEmpty(stringListMap)) {
            return doConnect(serverNodeInfo, null);
        }
        List<TcpConnServerInfo> tcpConnServerInfos = stringListMap.get(serverNodeInfo.getServiceName());
        if (CollectionUtils.isEmpty(tcpConnServerInfos)) {
            return doConnect(serverNodeInfo, null);
        }


        for (TcpConnServerInfo existConn : tcpConnServerInfos) {
            if (existConn.getServerNodeInfo().getServiceId().equals(serverNodeInfo.getServiceId())) {
                Channel channel = existConn.getChannel();
                if (channel.isActive()) {
                    return channel;
                }
                return doConnect(serverNodeInfo, existConn);
            }
        }
        return doConnect(serverNodeInfo, null);
    }

    @Override
    public void close(ServerNodeInfo serverNodeInfo) {
        Map<String, List<TcpConnServerInfo>> stringListMap = tcpConnServerInfoMap.get(serverNodeInfo.getAreaId());
        if (CollectionUtils.isEmpty(stringListMap)) {
            log.info("close server ={} not found connection", serverNodeInfo);
             return;
        }
        List<TcpConnServerInfo> tcpConnServerInfos = stringListMap.get(serverNodeInfo.getServiceName());
        if(CollectionUtils.isEmpty(tcpConnServerInfos)){
            log.info("close server ={} not found connection 2", serverNodeInfo);
            return;
        }
        Iterator<TcpConnServerInfo> iterator = tcpConnServerInfos.iterator();
        while(iterator.hasNext()){
            TcpConnServerInfo tcpConnServerInfo = iterator.next();
            if(tcpConnServerInfo.getServerNodeInfo().getServiceId().equals(serverNodeInfo.getServiceId())){
                iterator.remove();
                if(tcpConnServerInfo.getChannel() != null) {
                    delServerInfo(tcpConnServerInfo.getServerNodeInfo());
                    tcpConnServerInfo.getChannel().close();
                    log.info("-----remove and close server node: {}",tcpConnServerInfo.getServerNodeInfo());
                    return ;
                }
            }
        }
        log.info("close server ={} not found connection 3", serverNodeInfo);
    }

    private Channel doConnect(ServerNodeInfo serverNodeInfo, TcpConnServerInfo oldConnServerInfo) {
        Channel channel = aresTcpClientConn.connect(serverNodeInfo.getIp(), serverNodeInfo.getPort());
        checkAndAdd(serverNodeInfo);
        if (channel == null) {
            return null;
        }

        if (oldConnServerInfo != null) {
            oldConnServerInfo.setChannel(channel);
            return channel;
        }
        TcpConnServerInfo tcpConnServerInfo = new TcpConnServerInfo(channel, serverNodeInfo);
        Map<String, List<TcpConnServerInfo>> stringListMap = tcpConnServerInfoMap.get(serverNodeInfo.getAreaId());
        if (stringListMap == null) {
            stringListMap = new HashMap<>();
            List<TcpConnServerInfo> tcpConnServerInfos = new ArrayList<>();
            tcpConnServerInfos.add(tcpConnServerInfo);
            stringListMap.put(serverNodeInfo.getServiceName(), tcpConnServerInfos);
            tcpConnServerInfoMap.put(serverNodeInfo.getAreaId(), stringListMap);
            return channel;
        }
        List<TcpConnServerInfo> tcpConnServerInfos = stringListMap.get(serverNodeInfo.getServiceName());
        if (tcpConnServerInfos == null) {
            tcpConnServerInfos = new ArrayList<>();
            tcpConnServerInfos.add(tcpConnServerInfo);
            stringListMap.put(serverNodeInfo.getServiceName(), tcpConnServerInfos);
            return channel;
        }
        tcpConnServerInfos.add(tcpConnServerInfo);
        return channel;
    }

    @Override
    public void send(int areaId, String serverName, int msgId, Message body) {
        TcpConnServerInfo tcpConnServerInfo = getTcpConnServerInfo(areaId, serverName);
        if (tcpConnServerInfo == null || !tcpConnServerInfo.getChannel().isActive()) {
            log.error("areaId ={} serveName ={} not found", areaId, serverName);
            return;
        }
        send(tcpConnServerInfo, msgId, body);
    }

    @Override
    public TcpConnServerInfo getTcpConnServerInfo(int areaId, String gameServiceName) {
        Map<String, List<TcpConnServerInfo>> stringListMap = tcpConnServerInfoMap.get(areaId);
        if (stringListMap == null) {
            return null;
        }
        List<TcpConnServerInfo> tcpConnServerInfos = stringListMap.get(gameServiceName);
        if (tcpConnServerInfos == null) {
            return null;
        }

        //may be used load balance
        for (TcpConnServerInfo tcpConnServerInfo : tcpConnServerInfos) {
            if (tcpConnServerInfo.getServerNodeInfo().getServiceName().equals(gameServiceName)) {
                return tcpConnServerInfo;
            }
        }
        return null;
    }

    @Override
    public void send(int areaId, String serviceName, AresPacket... packets) {
        TcpConnServerInfo tcpConnServerInfo = getTcpConnServerInfo(areaId, serviceName);
        if (tcpConnServerInfo == null) {
            return;
        }
        super.send(tcpConnServerInfo.getChannel(), packets);
    }


    @Override
    public void send(int areaId, String serviceName, AresPacket packet) {
        TcpConnServerInfo tcpConnServerInfo = getTcpConnServerInfo(areaId, serviceName);
        super.send(tcpConnServerInfo.getChannel(), packet);
    }


}
