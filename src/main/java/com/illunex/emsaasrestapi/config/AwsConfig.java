package com.illunex.emsaasrestapi.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sesv2.SesV2Client;

@Configuration
public class AwsConfig {

    @Configuration
    @Profile("prod")
    static class ProdAwsConfig {
        @Value("${spring.cloud.aws.region.static}")
        private String region;

        @Bean
        public SesV2Client sesClientProd() {
            return SesV2Client.builder()
                    .region(Region.of(region))
                    .credentialsProvider(DefaultCredentialsProvider.create())
                    .build();
        }
    }

    @Configuration
    @Profile("!prod")
    static class DevAwsConfig {
        @Value("${spring.cloud.aws.credentials.access-key}")
        private String accessKey;

        @Value("${spring.cloud.aws.credentials.secret-key}")
        private String secretKey;

        @Value("${spring.cloud.aws.region.static}")
        private String region;

        @Bean
        public SesV2Client sesClientDev() {
            AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
            return SesV2Client.builder()
                    .region(Region.of(region))
                    .credentialsProvider(StaticCredentialsProvider.create(credentials))
                    .build();
        }
    }
}
