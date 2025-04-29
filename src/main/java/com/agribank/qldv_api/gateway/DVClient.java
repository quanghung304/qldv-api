package com.agribank.qldv_api.gateway;

import com.agribank.qldv_api.request.dv.SearchDVRequest;
import com.agribank.qldv_api.response.DefaultResponse;
import com.agribank.qldvutils.entity.DV;
import com.agribank.qldvutils.response.PageResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "dvClient", url = "${qldv.database.url}", configuration = DatabaseFeignConfiguration.class)
public interface DVClient {

    @GetMapping("api/v1/dv//find-by-id/{id}")
    DefaultResponse<DV> findById(
            @PathVariable("id") String id
    );

    @PostMapping("api/v1/dv/search")
    DefaultResponse<PageResponse<DV>> search(
            @RequestBody SearchDVRequest request
    );

    @PostMapping("api/v1/dv/save")
    DefaultResponse<DV> save(
            @RequestBody DV request
    );

    @PostMapping("api/v1/dv/save/all")
    DefaultResponse<List<DV>> saveAll(
            @RequestBody List<DV> dvs
    );
}
