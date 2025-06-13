package com.agribank.qldv_api.gateway;

import com.agribank.qldv_api.response.DefaultListResponse;
import com.agribank.qldvutils.entity.TransformationHistory;
import com.agribank.qldvutils.request.transformation_history.TransformationHistoryRpRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(
        name = "TransformationHistoryClient",
        url = "${qldv.database.url}" + "/api/v1/history",
        configuration = DatabaseFeignConfiguration.class
)
public interface TransformationHistoryClient extends BaseClient<TransformationHistory, String> {

    @PostMapping("/organization-code-and-date")
    DefaultListResponse<TransformationHistory> findByOrganizationCodeAndDate(
            @RequestBody TransformationHistoryRpRequest request
    );
}
