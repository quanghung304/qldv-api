package com.agribank.qldv_api.gateway.form02.updown;

import com.agribank.qldv_api.gateway.BaseClient;
import com.agribank.qldv_api.gateway.DatabaseFeignConfiguration;
import com.agribank.qldv_api.response.DefaultListResponse;
import com.agribank.qldvutils.entity.form02.updown.TransformationHistory;
import com.agribank.qldvutils.request.form02.TransformationHistoryRpRequest;
import com.agribank.qldvutils.request.form02.UpdownOrganizationFilterRequest;
import com.agribank.qldvutils.response.BaseResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

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

    @PostMapping("list")
    BaseResponse<Page<TransformationHistory>> getList(
            @RequestBody UpdownOrganizationFilterRequest request
    );
}
