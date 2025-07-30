package com.agribank.qldv_api.gateway.form02;

import com.agribank.qldv_api.gateway.BaseClient;
import com.agribank.qldv_api.gateway.DatabaseFeignConfiguration;
import com.agribank.qldvutils.dto.Report05BcdsDto;
import com.agribank.qldvutils.entity.form02.OrganizationHistory;
import com.agribank.qldvutils.request.form02.OrganizationHistoryRequest;
import com.agribank.qldvutils.request.report_tcd.SearchBCDSRequest;
import com.agribank.qldvutils.response.BaseResponse;
import com.agribank.qldvutils.response.PageResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(
        name = "OrganizationHistoryClient",
        url = "${qldv.database.url}" + "/api/v1/history",
        configuration = DatabaseFeignConfiguration.class
)
public interface OrganizationHistoryClient extends BaseClient<OrganizationHistory, String> {
    @GetMapping("find-by-refId")
    BaseResponse<OrganizationHistory> findByRefId(
            @RequestParam Integer type,
            @RequestParam String refId
    );

    @PostMapping("search-rp05")
    BaseResponse<PageResponse<Report05BcdsDto>> searchRp05(@RequestBody SearchBCDSRequest request);
}
