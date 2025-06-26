package com.agribank.qldv_api.gateway;

import com.agribank.qldv_api.response.DefaultListResponse;
import com.agribank.qldvutils.entity.DVRecognition;
import com.agribank.qldvutils.entity.DvOrgHistory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(
        name = "DvOrgHistoryClient",
        url = "${qldv.database.url}" + "/api/v1/dv-org-history",
        configuration = DatabaseFeignConfiguration.class
)
public interface DvOrgHistoryClient extends BaseClient<DvOrgHistory, String>{
    @PostMapping("/staff-code-in")
    DefaultListResponse<DvOrgHistory> findByStaffCodes(
            @RequestBody List<String> staffCodes
    );

    @PostMapping("/old-org-code-in")
    DefaultListResponse<DvOrgHistory> findByOldOrgCodeIn(
            @RequestBody List<String> oldOrgCodes
    );
}
