package com.illunex.emsaasrestapi;

import org.apache.poi.util.IOUtils;
import org.modelmapper.ModelMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@SpringBootApplication
@EnableScheduling
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class EmSaasRestApiApplication {
    public static void main(String[] args) {
        IOUtils.setByteArrayMaxOverride(200 * 1024 * 1024); // 100MB
        SpringApplication.run(EmSaasRestApiApplication.class, args);
    }

    @Bean
    public ModelMapper modelMapper(){
        return new ModelMapper();
    }
}