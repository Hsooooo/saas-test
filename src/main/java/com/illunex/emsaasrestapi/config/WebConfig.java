package com.illunex.emsaasrestapi.config;

import com.illunex.emsaasrestapi.common.CurrentMemberArgumentResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final String[] corsList;

    private final CurrentMemberArgumentResolver currentMemberArgumentResolver;

    public WebConfig(
            @Value("${server.cors-list}") String[] corsList,
            CurrentMemberArgumentResolver currentMemberArgumentResolver
    ) {
        this.corsList = corsList;
        this.currentMemberArgumentResolver = currentMemberArgumentResolver;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
       registry.addMapping("/**")
               .exposedHeaders("X-AUTH-TOKEN")
               .allowCredentials(true)
               .allowedOrigins(corsList)
               .allowedMethods("*")
               .maxAge(3000);
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(currentMemberArgumentResolver);
    }
}
