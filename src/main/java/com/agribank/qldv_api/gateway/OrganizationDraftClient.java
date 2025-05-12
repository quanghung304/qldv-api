package com.agribank.qldv_api.gateway;

import com.agribank.qldv_api.request.organizationDraft.OrganizationDraftSearchRequest;
import com.agribank.qldv_api.response.DefaultResponse;
import com.agribank.qldvutils.entity.OrganizationDraft;
import com.agribank.qldvutils.response.PageResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "organizationDraftClient", url = "${qldv.database.url}", configuration = DatabaseFeignConfiguration.class)
public interface OrganizationDraftClient {
    @PostMapping("api/v1/organization-draft/save")
    DefaultResponse<OrganizationDraft> save(
            @RequestBody OrganizationDraft request
    );

    @PostMapping("api/v1/organization-draft/find-all-by-id")
    DefaultResponse<List<OrganizationDraft>> findAllById(
            @RequestBody List<String> ids
    );

    @PostMapping("api/v1/organization-draft/save/all")
    DefaultResponse<List<OrganizationDraft>> saveAll(
            @RequestBody List<OrganizationDraft> requests
    );

    @PostMapping("api/v1/organization-draft/search")
    DefaultResponse<PageResponse<OrganizationDraft>> search(
            @RequestBody OrganizationDraftSearchRequest requests
    );
}
