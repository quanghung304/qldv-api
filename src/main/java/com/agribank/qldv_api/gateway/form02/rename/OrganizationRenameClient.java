package com.agribank.qldv_api.gateway.form02.rename;

import com.agribank.qldv_api.gateway.BaseClient;
import com.agribank.qldv_api.gateway.DatabaseFeignConfiguration;
import com.agribank.qldvutils.entity.form02.rename.OrganizationRename;
import com.agribank.qldvutils.request.form02.*;
import com.agribank.qldvutils.response.BaseResponse;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "OrganizationRenameClient",
        url = "${qldv.database.url}" + "/api/v1/rename",
        configuration = DatabaseFeignConfiguration.class
)
public interface OrganizationRenameClient extends BaseClient<OrganizationRename, String> {
    @PostMapping("list")
    BaseResponse<Page<OrganizationRename>> getList(@RequestBody OrganizationRenameFilterRequest request);

    @PutMapping("/save-entities")
    BaseResponse<Boolean> saveEntities(@RequestBody @Valid OrganizationRenameMetaRequest request);
}
