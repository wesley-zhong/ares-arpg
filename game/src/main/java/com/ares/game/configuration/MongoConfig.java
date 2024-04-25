package com.ares.game.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "mongo")
@Getter
@Setter
public class MongoConfig {
    private String addrs;
    private String userName;
    private String password;
}
