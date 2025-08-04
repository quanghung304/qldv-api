package com.agribank.qldv_api.gateway.report26;

import com.agribank.qldv_api.gateway.BaseClient;
import com.agribank.qldv_api.gateway.IamFeignConfiguration;
import com.agribank.qldv_api.response.DefaultResponse;
import com.agribank.qldvutils.entity.report26.Report26;
import com.agribank.qldvutils.request.bcsl_report.tcd.SearchRpRequest;
import com.agribank.qldvutils.request.report26.Report26SearchRequest;
import com.agribank.qldvutils.response.PageResponse;
import com.agribank.qldvutils.response.report26.Report26DtoResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "report26Client", url = "${qldv.database.url}" + "/api/v1/report26", configuration = IamFeignConfiguration.class)
public interface Report26Client extends BaseClient<Report26, String> {
    @PostMapping("/search")
    DefaultResponse<PageResponse<Report26DtoResponse>> search(
            @RequestBody Report26SearchRequest request
    );

    @GetMapping("/find-by-ref-id")
    DefaultResponse<Report26> findByRefId(
            @RequestParam(name = "refId") String refId
    );
    @PostMapping("/search-report-34")
    DefaultResponse<PageResponse<Report26>> searchRp34(
            @RequestBody SearchRpRequest request
    );

}
