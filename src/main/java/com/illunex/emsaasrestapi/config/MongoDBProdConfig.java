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

    private static final int CONNECT_TIMEOUT_MS = (int) Duration.ofSeconds(10).toMillis();
    private static final int READ_TIMEOUT_MS    = (int) Duration.ofSeconds(180).toMillis(); // 핵심: 15s → 180s
    private static final int SOCKET_TIMEOUT_MS  = READ_TIMEOUT_MS; // 동치로 운용
    private static final int SERVER_SELECT_MS   = (int) Duration.ofSeconds(10).toMillis();
    private static final int HEARTBEAT_MS       = (int) Duration.ofSeconds(10).toMillis();
    private static final int POOL_MAX_SIZE      = 150;   // 피크 대비 상향
    private static final int POOL_MIN_SIZE      = 10;
    private static final int POOL_MAX_CONNECT   = 10;    // 동시 연결 생성 상향
    private static final int POOL_MAX_WAIT_MS   = (int) Duration.ofSeconds(30).toMillis(); // 5s → 30s


    @Bean
    public MongoClient mongoClient() {
        ConnectionString cs = new ConnectionString(mongoUri);

        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(cs) // URI 파라미터 선적용
                .serverApi(ServerApi.builder().version(ServerApiVersion.V1).build())
                .retryReads(true)
                .retryWrites(true)
                .applyToSocketSettings(b -> {
                    b.connectTimeout(CONNECT_TIMEOUT_MS, TimeUnit.MILLISECONDS);
                    b.readTimeout(READ_TIMEOUT_MS, TimeUnit.MILLISECONDS);     // = socketTimeoutMS
                })
                .applyToClusterSettings(b -> {
                    b.serverSelectionTimeout(SERVER_SELECT_MS, TimeUnit.MILLISECONDS);
                    b.localThreshold(15, TimeUnit.MILLISECONDS);
                })
                .applyToServerSettings(b -> b.heartbeatFrequency(HEARTBEAT_MS, TimeUnit.MILLISECONDS))
                .applyToConnectionPoolSettings(b -> {
                    b.maxSize(POOL_MAX_SIZE);
                    b.minSize(POOL_MIN_SIZE);
                    b.maxConnecting(POOL_MAX_CONNECT);
                    b.maxWaitTime(POOL_MAX_WAIT_MS, TimeUnit.MILLISECONDS);
                })
                .compressorList(List.of(
                        MongoCompressor.createZstdCompressor(),
                        MongoCompressor.createSnappyCompressor()
                ))
                .build();

        return MongoClients.create(settings);
    }

    @Bean
    public MongoDatabaseFactory mongoDatabaseFactory(MongoClient mongoClient) {
        return new SimpleMongoClientDatabaseFactory(mongoClient, new ConnectionString(mongoUri).getDatabase());
    }

    @Bean
    public MongoTemplate mongoTemplate(MongoDatabaseFactory factory, MongoConverter converter) {
        MongoTemplate template = new MongoTemplate(factory, converter);

        template.setReadPreference(ReadPreference.primary());
        template.setWriteConcern(WriteConcern.ACKNOWLEDGED);
        template.setWriteResultChecking(WriteResultChecking.EXCEPTION);

        return template;
    }
}
