package com.agribank.qldv_api.gateway.import_file;

import com.agribank.qldv_api.gateway.BaseClient;
import com.agribank.qldv_api.gateway.DatabaseFeignConfiguration;
import com.agribank.qldv_api.response.DefaultResponse;
import com.agribank.qldvutils.entity.import_file.ImportFileConfigDetail;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "ImportFileConfigDetailClient",
        url = "${qldv.database.url}" + "/api/v1/import-file-config-detail",
        configuration = DatabaseFeignConfiguration.class)
public interface ImportFileConfigDetailClient extends BaseClient<ImportFileConfigDetail, String> {

    @GetMapping("/find-by-ref-id")
    DefaultResponse<List<ImportFileConfigDetail>> findByRefId(
            @RequestParam(name = "refId") String refId
    );
}
