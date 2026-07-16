package com.agribank.qldv_api.service.log;

import com.agribank.qldv_api.enums.EApiLogType;
import com.agribank.qldv_api.gateway.ApiLogClient;
import com.agribank.qldvutils.entity.ApiLog;
import com.agribank.qldvutils.entity.User;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@Service
public class UserLogService extends ApiLogBaseService{
    private final List<String> IGNORED_PROPERTIES = Arrays.asList("id", "idIam", "updatedAt");
    private final String OBJECT_REFERENCE = "USER";
    public UserLogService(ApiLogClient apiLogClient) {
        super(apiLogClient);
    }

    public void handlerWriteLogUpdate(User userOld, User userNew){
        ApiLog apiLog = initApiLog();
        ExecutorService executor = Executors.newFixedThreadPool(5);
        executor.submit(() -> {
            try {
                apiLog.setObjectReference(OBJECT_REFERENCE);
                apiLog.setReferenceId(userOld.getId());
                apiLog.setAction(EApiLogType.UPDATE.getValue());
                apiLog.setDataType(EApiLogType.UPDATE.name());
                List<Field> propertyChange = handleGetPropertyChange(userNew, userOld, User.class, IGNORED_PROPERTIES);
                List<String> propertyCustom = List.of();
                String description = handleBuildContent(userNew, userOld, User.class, propertyChange, propertyCustom);
                apiLog.setDescription(description);
                save(apiLog);
            }catch (Exception ex){
                System.out.println(ex.getMessage());
            }
        });
        executor.shutdown();
    }

    public void handlerWriteLogDelete(User user) {
        ApiLog apiLog = initApiLog();
        apiLog.setObjectReference(OBJECT_REFERENCE);
        apiLog.setAction(EApiLogType.DELETE.getValue());
        apiLog.setDataType(EApiLogType.DELETE.name());
        ExecutorService executor = Executors.newFixedThreadPool(5);
        executor.submit(() -> {
            try {
                String content = String.format("User: id: <%s>, username: <%s>", user.getId(), user.getUsername());
                apiLog.setDescription(content);
                save(apiLog);
            }catch (Exception e) {
                System.out.println(e.getMessage());
            }
        });
        executor.shutdown();
    }
}
