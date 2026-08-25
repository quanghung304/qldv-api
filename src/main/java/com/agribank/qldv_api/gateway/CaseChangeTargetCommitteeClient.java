package com.agribank.qldv_api.gateway;

import com.agribank.qldv_api.gateway.config.DatabaseFeignConfiguration;
import com.agribank.qldvutils.entity.CaseChangeTargetCommittee;
import com.agribank.qldvutils.entity.CaseChangeTargetCommitteeId;
import com.agribank.qldvutils.response.DefaultListResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "caseChangeTargetCommitteeClient", url = "${qldv.database.url}" + "/api/v1/case-change-target-committee", configuration = DatabaseFeignConfiguration.class)
public interface CaseChangeTargetCommitteeClient extends BaseClient<CaseChangeTargetCommittee, CaseChangeTargetCommitteeId> {
    @GetMapping("/find-by-case-change-target-ids")
    DefaultListResponse<CaseChangeTargetCommittee> findByCaseChangeTargetIds(@RequestParam List<String> caseChangeTargetIds);
}
