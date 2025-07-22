package com.agribank.qldv_api.gateway.form02.rename;

import com.agribank.qldv_api.gateway.BaseClient;
import com.agribank.qldv_api.gateway.DatabaseFeignConfiguration;
import com.agribank.qldvutils.entity.form02.rename.OrganizationRenameDraft;
import com.agribank.qldvutils.response.DefaultListResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;




@FeignClient(
        name = "OrganizationRenameDraftClient",
        url = "${qldv.database.url}" + "/api/v1/rename-draft",
        configuration = DatabaseFeignConfiguration.class
)
public interface OrganizationRenameDraftClient extends BaseClient<OrganizationRenameDraft, String> {
    @GetMapping("/find-pending/{code}")
    DefaultListResponse<OrganizationRenameDraft> findPendingDraftByCode(@PathVariable String code);
}
