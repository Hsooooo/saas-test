package com.illunex.emsaasrestapi.common.aws.dto;

import io.awspring.cloud.s3.S3Resource;
import lombok.Builder;
import lombok.Getter;

import java.io.IOException;

@Getter
public class AwsS3ResourceDTO {
    private String fileName;
    private String orgFileName;
    private String url;
    private String path;
    private Long size;

    @Builder
    public AwsS3ResourceDTO(S3Resource s3Resource, String fileName) throws IOException {
        this.fileName = s3Resource.getFilename();
        this.orgFileName = fileName;
        this.url = s3Resource.getURL().toString();
        this.path = s3Resource.getLocation().getObject();
        this.size = s3Resource.contentLength();
    }
}
