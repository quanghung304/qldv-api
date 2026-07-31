package com.agribank.qldv_api.storage;

import com.agribank.qldvutils.exception.CommonException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

/** S3Service DÙNG CHUNG toàn dự án — mọi nghiệp vụ cần đọc/ghi file lên MinIO PHẢI tái sử dụng bean này. */
@Service
@RequiredArgsConstructor
public class S3Service {
    private final S3Client s3Client;
    private final StorageProperties storageProperties;

    public void uploadObject(String key, byte[] content, String contentType) {
        s3Client.putObject(PutObjectRequest.builder()
                        .bucket(storageProperties.getBucketName())
                        .key(key)
                        .contentType(contentType)
                        .build(),
                RequestBody.fromBytes(content));
    }

    public byte[] downloadObject(String key) {
        try {
            return s3Client.getObjectAsBytes(GetObjectRequest.builder()
                    .bucket(storageProperties.getBucketName())
                    .key(key)
                    .build()).asByteArray();
        } catch (NoSuchKeyException e) {
            throw new CommonException("Không tìm thấy file trên storage: " + key);
        }
    }

    public void deleteObject(String key) {
        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(storageProperties.getBucketName())
                .key(key)
                .build());
    }

    public boolean objectExists(String key) {
        try {
            s3Client.headObject(HeadObjectRequest.builder()
                    .bucket(storageProperties.getBucketName())
                    .key(key)
                    .build());
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        } catch (S3Exception e) {
            if (e.statusCode() == 404) {
                return false;
            }
            throw e;
        }
    }
}
