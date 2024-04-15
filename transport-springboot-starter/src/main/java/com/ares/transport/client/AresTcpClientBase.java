package com.ares.transport.client;

import com.ares.transport.bean.ServerNodeInfo;
import com.ares.core.bean.AresPacket;
import com.ares.transport.bean.TcpConnServerInfo;
import com.ares.core.thread.AresThreadFactory;
import com.google.protobuf.Message;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadFactory;


@Slf4j
public abstract class AresTcpClientBase implements AresTcpClient {
    private final ThreadFactory threadFactory = new AresThreadFactory("a-c-t");
    protected final List<ServerNodeInfo> serverNodeInfos = new CopyOnWriteArrayList<>();
    protected final Map<Integer, Map<String,List<TcpConnServerInfo>>> tcpConnServerInfoMap = new ConcurrentHashMap<>();

    public AresTcpClientBase(List<ServerNodeInfo> serverNodeInfos) {
        this.serverNodeInfos.addAll(serverNodeInfos);
    }

    @Override
    public void init() {
        threadFactory.newThread(new Runnable() {
            @Override
            public void run() {
                log.info("---- start  tcp client thread ------");
                try {
                    while (true) {
                        connectCheck();
                        Thread.sleep(5000);
                    }
                } catch (Exception e) {
                    log.error("-----conn error", e);
                }
            }
        }).start();
    }

    private void connectCheck() {
        for (ServerNodeInfo serverNodeInfo : serverNodeInfos) {
            Map<String,List<TcpConnServerInfo>> tcpConnServerMap = tcpConnServerInfoMap.get(serverNodeInfo.getAreaId());
            if(CollectionUtils.isEmpty(tcpConnServerMap)){
                connect(serverNodeInfo);
                continue;
            }

            List<TcpConnServerInfo> tcpConnServerInfos = tcpConnServerMap.get(serverNodeInfo.getServiceName());
            if(CollectionUtils.isEmpty(tcpConnServerInfos)){
                connect(serverNodeInfo);
            }

            boolean bfound = false;
            for(TcpConnServerInfo tcpConnServerInfo : tcpConnServerInfos){
                if(tcpConnServerInfo.getServerNodeInfo().getServiceId().equals(serverNodeInfo.getServiceId())){
                    if(tcpConnServerInfo.getChannel() != null && tcpConnServerInfo.getChannel().isActive()){
                        continue;
                    }
                    connect(serverNodeInfo);
                    bfound = true;
                }
            }

            if(!bfound){
                connect(serverNodeInfo);
            }
        }
    }

    protected void checkAndAdd(ServerNodeInfo newserverNodeInfo) {
        for (ServerNodeInfo serverNodeInfo : serverNodeInfos) {
            if (serverNodeInfo.getServiceId().equals(newserverNodeInfo.getServiceId())) {
                return;
            }
        }
        serverNodeInfos.add(newserverNodeInfo);
    }
    public void addServerInfo(ServerNodeInfo serverNodeInfo) {
        serverNodeInfos.add(serverNodeInfo);
    }

    public void delServerInfo(ServerNodeInfo serverNodeInfo) {
        serverNodeInfos.removeIf(value->value.getServiceId().equals(serverNodeInfo.getServiceId()));
    }

   public  void send(TcpConnServerInfo serverInfo, int msgId, Message body){
        send(serverInfo.getChannel(), msgId, body);
    }


    public void send(Channel channel, int msgId, Message message) {
        AresPacket msgPack = AresPacket.create(msgId, message);
        channel.writeAndFlush(msgPack);
    }

    public void send(Channel channel, AresPacket aresPacket) {
        channel.writeAndFlush(aresPacket);
    }

    public void send(Channel channel, AresPacket... packets) {
        for (AresPacket aresPacket : packets) {
            channel.write(aresPacket);
        }
        channel.flush();
    }
}
