package com.agribank.qldv_api.gateway;

import com.agribank.qldvutils.entity.DvDraft;
import com.agribank.qldvutils.response.DefaultListResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "dVDraftClient", url = "${qldv.database.url}" + "/api/v1/dv-draft", configuration = DatabaseFeignConfiguration.class)
public interface DVDraftClient extends BaseClient<DvDraft, String> {
    @PostMapping("find-by-vneids")
    DefaultListResponse<DvDraft> findByVneids(@RequestBody List<String> vneids);

    @PostMapping("find-by-party-card-numbers")
    DefaultListResponse<DvDraft> findByPartyCardNumbers(@RequestBody List<String> partyCardNumbers);

    @PostMapping("find-by-staff-codes")
    DefaultListResponse<DvDraft> findByStaffCodes(@RequestBody List<String> staffCodes);
}
