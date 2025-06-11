package com.agribank.qldv_api.gateway.import_file;

import com.agribank.qldv_api.gateway.BaseClient;
import com.agribank.qldv_api.gateway.DatabaseFeignConfiguration;
import com.agribank.qldv_api.response.DefaultResponse;
import com.agribank.qldvutils.entity.import_file.ImportFileConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "ImportFileConfigClient",
        url = "${qldv.database.url}" + "/api/v1/import-file-config",
        configuration = DatabaseFeignConfiguration.class)
public interface ImportFileConfigClient extends BaseClient<ImportFileConfig, String> {

    @GetMapping("/find-by-code")
    DefaultResponse<ImportFileConfig> findByCode(
            @RequestParam(name = "code") String code
    );
}
