package com.illunex.emsaasrestapi;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.info.BuildProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZonedDateTime;

@RestController
@Slf4j
@RequiredArgsConstructor
public class CommonController {

    private final BuildProperties buildProperties;

    @GetMapping
    public String getRootRequest(@Value("${server.version}") String version) {
        return buildProperties.getName() + " / " + version + " / " + ZonedDateTime.now();
    }
}