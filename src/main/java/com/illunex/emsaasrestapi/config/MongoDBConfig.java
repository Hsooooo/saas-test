package com.illunex.emsaasrestapi.config;

import com.mongodb.ReadPreference;
import com.mongodb.WriteConcern;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.WriteResultChecking;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories
@RequiredArgsConstructor
public class MongoDBConfig {

    @Bean
    public MongoTemplate mongoTemplate(MongoDatabaseFactory factory, MongoConverter converter) {
        MongoTemplate mongoTemplate = new MongoTemplate(factory, converter);
        mongoTemplate.setWriteResultChecking(WriteResultChecking.EXCEPTION);        // 쓰기 실패 시 Exception 발생
        mongoTemplate.setReadPreference(ReadPreference.secondaryPreferred());
        mongoTemplate.setWriteConcern(WriteConcern.ACKNOWLEDGED);
        return mongoTemplate;
    }
}
