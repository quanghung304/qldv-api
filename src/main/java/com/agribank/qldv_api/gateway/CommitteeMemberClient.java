package com.agribank.qldv_api.gateway;

import com.agribank.qldv_api.gateway.config.DatabaseFeignConfiguration;
import com.agribank.qldvutils.entity.CommitteeMember;
import com.agribank.qldvutils.entity.CommitteeMemberId;
import com.agribank.qldvutils.response.DefaultListResponse;
import com.agribank.qldvutils.response.organization.CommitteeMemberResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "committeeMemberClient", url = "${qldv.database.url}" + "/api/v1/committee-member", configuration = DatabaseFeignConfiguration.class)
public interface CommitteeMemberClient extends BaseClient<CommitteeMember, CommitteeMemberId> {
    @GetMapping("/find-by-organization-id")
    DefaultListResponse<CommitteeMemberResponse> findByOrganizationIdAndStatus(@RequestParam String organizationId, @RequestParam Integer status);
}
