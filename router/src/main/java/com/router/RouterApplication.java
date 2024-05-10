package com.router;

import com.ares.discovery.annotation.EnableAresDiscovery;
import com.ares.transport.annotation.EnableAresTcpServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableAresDiscovery("com.ares.router.network")
public class RouterApplication {
    public static void main(String[] args) {
       SpringApplication.run(RouterApplication.class, args);
    }
}
