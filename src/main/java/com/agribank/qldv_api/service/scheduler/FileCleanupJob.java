package com.agribank.qldv_api.service.scheduler;

import com.agribank.qldv_api.service.log.FileCleanupJobLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class FileCleanupJob {
    @Value(value = "${zip.folder.path}")
    private String TARGET_DIR;

    @Value(value = "${zip.folder.day.threshould}")
    private Integer DAYS_THRESHOLD;

    private final FileCleanupJobLogService fileCleanupJobLogService;

    // Chạy mỗi ngày lúc 3 giờ sáng
    @Scheduled(cron = "0 0 3 * * *")
    public void deleteOldFiles() {
        File folder = new File(TARGET_DIR);
        if (!folder.exists() || !folder.isDirectory()) {
            System.err.println("Thư mục không tồn tại: " + TARGET_DIR);
            return;
        }
        Instant cutoffTime = Instant.now().minus(DAYS_THRESHOLD, ChronoUnit.DAYS);
        File[] files = folder.listFiles();

        if (Objects.isNull(files)) return;

        List<File> fileSuccess = new ArrayList<>();
        List<File> fileFail = new ArrayList<>();
        for (File file : files) {
            if (!file.isFile()) {
                continue;
            }
            Instant fileModified = Instant.ofEpochMilli(file.lastModified());
            if (!fileModified.isBefore(cutoffTime)) {
                continue;
            }
            boolean deleted = file.delete();
            if (deleted) {
                System.out.println("Đã xóa: " + file.getAbsolutePath());
                fileSuccess.add(file);
            } else {
                fileFail.add(file);
                System.err.println("Không thể xóa: " + file.getAbsolutePath());
            }
        }

        if (!fileSuccess.isEmpty()){
            fileCleanupJobLogService.writeLogFileSuccess(fileSuccess);
        }

        if (!fileFail.isEmpty()){
            fileCleanupJobLogService.writeLogFileFail(fileFail);
        }
    }
}
