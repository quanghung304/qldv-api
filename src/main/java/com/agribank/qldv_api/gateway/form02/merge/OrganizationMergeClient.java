package com.agribank.qldv_api.gateway.form02.merge;

import com.agribank.qldv_api.gateway.BaseClient;
import com.agribank.qldv_api.gateway.DatabaseFeignConfiguration;
import com.agribank.qldvutils.entity.form02.merge.OrganizationMerge;
import com.agribank.qldvutils.request.form02.*;
import com.agribank.qldvutils.response.BaseResponse;
import com.agribank.qldvutils.response.PageResponse;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "organizationMergeClient",
        url = "${qldv.database.url}" + "/api/v1/merge",
        configuration = DatabaseFeignConfiguration.class
)
public interface OrganizationMergeClient extends BaseClient<OrganizationMerge, String> {
    @PostMapping("/search/union")
    BaseResponse<PageResponse<OrganizationMerge>> searchUnion(
            @RequestBody SearchOrganizationUnionRequest request
    );

    @PutMapping("/save-entities")
    BaseResponse<Boolean> saveEntities(@RequestBody @Valid ApproveMergeRequest request);

    @PutMapping("/save-draft-entities")
    BaseResponse<Boolean> saveDraftEntities(@RequestBody @Valid MergeDraftRequest request);

    @PutMapping("/save-update-draft-entities")
    BaseResponse<Boolean> saveUpdateDraftEntities(@RequestBody @Valid MergeUpdateDraftRequest request);

    @PutMapping("/save-unify-entities")
    BaseResponse<Boolean> saveUnifyEntities(@RequestBody @Valid ApproveUnifyRequest request);

    @PutMapping("/update-entities")
    BaseResponse<Boolean> updateEntities(@RequestBody @Valid ApproveUpdateMergeRequest request);

    @PutMapping("/update-unify-entities")
    BaseResponse<Boolean> updateUnifyEntities(@RequestBody @Valid ApproveUpdateUnifyRequest request);
}
