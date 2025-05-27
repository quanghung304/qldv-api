package com.agribank.qldv_api.gateway;

import com.agribank.qldv_api.request.organizationDraft.OrganizationDraftSearchRequest;
import com.agribank.qldv_api.response.DefaultResponse;
import com.agribank.qldvutils.entity.OrganizationDraft;
import com.agribank.qldvutils.entity.TransformationHistoryDraft;
import com.agribank.qldvutils.response.PageResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "organizationDraftClient", url = "${qldv.database.url}" + "/api/v1/organization-draft", configuration = DatabaseFeignConfiguration.class)
public interface OrganizationDraftClient extends BaseClient<OrganizationDraft, String>{
}
