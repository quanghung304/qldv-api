package com.agribank.qldv_api.service.log;

import com.agribank.qldv_api.enums.EAction;
import com.agribank.qldv_api.gateway.ApiLogClient;
import com.agribank.qldv_api.request.IAMRegisterRequest;
import com.agribank.qldvutils.entity.ApiLog;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class AuthenticationLogService extends ApiLogBaseService{
    private final String OBJECT_REFERENCE = "Authentication";
    public AuthenticationLogService(ApiLogClient apiLogClient) {
        super(apiLogClient);
    }

    public void writeLogRegister(List<IAMRegisterRequest> registerRequests) {
        ApiLog apiLog = initApiLog();
        apiLog.setObjectReference(OBJECT_REFERENCE);
        apiLog.setAction(EAction.INSERT.getValue());
        apiLog.setDataType(EAction.INSERT.name());
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
