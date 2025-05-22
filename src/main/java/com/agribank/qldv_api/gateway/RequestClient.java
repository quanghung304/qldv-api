package com.agribank.qldv_api.gateway;

import com.agribank.qldv_api.response.DefaultResponse;
import com.agribank.qldvutils.dto.RequestDto;
import com.agribank.qldvutils.entity.Request;
import com.agribank.qldvutils.request.FilterRequest;
import com.agribank.qldvutils.response.PageResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;


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
}
