package com.router.config;

import com.ares.core.tcp.AresTcpHandler;
import com.ares.core.thread.LogicThreadPoolGroup;
import com.ares.dal.redis.RedisDAO;
import com.ares.dal.redis.RedisFactory;
import com.ares.transport.client.AresTcpClient;
import com.ares.transport.client.AresTcpClientConn;
import com.ares.transport.client.AresTcpClientImpl;
import com.ares.transport.inner.InnerMsgEncoder;
import com.ares.transport.server.AresNettyServer;
import com.router.network.RouterMsgHandler;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Configuration
@ComponentScan("com.ares")
public class RouterConfiguration implements InitializingBean {
    @Value("${redis.url}")
    private String redisUrl;
    @Bean
    public AresTcpClientConn aresTcpClientConn(@Autowired AresTcpHandler aresTcpHandler) {
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
    public AresTcpHandler aresTcpHandler() {
        return new RouterMsgHandler();
    }

    @Bean
    public RedisFactory redisFactory() {
        RedisFactory redisFactory = new RedisFactory();
        redisFactory.initPool(redisUrl);
        return redisFactory;
    }

    @Bean
    public RedisDAO redisDAO(@Autowired RedisFactory redisFactory) {
        return new RedisDAO(redisFactory);
    }

    @Bean
    public AresNettyServer aresNettyServer(@Autowired AresTcpHandler aresTcpHandler) {
        return new AresNettyServer(aresTcpHandler, new InnerMsgEncoder());
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        LogicThreadPoolGroup logicThreadPoolGroup = new LogicThreadPoolGroup(1);
        logicThreadPoolGroup.createThreadPool(ThreadPoolType.LOGIC.getValue(), 1);
    }
}
