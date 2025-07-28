package com.agribank.qldv_api.gateway;

import com.agribank.qldv_api.response.DefaultListResponse;
import com.agribank.qldvutils.entity.DvOrgHistory;
import com.agribank.qldvutils.request.dv_org.DvOrgHisRequest;
import com.agribank.qldvutils.request.dv_org.DvOrgHistoryRequest;
import com.agribank.qldvutils.request.dv_org_history.DvOrganizationHisRequest;
import jakarta.validation.Valid;
import com.agribank.qldvutils.request.dv_org_history.DvOrgHisListRequest;
import com.agribank.qldvutils.response.BaseResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;


@FeignClient(
        name = "DvOrgHistoryClient",
        url = "${qldv.database.url}" + "/api/v1/dv-org-history",
        configuration = DatabaseFeignConfiguration.class
)
public interface DvOrgHistoryClient extends BaseClient<DvOrgHistory, String>{
    @PostMapping("/old-org-code-in")
    DefaultListResponse<DvOrgHistory> findByOldOrgCodeIn(
           @Valid @RequestBody DvOrgHisRequest request
    );

    @GetMapping("/find-ref-id-and-organization-code")
    DefaultListResponse<DvOrgHistory> findByNewOrgCodeAndRefId(
            @RequestParam String organizationCode,
            @RequestParam String referenceId
    );

    @PostMapping("/find-by-staff-code")
    BaseResponse<DvOrgHistory> findByStaffCodeAndRefId(
            @RequestBody DvOrgHistoryRequest request
    );

    @PostMapping("/org-his")
    DefaultListResponse<DvOrgHistory> getOrgHis(@RequestBody DvOrganizationHisRequest request);

    @GetMapping("/find-by-ref-id")
    DefaultListResponse<DvOrgHistory> findByRefId(
            @RequestParam String referenceId
    );

    @PostMapping("/find-by-staff-codes")
    DefaultListResponse<DvOrgHistory> findByStaffCodesAndRefId(
            @RequestBody DvOrgHisListRequest request
    );

    @PostMapping("/staff-code-in")
    DefaultListResponse<DvOrgHistory> findByStaffCodes(
            @RequestBody List<String> staffCodes
    );

}
