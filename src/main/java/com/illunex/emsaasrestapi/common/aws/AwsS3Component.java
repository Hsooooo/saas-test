package com.illunex.emsaasrestapi.common.aws;

import com.illunex.emsaasrestapi.common.CustomException;
import com.illunex.emsaasrestapi.common.Utils;
import com.illunex.emsaasrestapi.project.document.excel.Excel;
import com.illunex.emsaasrestapi.project.document.excel.ExcelFile;
import com.illunex.emsaasrestapi.project.document.excel.ExcelSheet;
import com.illunex.emsaasrestapi.project.session.ProjectDraftRepository;
import io.awspring.cloud.s3.ObjectMetadata;
import io.awspring.cloud.s3.S3Exception;
import io.awspring.cloud.s3.S3Operations;
import io.awspring.cloud.s3.S3Resource;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.paginators.ListObjectsV2Iterable;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
@Component
public class AwsS3Component {
    private final String DELIMITER = "/";
    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucket;
    @Value("${spring.config.activate.on-profile}")
    private String folderName;
    @Value("${spring.cloud.aws.region.static}")
    private String region;
    private final S3Operations s3Operations;
    private final S3Client s3Client;
    private final ProjectDraftRepository draftRepo;

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
     * @param s3FileUrl
     * @return
     */
    public InputStream downloadInputStream(String s3FileUrl) {
        S3Resource s3Resource = s3Operations.download(bucket, s3FileUrl);
        try {
            return s3Resource.getInputStream();
        } catch (IOException e) {
            throw new RuntimeException("S3 파일 다운로드 실패: " + s3FileUrl, e);
        }
    }

    /**
     * s3파일 삭제
     * @param key
     */
    public void delete(String key) {
        s3Operations.deleteObject(bucket, key);
    }

    /**
     * prefix 하위 전체 삭제
     * - ListObjectsV2Paginator로 페이지 단위 조회
     * - 1000개씩 벌크 삭제
     * - 마지막에 '폴더 마커'도 삭제 시도
     */
    public int deleteFolder(String prefix) {
        String p = prefix.endsWith(DELIMITER) ? prefix : prefix + DELIMITER;

        ListObjectsV2Request listReq = ListObjectsV2Request.builder()
                .bucket(bucket)
                .prefix(p)
                .build();

        int deleted = 0;
        ListObjectsV2Iterable pages = s3Client.listObjectsV2Paginator(listReq);
        for (ListObjectsV2Response page : pages) {
            if (!page.hasContents()) continue;

            List<ObjectIdentifier> batch = new ArrayList<>(1000);
            for (S3Object obj : page.contents()) {
                batch.add(ObjectIdentifier.builder().key(obj.key()).build());
                if (batch.size() == 1000) {
                    deleted += bulkDelete(batch);
                    batch.clear();
                }
            }
            if (!batch.isEmpty()) deleted += bulkDelete(batch);
        }

        // 폴더 마커 삭제(없으면 무시)
        try { s3Operations.deleteObject(bucket, p); } catch (Exception ignore) {}
        return deleted;
    }

    private int bulkDelete(List<ObjectIdentifier> objects) {
        DeleteObjectsRequest delReq = DeleteObjectsRequest.builder()
                .bucket(bucket)
                .delete(Delete.builder().objects(objects).build())
                .build();
        DeleteObjectsResponse res = s3Client.deleteObjects(delReq);
        return (res.deleted() == null) ? 0 : res.deleted().size();
    }

    /**
     * Excel 메타에 포함된 모든 파일을 draft/{sid}/로 COPY하고,
     * filePath/url이 바뀐 Excel 객체를 돌려준다.
     */
    public Excel copyExcelMetaToDraftPrefix(
            Excel src,
            ObjectId sid
    ) {
        final String draftPrefixRoot = buildDraftPrefix(sid) + "/";

        var cloned = new Excel();
        cloned.setProjectIdx(src.getProjectIdx());
        cloned.setExcelFileList(new ArrayList<>());
        cloned.setExcelSheetList(new ArrayList<>());

        List<String> newKeys = new ArrayList<>();

        // 파일 레벨
        if (src.getExcelFileList() != null) {
            for (var f : src.getExcelFileList()) {
                String oldKey = f.getFilePath();
                String newKey = draftPrefixRoot + extractFileName(oldKey);
                copyObject(oldKey, newKey);

                var nf = new ExcelFile();
                nf.setFileName(f.getFileName());
                nf.setFilePath(newKey);
                nf.setFileUrl(buildUrl(newKey));
                nf.setFileSize(f.getFileSize());
                nf.setFileCd(f.getFileCd());
                nf.setCreateDate(f.getCreateDate());
                nf.setUpdateDate(f.getUpdateDate());
                cloned.getExcelFileList().add(nf);

                newKeys.add(newKey);
            }
        }

        // 시트 레벨 파일경로가 있다면 동일 처리
        if (src.getExcelSheetList() != null) {
            for (var s : src.getExcelSheetList()) {
                var ns = new ExcelSheet();
                ns.setExcelSheetName(s.getExcelSheetName());
                ns.setTotalRowCnt(s.getTotalRowCnt());

                if (s.getFilePath() != null) {
                    String newKey = draftPrefixRoot + extractFileName(s.getFilePath());
                    copyObject(s.getFilePath(), newKey);
                    ns.setFilePath(newKey);
                    newKeys.add(newKey);
                }
                cloned.getExcelSheetList().add(ns);
            }
        }

        draftRepo.addS3Keys(sid, newKeys);

        cloned.setCreateDate(src.getCreateDate());
        return cloned;
    }

    public int deleteObjects(Collection<String> keys) {
        if (keys == null || keys.isEmpty()) return 0;

        int deleted = 0;
        List<String> chunk = new ArrayList<>(1000);
        for (String k : keys) {
            chunk.add(k);
            if (chunk.size() == 1000) {
                deleted += deleteChunk(chunk);
                chunk.clear();
            }
        }
        if (!chunk.isEmpty()) deleted += deleteChunk(chunk);
        return deleted;
    }

    private int deleteChunk(List<String> keys) {
        var objects = keys.stream().map(k -> ObjectIdentifier.builder().key(k).build()).toList();
        var req = DeleteObjectsRequest.builder()
                .bucket(bucket)
                .delete(Delete.builder().objects(objects).build())
                .build();
        var res = s3Client.deleteObjects(req);
        return res.deleted() == null ? 0 : res.deleted().size();
    }

    private void copyObject(String oldKey, String newKey) {
        var req = software.amazon.awssdk.services.s3.model.CopyObjectRequest.builder()
                .sourceBucket(bucket)
                .sourceKey(oldKey)
                .destinationBucket(bucket)
                .destinationKey(newKey)
                .build();
        s3Client.copyObject(req);
    }

    private String extractFileName(String key) {
        int idx = key.lastIndexOf(DELIMITER);
        return (idx >= 0) ? key.substring(idx + 1) : key;
    }

    private String buildUrl(String key) {
        try {
            return "https://" + bucket + ".s3." + region + ".amazonaws.com/" + key;
        } catch (Exception e) {
            throw new RuntimeException("S3 URL 생성 실패: " + key, e);
        }
    }

    public String buildDraftPrefix(ObjectId sid) {
        return folderName + "/" + FolderType.ProjectFile.name() + "/draft/" + sid.toHexString();
    }
}
