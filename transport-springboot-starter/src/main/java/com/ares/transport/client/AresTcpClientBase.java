package com.ares.transport.client;

import com.ares.transport.bean.TcpConnServerInfo;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Slf4j
public abstract class AresTcpClientBase implements AresTcpClient {
    //  private final ThreadFactory threadFactory = new AresThreadFactory("a-c-t");
    //  protected final Map<String, ServerNodeInfo> serviceIdNodeInfos = new ConcurrentHashMap<>();
    /**
     * Integer  key : service name  : game.V1
     * <p>
     * String key : service Id : game.V1/192.18.2.101:7080
     * TcpConnServerInfo  : service instance
     */
    protected final Map<String, Map<String, TcpConnServerInfo>> serviceNameTcpConnServerInfoMap = new ConcurrentHashMap<>();

    @Override
    public void init() {
        Thread.ofVirtual().name("client-check_v_t_", 0).start(() -> {
            log.info("---- start  tcp client virtual thread ------");
            while (true) {
                try {
                    connectCheck();
                    Thread.sleep(8000);
                } catch (Exception e) {
                    log.error("-----conn error", e);
                }
            }
        });
    }

    private void connectCheck() {
        Collection<Map<String, TcpConnServerInfo>> serviceIdInstances = serviceNameTcpConnServerInfoMap.values();
        for (Map<String, TcpConnServerInfo> serverInfoMap : serviceIdInstances) {
            for (TcpConnServerInfo tcpConnServerInfo : serverInfoMap.values()) {
                int lostConnect = tcpConnServerInfo.checkLostConnect();
                if (lostConnect == 0) {
                    continue;
                }
                log.info("-----------connect check- do connect serverNode={} count ={}", tcpConnServerInfo.getServerNodeInfo(), lostConnect);
                doConnect(tcpConnServerInfo, lostConnect);
            }
        }
    }

    abstract protected void doConnect(TcpConnServerInfo oldConnServerInfo, int count);
}
