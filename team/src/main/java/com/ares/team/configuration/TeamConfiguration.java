package com.ares.team.configuration;

import com.ares.core.tcp.AresTcpHandler;
import com.ares.core.thread.LogicThreadPoolGroup;
import com.ares.team.network.TeamMsgHandler;
import com.ares.transport.client.AresTcpClient;
import com.ares.transport.client.AresTcpClientConn;
import com.ares.transport.client.AresTcpClientImpl;
import com.ares.transport.inner.InnerMsgEncoder;
import com.ares.transport.server.AresNettyServer;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Configuration
@ComponentScan("com.ares")
public class TeamConfiguration  implements InitializingBean {

    @Bean
    public AresTcpClientConn aresTcpClientConn(@Autowired AresTcpHandler aresTcpHandler) {
        AresTcpClientConn aresTcpClientConn = new AresTcpClientConn();
        aresTcpClientConn.initWithMsgEncoder(aresTcpHandler, new InnerMsgEncoder());
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
        return new TeamMsgHandler();
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
