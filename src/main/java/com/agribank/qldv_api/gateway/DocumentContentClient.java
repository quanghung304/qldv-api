package com.agribank.qldv_api.gateway;

import com.agribank.qldv_api.gateway.config.DatabaseFeignConfiguration;
import com.agribank.qldvutils.response.BaseResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;
import java.util.Optional;

/**
 * Sinh văn bản (lần 3) — tra giá trị placeholder qua DocumentContentProvider (qldv-db, xem
 * {@code com.agribank.qldvdb.docgen.DocumentContentResolutionService}). Đặt ở qldv-db vì provider
 * cần Spring-managed JPA repository trực tiếp, qldv-api chỉ gọi qua Feign như mọi truy vấn khác.
 *
 * {@link Optional#empty()} nghĩa là {@code generatorKey} chưa đăng ký bean nào — qldv-api tự lắp
 * thông báo lỗi kèm template_name (chỉ qldv-api mới biết) trước khi trả về client.
 */
@FeignClient(name = "documentContentClient", url = "${qldv.database.url}" + "/api/v1/document-content",
        configuration = DatabaseFeignConfiguration.class)
public interface DocumentContentClient {
    @GetMapping("/resolve")
    BaseResponse<Optional<Map<String, String>>> resolve(@RequestParam String generatorKey, @RequestParam String caseId);
}
