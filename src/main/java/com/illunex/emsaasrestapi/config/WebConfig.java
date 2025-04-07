package com.illunex.emsaasrestapi.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${server.cors-list}")
    private String[] corsList;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
       registry.addMapping("/**")
               .exposedHeaders("X-AUTH-TOKEN")
               .allowCredentials(true)
               .allowedOrigins(corsList)
               .allowedMethods("*")
               .maxAge(3000);
    }
}
