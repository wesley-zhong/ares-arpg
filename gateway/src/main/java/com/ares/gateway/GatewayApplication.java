package com.ares.gateway;

import com.ares.discovery.annotation.EnableAresDiscovery;
import com.ares.transport.annotation.EnableAresTcpServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
//@EnableAresTcpServer
@EnableAresDiscovery("com.ares.gateway.network")
public class GatewayApplication {
    public static void main(String[] args) {
       SpringApplication.run(GatewayApplication.class, args);
    }
}
