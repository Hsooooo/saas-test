package com.illunex.emsaasrestapi.common.aws;

import com.illunex.emsaasrestapi.common.CustomException;
import com.illunex.emsaasrestapi.common.Utils;
import io.awspring.cloud.s3.ObjectMetadata;
import io.awspring.cloud.s3.S3Operations;
import io.awspring.cloud.s3.S3Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

import java.io.IOException;
import java.io.InputStream;

@RequiredArgsConstructor
@Component
public class AwsS3Component {
    private final String DELIMITER = "/";
    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucket;
    @Value("${spring.config.activate.on-profile}")
    private String folderName;
    private final S3Operations s3Operations;
    private final S3Client s3Client;

    public enum FolderType {
        PartnershipMember,
        ProjectFile,
        ProjectImage,
        LLMGeneratedPPTX,
    }

    /**
     * s3파일 업로드
     * @param multipartFile
     * @param folderType
     * @param detailPath
     * @return
     * @throws IOException
     * @throws CustomException
     */
    public S3Resource upload(MultipartFile multipartFile, FolderType folderType, String detailPath) throws IOException {
        try (InputStream is = multipartFile.getInputStream()){
            // 파일 올릴 전체 경로 : {루트폴더(profile명)} / {폴더 타입} / {세부 경로} / {UUID파일명}.{확장자}
            String fileKey = folderName + DELIMITER + folderType.name() + DELIMITER + detailPath + DELIMITER + Utils.createFileName(multipartFile.getOriginalFilename());
            return s3Operations.upload(bucket, fileKey, is, ObjectMetadata.builder().contentType(multipartFile.getContentType()).build());
        }
    }

    public S3Resource upload(InputStream is, FolderType folderType, String detailPath, String contentType, String fileName) throws IOException {
        String fileKey = folderName + DELIMITER + folderType.name() + DELIMITER + detailPath + DELIMITER + Utils.createFileName(fileName);
        return s3Operations.upload(bucket, fileKey, is, ObjectMetadata.builder().contentType(contentType).build());
    }

    /**
     * s3파일 다운로드
     * @param s3FileUrl
     * @return
     */
    public ResponseEntity<?> download(String s3FileUrl) {
        S3Resource s3Resource = s3Operations.download(bucket, s3FileUrl);
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(s3Resource.contentType())).body(s3Resource);
    }

    /**
     * S3 파일 다운로드(InputStream)
     * @param s3FileKey
     * @return
     */
    public InputStream downloadInputStream(String s3FileKey) {
        try {
            GetObjectRequest req = GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(s3FileKey)
                    .build();
            // ResponseInputStream<GetObjectResponse> 는 InputStream 구현체
            return s3Client.getObject(req);
        } catch (Exception e) {
            throw new RuntimeException("S3 파일 다운로드 실패: " + s3FileKey, e);
        }
    }

    /**
     * s3파일 삭제
     * @param key
     */
    public void delete(String key) {
        s3Operations.deleteObject(bucket, key);
    }
}
