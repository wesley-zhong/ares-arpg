package com.ares.discovery.support;

import com.ares.discovery.DiscoveryService;
import com.ares.discovery.DiscoveryServiceImpl;
import com.ares.discovery.transfer.OnDiscoveryWatchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.util.ArrayList;
import java.util.List;

@Configuration
@Slf4j
public class AresDiscoveryConfigure {
    @Value("${spring.application.name}")
    private String appName;

    @Value("${server.port}")
    private int serverPort;
    @Value("${server.tcp-port:0}")
    private int serverTcpPort;

    @Value("${area.id:0}")
    private int areaId;
    @Value("${server.type:0}")
    private int serverType;


    @Bean
    @ConditionalOnMissingBean
    @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    public DiscoveryService discoveryService(@Autowired DiscoveryEndPoints discoveryEndPoints, @Autowired OnDiscoveryWatchService onDiscoveryWatchService) {
        DiscoveryServiceImpl etcdService = new DiscoveryServiceImpl();
        DiscoveryEndPoints.WatchInfo[] watchServers = discoveryEndPoints.getWatchServers();
        List<String> watchPreFixes = new ArrayList<>();
        if (watchServers != null) {
            for (DiscoveryEndPoints.WatchInfo watchInfo : watchServers) {
                List<String> watchList = watchInfo.getWatchPrefix();
                watchPreFixes.addAll(watchList);
            }
        }
        int tcpPort = serverTcpPort == 0 ? serverPort : serverTcpPort;
        etcdService.init(discoveryEndPoints.getEndpoints(), serverType, appName, tcpPort, areaId, watchPreFixes, onDiscoveryWatchService::onWatchServiceChange);
        return etcdService;
    }
}
