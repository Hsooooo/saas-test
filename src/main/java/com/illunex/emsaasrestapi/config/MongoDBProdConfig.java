package com.illunex.emsaasrestapi.config;

import com.mongodb.*;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.data.mongodb.core.WriteResultChecking;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableMongoRepositories
@RequiredArgsConstructor
@Profile("prod")
public class MongoDBProdConfig {

    @Value("${spring.data.mongodb.uri}")
    private String mongoUri;

    @Bean
    public MongoClient mongoClient() {
        ConnectionString cs = new ConnectionString(mongoUri);
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(cs)
                .serverApi(ServerApi.builder().version(ServerApiVersion.V1).build())
                .retryReads(true)
                .applyToSocketSettings(b -> {
                    b.connectTimeout((int) Duration.ofSeconds(10).toMillis(), TimeUnit.MILLISECONDS);
                    b.readTimeout((int) Duration.ofSeconds(180).toMillis(), TimeUnit.MILLISECONDS); // = socketTimeoutMS
                })
                .applyToConnectionPoolSettings(b -> {
                    b.maxSize(100);
                    b.minSize(10);
                    b.maxConnecting(5);
                    b.maxWaitTime((int) Duration.ofSeconds(5).toMillis(), TimeUnit.MILLISECONDS);
                })
                .build();
        return MongoClients.create(settings);
    }

    @Bean
    public MongoDatabaseFactory mongoDatabaseFactory(MongoClient mongoClient) {
        return new SimpleMongoClientDatabaseFactory(mongoClient, new ConnectionString(mongoUri).getDatabase());
    }

    @Bean
    public MongoTemplate mongoTemplate(MongoDatabaseFactory factory, MongoConverter converter) {
        MongoTemplate mongoTemplate = new MongoTemplate(factory, converter);
        mongoTemplate.setWriteResultChecking(WriteResultChecking.EXCEPTION);
        mongoTemplate.setReadPreference(ReadPreference.secondaryPreferred());
        mongoTemplate.setWriteConcern(WriteConcern.ACKNOWLEDGED);
        return mongoTemplate;
    }
}
