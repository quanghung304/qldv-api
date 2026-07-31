package com.agribank.qldv_api.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;

/** Kiểm tra bucket S3/MinIO tồn tại lúc khởi động — chỉ log ERROR, KHÔNG tự động tạo bucket. */
@Slf4j
@Component
@RequiredArgsConstructor
public class StorageBucketCheckRunner implements CommandLineRunner {
    private final S3Client s3Client;
    private final StorageProperties storageProperties;

    @Override
    public void run(String... args) {
        try {
            s3Client.headBucket(HeadBucketRequest.builder()
                    .bucket(storageProperties.getBucketName())
                    .build());
        } catch (Exception e) {
            log.error("Bucket S3 '{}' chưa tồn tại hoặc không truy cập được: {}",
                    storageProperties.getBucketName(), e.getMessage());
        }
    }
}
