package com.agribank.qldv_api.gateway;

import com.agribank.qldv_api.gateway.config.DatabaseFeignConfiguration;
import com.agribank.qldvutils.entity.CaseEstablishmentCommittee;
import com.agribank.qldvutils.entity.CaseEstablishmentCommitteeId;
import com.agribank.qldvutils.response.DefaultListResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Chỉ dùng để ĐỌC (GET /cases/{id}/establishment) — thao tác ghi (xóa/ghi lại danh sách cấp ủy
 * dự kiến) đã gộp vào {@code CaseEstablishmentClient.persist(...)}, chạy trong transaction ở
 * qldv-db (CaseEstablishmentPersistService), KHÔNG gọi save/delete trực tiếp qua client này.
 */
@FeignClient(name = "caseEstablishmentCommitteeClient", url = "${qldv.database.url}" + "/api/v1/case-establishment-committee", configuration = DatabaseFeignConfiguration.class)
public interface CaseEstablishmentCommitteeClient extends BaseClient<CaseEstablishmentCommittee, CaseEstablishmentCommitteeId> {
    @GetMapping("/find-by-case-id")
    DefaultListResponse<CaseEstablishmentCommittee> findByCaseId(@RequestParam String caseId);
}
