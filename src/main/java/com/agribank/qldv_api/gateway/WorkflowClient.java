package com.agribank.qldv_api.gateway;

import com.agribank.qldv_api.gateway.config.DatabaseFeignConfiguration;
import com.agribank.qldvutils.request.casemgmt.WorkflowTransitionRequest;
import com.agribank.qldvutils.response.BaseResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "workflowClient", url = "${qldv.database.url}" + "/api/v1/workflow", configuration = DatabaseFeignConfiguration.class)
public interface WorkflowClient {
    @PostMapping("/apply-transition")
    BaseResponse<String> applyTransition(@RequestBody WorkflowTransitionRequest request);
}
