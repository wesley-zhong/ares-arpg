package com.ares.transport.surpport;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AresWebSocketConfiguration {
    @Bean
    public AresWebSocketServer AresWebSocketServer() {
        return new AresWebSocketServer();
    }


    public static class AresWebSocketServer {

    }
}
