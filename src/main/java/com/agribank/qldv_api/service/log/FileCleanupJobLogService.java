package com.agribank.qldv_api.service.log;

import com.agribank.qldv_api.enums.EApiLogType;
import com.agribank.qldv_api.gateway.ApiLogClient;
import com.agribank.qldvutils.entity.ApiLog;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class FileCleanupJobLogService extends ApiLogBaseService{
    private final String OBJECT_REFERENCE = "FileCleanupJob";

    public FileCleanupJobLogService(ApiLogClient apiLogClient) {
        super(apiLogClient);
    }

    private void writeLog(String message) {
        ApiLog apiLog = ApiLog.builder()
                .username("SYSTEM")
                .email("SYSTEM")
                .brcd(1000)
                .build();
        apiLog.setObjectReference(OBJECT_REFERENCE);
        apiLog.setAction(EApiLogType.DELETE.getValue());
        apiLog.setDataType(EApiLogType.DELETE.name());
        ExecutorService executor = Executors.newFixedThreadPool(5);
        executor.submit(() -> {
            try {
                apiLog.setDescription(message);
                save(apiLog);
            }catch (Exception e) {
                System.out.println(e.getMessage());
            }
        });
        executor.shutdown();
    }

    public void writeLogFileSuccess(List<File> files) {
        String description = getDescription(files, "thành công");
        writeLog(description);
    }

    private String getDescription(List<File> files, String title){
        List<String> descriptions = new ArrayList<>();
        for (File file : files) {
            descriptions.add(String.format("Xóa %s file: <%s>, path: <%s>", title, file.getName(), file.getPath()));
        }

        return String.join("\n", descriptions);
    }

    public void writeLogFileFail(List<File> files) {
        String description = getDescription(files, "thất bại`");
        writeLog(description);
    }
}
