package com.agribank.qldv_api.gateway;

import com.agribank.qldv_api.response.DefaultResponse;
import com.agribank.qldvutils.entity.DV;
import com.agribank.qldvutils.request.SearchDVRequest;
import com.agribank.qldvutils.response.DefaultListResponse;
import com.agribank.qldvutils.response.PageResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "dvClient", url = "${qldv.database.url}", configuration = DatabaseFeignConfiguration.class)
public interface DVClient extends BaseClient<DV, String> {
    @PostMapping("api/v1/dv/search")
    DefaultResponse<PageResponse<DV>> search(
            @RequestBody SearchDVRequest request
    );

    @PostMapping("api/v1/dv/save/all")
    DefaultListResponse<DV> saveAll(
            @RequestBody List<DV> requests
    );
}
