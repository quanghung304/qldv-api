package com.agribank.qldv_api.gateway;

import com.agribank.qldv_api.response.DefaultResponse;
import com.agribank.qldvutils.dto.RequestDto;
import com.agribank.qldvutils.entity.Request;
import com.agribank.qldvutils.request.FilterRequest;
import com.agribank.qldvutils.request.bcsl_report.dv.SearchRp10DataRequest;
import com.agribank.qldvutils.response.BaseResponse;
import com.agribank.qldvutils.response.PageResponse;
import com.agribank.qldvutils.response.bcsl_report.dv.BcslDvRp10Response;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;



@FeignClient(
        name = "requestClient",
        url = "${qldv.database.url}" + "/api/v1/request",
        configuration = DatabaseFeignConfiguration.class
)
public interface RequestClient extends BaseClient<Request, String> {
    @PostMapping("/list")
    DefaultResponse<PageResponse<RequestDto>> getRequestList(
            @RequestBody FilterRequest request
    );

    @GetMapping("/find-by-form-code")
    DefaultResponse<List<Request>> findByFormCode(
            @RequestParam(name = "formCode") String formCode
    );

    @GetMapping("/find-by-ref-id")
    DefaultResponse<Request> findByReferenceId(
            @RequestParam(name = "refId") String refId
    );

    @GetMapping("/{id}")
    BaseResponse<RequestDto> getDetail(
            @PathVariable String id
    );

    @PostMapping("search-report-10/count-develop")
    DefaultResponse<List<BcslDvRp10Response>> searchRp10(
            @RequestBody SearchRp10DataRequest request
    );
}
