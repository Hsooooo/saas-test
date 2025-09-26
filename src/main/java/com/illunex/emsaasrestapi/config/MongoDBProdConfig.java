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
                    b.readTimeout((int) Duration.ofSeconds(15).toMillis(), TimeUnit.MILLISECONDS);
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
        // DB 이름은 URI의 path(dbname)를 사용
        return new SimpleMongoClientDatabaseFactory(mongoClient, new ConnectionString(mongoUri).getDatabase());
    }

    @Bean
    public MongoTemplate mongoTemplate(MongoDatabaseFactory factory, MongoConverter converter) {
        MongoTemplate mongoTemplate = new MongoTemplate(factory, converter);
        mongoTemplate.setWriteResultChecking(WriteResultChecking.EXCEPTION); // 쓰기 실패 시 예외
        mongoTemplate.setReadPreference(ReadPreference.secondaryPreferred()); // 읽기 부하분산
        mongoTemplate.setWriteConcern(WriteConcern.ACKNOWLEDGED);            // 필요 시 .MAJORITY로 상향
        return mongoTemplate;
    }
}
