package com.agribank.qldv_api.gateway;


import com.agribank.qldv_api.gateway.config.DatabaseFeignConfiguration;
import com.agribank.qldv_api.response.DefaultResponse;
import com.agribank.qldvutils.entity.ExcelColumnInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(
        name = "ExcelColumnInfoClient",
        url = "${qldv.database.url}" + "/api/v1/excel-column-info",
        configuration = DatabaseFeignConfiguration.class
)
public interface ExcelColumnInfoClient extends BaseClient<ExcelColumnInfo, String>{

    @GetMapping("/find-by-code")
    DefaultResponse<List<ExcelColumnInfo>> findByCode(@RequestParam("code") String code);
}
