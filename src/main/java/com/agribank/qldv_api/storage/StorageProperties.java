package com.agribank.qldv_api.storage;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Đọc cấu hình S3/MinIO có sẵn trong application.properties (khóa {@code s3.*}, đã được cấu
 * hình từ trước — KHÔNG đổi sang tiền tố {@code storage.s3.*} để khỏi phải cấp lại biến môi
 * trường .env đã trỏ tới endpoint MinIO thật).
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "s3")
public class StorageProperties {
    private String endpoint;
    private String bucketName;
    private String projectPrefix;
    private String accessKey;
    private String secretKey;
}
