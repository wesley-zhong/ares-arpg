package com.ares.login.configuration;

import com.ares.common.bean.ServerType;
import com.ares.core.tcp.AresTcpHandler;
import com.ares.core.thread.LogicThreadPoolGroup;
import com.ares.core.utils.SnowFlake;
import com.ares.dal.redis.RedisDAO;
import com.ares.dal.redis.RedisFactory;
import com.ares.dal.redis.SyncRedisFactory;
import com.ares.login.network.LoginMsgHandler;
import com.ares.transport.client.AresTcpClient;
import com.ares.transport.client.AresTcpClientConn;
import com.ares.transport.client.AresTcpClientImpl;
import com.ares.transport.encode.AresPacketMsgEncoder;
import com.ares.transport.inner.InnerMsgEncoder;
import com.ares.transport.server.AresNettyServer;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import java.util.ArrayList;
import java.util.List;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;


@Configuration
@ComponentScan("com.ares")
public class LoginConfiguration implements InitializingBean {
    @Value("${srvId:1}")
    private int srvId;
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
        return new LoginMsgHandler();
    }

    @Bean
    public MongoClient mongoClient(@Autowired MongoConfig mongoConfig) {
        CodecRegistry pojoCodecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),
                fromProviders(PojoCodecProvider.builder().automatic(true).build()));
        fromProviders(PojoCodecProvider.builder().automatic(true).build());

        MongoCredential credential = MongoCredential.createCredential(mongoConfig.getUserName(), "admin", mongoConfig.getUserName().toCharArray());
        List<ServerAddress> mongoAddrsList = new ArrayList<>();
        String[] serverAddrs = mongoConfig.getAddrs().split(";");
        for (String serverAddr : serverAddrs) {
            ServerAddress serverAddress = new ServerAddress(serverAddr);
            mongoAddrsList.add(serverAddress);
        }
        MongoClientSettings settings = MongoClientSettings.builder()
                .credential(credential)
                .applyToClusterSettings(builder -> builder.hosts(mongoAddrsList))
                .codecRegistry(pojoCodecRegistry)
                .build();
        return MongoClients.create(settings);
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

    @Bean
    public AresNettyServer aresNettyServer(@Autowired AresTcpHandler aresTcpHandler) {
        return new AresNettyServer(aresTcpHandler, new AresPacketMsgEncoder());
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        SnowFlake.init(srvId, ServerType.LOGIN.getValue());
        LogicThreadPoolGroup logicThreadPoolGroup = new LogicThreadPoolGroup(1);
        logicThreadPoolGroup.createThreadPool(ThreadPoolType.LOGIC.getValue(), 1);
    }
}
