package com.agribank.qldv_api.service.log;

import com.agribank.qldv_api.enums.EApiLogType;
import com.agribank.qldv_api.gateway.ApiLogClient;
import com.agribank.qldv_api.request.IAMRegisterRequest;
import com.agribank.qldv_api.service.UserService;
import com.agribank.qldvutils.entity.ApiLog;
import com.agribank.qldvutils.entity.User;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class AuthenticationLogService extends ApiLogBaseService{
    private final String OBJECT_REFERENCE = "Authentication";
    public AuthenticationLogService(ApiLogClient apiLogClient, UserService userService) {
        super(apiLogClient, userService);
    }

    public void writeLogRegister(List<IAMRegisterRequest> registerRequests) {
        ApiLog apiLog = initApiLog();
        apiLog.setObjectReference(OBJECT_REFERENCE);
        apiLog.setAction(EApiLogType.INSERT.getValue());
        apiLog.setDataType(EApiLogType.INSERT.name());
        ExecutorService executor = Executors.newFixedThreadPool(5);
        executor.submit(() -> {
            try {
                List<String> content = new ArrayList<>();
                for (IAMRegisterRequest registerRequest : registerRequests) {
                    content.add(String.format("Thêm mới User: username: <%s>, email: <%s>, fullName: <%s>, staffCode: <%s>, " +
                                    "jobPosition: <%s>, depId: <%s>, userKind: <%s>", registerRequest.getUsername(), registerRequest.getEmail(),
                            registerRequest.getFullName(), registerRequest.getStaffCode(),
                            registerRequest.getJobPosition(), registerRequest.getDepId(), registerRequest.getUserKind()));
                }
                apiLog.setDescription(String.join("\n", content));
                save(apiLog);
            }catch (Exception e) {
                System.out.println(e.getMessage());
            }
        });
        executor.shutdown();
    }
}
