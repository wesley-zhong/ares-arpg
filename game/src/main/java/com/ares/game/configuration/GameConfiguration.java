package com.ares.game.configuration;

import com.ares.core.tcp.AresTcpHandler;
import com.ares.core.thread.LogicThreadPoolGroup;
import com.ares.core.utils.SnowFlake;
import com.ares.dal.redis.RedisDAO;
import com.ares.dal.redis.RedisFactory;
import com.ares.dal.redis.SyncRedisFactory;
import com.ares.discovery.DiscoveryService;
import com.ares.game.network.GameMsgHandler;
import com.ares.nk2.component.EntityMetaDataMgr;
import com.ares.nk2.coroutine.CoroHandle;
import com.ares.nk2.tool.NKStringFormater;
import com.ares.transport.bean.ServerNodeInfo;
import com.ares.transport.client.AresTcpClient;
import com.ares.transport.client.AresTcpClientConn;
import com.ares.transport.client.AresTcpClientImpl;
import com.ares.transport.inner.InnerMsgEncoder;
import com.ares.transport.server.AresNettyServer;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import io.vertx.core.impl.ConcurrentHashSet;
import lombok.extern.slf4j.Slf4j;
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
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

@Slf4j
@Configuration
@ComponentScan("com.ares")
public class GameConfiguration implements InitializingBean {
    @Autowired
    private DiscoveryService discoveryService;
    @Value("${redis.url}")
    private String redisUrl;
    @Value("${useCoroutine:false}")
    private boolean useCoroutine;

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
    public AresTcpHandler aresTcpHandler() {
        return new GameMsgHandler();
    }

    //
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
        return new AresNettyServer(aresTcpHandler, new InnerMsgEncoder());
    }

    public boolean isUseCoroutine() {
        return useCoroutine;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (useCoroutine) {
            CoroHandle.init();
        }

        ServerNodeInfo myselfNodeInfo = discoveryService.getEtcdRegister().getMyselfNodeInfo();
        int myWorkId = discoveryService.getMyWorkId();
        SnowFlake.init(myWorkId, myselfNodeInfo.getServerType());
       // testRegisterWorkerId();

        LogicThreadPoolGroup logicThreadPoolGroup = new LogicThreadPoolGroup(2, 1);
        logicThreadPoolGroup.createThreadPool(ThreadPoolType.PLAYER_LOGIN.getValue(), 4);
        logicThreadPoolGroup.createThreadPool(ThreadPoolType.LOGIC.getValue(), 4);
        // logicThreadPoolGroup.createVirtualThreadPool(VirtualThreadPoolType.LOGIC.getValue(), 2000);

        int checkRet = EntityMetaDataMgr.getInstance().entityCheck("com.ares.game");
        if (checkRet != 0) {
            throw new RuntimeException(NKStringFormater.format("check entity failed! checkResult:[{}]", checkRet));
        }
    }

//    private void testRegisterWorkerId() {
//        Set<Integer> workerIdMap = new ConcurrentHashSet<>();
//        AtomicInteger index = new AtomicInteger(0);
//        Executors.newVirtualThreadPerTaskExecutor().submit(() -> {
//                    for (int i = 0; i <4000; ++i) {
//                        try {
//                            String serviceId = "game_test_" + index.incrementAndGet();
//                            int workId = discoveryService.genNextSeqNum(serviceId, "game");
//                            if (workerIdMap.contains(workId)) {
//                                log.error("_------------ workerId ={} gameservice ={} error", workId, serviceId);
//                            }
//                        } catch (Exception e) {
//                            throw new RuntimeException(e);
//                        }
//                    }
//                }
//        );
//    }

}
