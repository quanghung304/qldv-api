package com.agribank.qldv_api.gateway;

import com.agribank.qldv_api.gateway.config.DatabaseFeignConfiguration;
import com.agribank.qldvutils.request.doctemplate.ResolveFieldsRequest;
import com.agribank.qldvutils.response.DefaultResponse;
import com.agribank.qldvutils.response.doctemplate.FieldResolutionResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * S2-03 — tra giá trị placeholder qua Resolver Registry (qldv-db, xem
 * {@code com.agribank.qldvdb.docgen.FieldMappingService}). Đặt ở qldv-db vì resolver cần
 * Spring-managed JPA repository trực tiếp, qldv-api chỉ gọi qua Feign như mọi truy vấn khác.
 */
@FeignClient(name = "fieldMappingClient", url = "${qldv.database.url}" + "/api/v1/field-mapping", configuration = DatabaseFeignConfiguration.class)
public interface FieldMappingClient {
    @PostMapping("/resolve-fields")
    DefaultResponse<FieldResolutionResult> resolveFields(@RequestBody ResolveFieldsRequest request);
}
