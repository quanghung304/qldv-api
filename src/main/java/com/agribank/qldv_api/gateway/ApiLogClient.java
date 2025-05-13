package com.agribank.qldv_api.gateway;

import com.agribank.qldv_api.request.apiLog.SearchApiLogRequest;
import com.agribank.qldv_api.response.DefaultResponse;
import com.agribank.qldvutils.entity.ApiLog;
import com.agribank.qldvutils.response.PageResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "apiLogClient", url = "${qldv.database.url}", configuration = DatabaseFeignConfiguration.class)
public interface ApiLogClient {
    @PostMapping("api/v1/api-log/save")
    DefaultResponse<ApiLog> save(
            @RequestBody ApiLog apiLog
    );

    @PostMapping("api/v1/api-log/search")
    DefaultResponse<PageResponse<ApiLog>> search(
            @RequestBody SearchApiLogRequest request
    );
}
