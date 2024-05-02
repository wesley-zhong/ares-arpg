package com.ares.gateway.configuration;

import com.ares.core.tcp.AresClientTcpHandler;
import com.ares.core.tcp.AresServerTcpHandler;
import com.ares.core.thread.LogicThreadPoolGroup;
import com.ares.dal.redis.RedisDAO;
import com.ares.dal.redis.RedisFactory;
import com.ares.dal.redis.SyncRedisFactory;
import com.ares.discovery.DiscoveryService;
import com.ares.gateway.network.GateWayMsgHandler;
import com.ares.gateway.network.InnerMsgHandler;
import com.ares.nk2.coroutine.CoroHandle;
import com.ares.transport.client.AresTcpClient;
import com.ares.transport.client.AresTcpClientConn;
import com.ares.transport.client.AresTcpClientImpl;
import com.ares.transport.inner.InnerMsgEncoder;
import com.ares.transport.server.AresNettyServer;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Configuration
@ComponentScan("com.ares")
public class GatewayConfiguration implements InitializingBean {
    @Value("${redis.url}")
    private String redisUrl;

    @Bean
    public AresTcpClientConn aresTcpClientConn(@Autowired AresClientTcpHandler aresTcpHandler) {
        AresTcpClientConn aresTcpClientConn = new AresTcpClientConn(aresTcpHandler, new InnerMsgEncoder());
        return aresTcpClientConn;
    }

    @Bean
    @Lazy
    public AresTcpClient aresTcpClient(@Autowired @Lazy AresTcpClientConn conn) {
        AresTcpClient aresTcpClient = new AresTcpClientImpl(conn);
        aresTcpClient.init();
        return aresTcpClient;
    }


    @Bean
    public AresClientTcpHandler aresClientTcpHandler() {
        return new InnerMsgHandler();
    }

    @Bean
    public AresServerTcpHandler aresServerTcpHandler() {
        return new GateWayMsgHandler();
    }


    @Bean
    public AresNettyServer aresNettyServer(@Autowired AresServerTcpHandler aresServerTcpHandler) {
        return new AresNettyServer(aresServerTcpHandler, new InnerMsgEncoder());
    }

    @Bean
    public RedisFactory redisFactory() {
        RedisFactory redisFactory = new SyncRedisFactory();
        redisFactory.initPool(redisUrl);
        return redisFactory;
    }

    @Bean
    public RedisDAO redisDAO(@Autowired RedisFactory redisFactory) {
        return new RedisDAO(redisFactory);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        CoroHandle.init();
        LogicThreadPoolGroup logicThreadPoolGroup = new LogicThreadPoolGroup(1);
        logicThreadPoolGroup.createThreadPool(ThreadPoolType.LOGIN.getValue(), 8);
    }
}
