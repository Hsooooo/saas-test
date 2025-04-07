package com.illunex.emsaasrestapi.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sesv2.SesV2Client;

@Configuration
public class AwsConfig {
    @Value("${spring.cloud.aws.credentials.access-key}")
    private String accessKey;
    @Value("${spring.cloud.aws.credentials.secret-key}")
    private String secretKey;
    @Value("${spring.cloud.aws.region.static}")
    private String region;

    @Bean
    public SesV2Client getSes2Client() {
        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
        return SesV2Client.builder()
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .region(Region.of(region))
                .build();
    }
}
