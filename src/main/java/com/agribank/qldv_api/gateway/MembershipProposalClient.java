package com.agribank.qldv_api.gateway;

import com.agribank.qldv_api.response.DefaultResponse;
import com.agribank.qldvutils.entity.MembershipProposal;
import com.agribank.qldvutils.request.bcsl_report.tcd.SearchRpRequest;
import com.agribank.qldvutils.request.membershipProposal.MPSearchRequest;
import com.agribank.qldvutils.response.BaseResponse;
import com.agribank.qldvutils.response.PageResponse;
import com.agribank.qldvutils.response.dv_report.DvRp22Response;
import com.agribank.qldvutils.response.dv_report.DvRp23Response;
import com.agribank.qldvutils.response.membershipProposal.MembershipProposalSearchResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@FeignClient(name = "membershipProposalClient",
        url = "${qldv.database.url}" + "/api/v1/membership-proposal",
        configuration = DatabaseFeignConfiguration.class)
public interface MembershipProposalClient extends BaseClient<MembershipProposal, String>{
    @PostMapping("/search")
    DefaultResponse<PageResponse<MembershipProposalSearchResponse>> search(
            @RequestBody MPSearchRequest requests
    );

    @GetMapping("/find-by-code/{code}")
    BaseResponse<MembershipProposal> findByStaffCode(
            @PathVariable String code
    );

    @PostMapping("/search-report-22")
    DefaultResponse<PageResponse<DvRp22Response>> searchRp22(
            @RequestBody SearchRpRequest request
    );

    @PostMapping("/search-report-23")
    DefaultResponse<PageResponse<DvRp23Response>> searchRp23(
            @RequestBody SearchRpRequest request
    );
}
