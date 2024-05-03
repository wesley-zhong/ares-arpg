package com.ares.transport.surpport;

import com.ares.transport.server.AresNettyServer;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;

public class AresTcpServerConfiguration {
    @Bean
    @ConditionalOnMissingBean
    @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    public AresNettyServer aresNettyServer() {
        return new AresNettyServer(null,null);
    }

}
