package com.ares.dal.mongo;

import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import java.util.ArrayList;
import java.util.List;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

@Getter
@Setter
public class AresMongoClient {
    private String mongoAddrs;
    private String mongoUserName;
    private String mongoPassword;

    private MongoClient mongoClient;

    public AresMongoClient(String addrs, String userName, String password){
        this.mongoAddrs = addrs;
        this.mongoUserName = userName;
        this.mongoPassword = password;
    }

    public void init() {
        CodecRegistry pojoCodecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),
                fromProviders(PojoCodecProvider.builder().automatic(true).build()));
        fromProviders(PojoCodecProvider.builder().automatic(true).build());

        MongoCredential credential = MongoCredential.createCredential(mongoUserName, "admin", mongoPassword.toCharArray());
        List<ServerAddress> mongoAddrsList = new ArrayList<>();
        String[] serverAddrs = mongoAddrs.split(";");
        for (String serverAddr : serverAddrs) {
            ServerAddress serverAddress = new ServerAddress(serverAddr);
            mongoAddrsList.add(serverAddress);
        }
        MongoClientSettings settings = MongoClientSettings.builder()
                .credential(credential)
                .applyToClusterSettings(builder -> builder.hosts(mongoAddrsList))
                .codecRegistry(pojoCodecRegistry)
                .build();
        mongoClient = MongoClients.create(settings);
    }

    public void clientClose() {
        mongoClient.close();
    }
}
