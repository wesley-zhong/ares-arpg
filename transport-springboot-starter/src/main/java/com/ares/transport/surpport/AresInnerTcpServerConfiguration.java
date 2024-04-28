package com.ares.transport.surpport;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AresInnerTcpServerConfiguration {

    @Bean
    public AresInnerServer aresRpcServer() {
        return new AresInnerServer();
    }


    public static class AresInnerServer {

    }
}
