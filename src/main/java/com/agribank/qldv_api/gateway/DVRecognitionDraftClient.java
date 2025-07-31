package com.agribank.qldv_api.gateway;

import com.agribank.qldvutils.entity.DVRecognitionDraft;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "dvRecognitionDraftClient",
        url = "${qldv.database.url}" + "/api/v1/dv-recognition-draft",
        configuration = IamFeignConfiguration.class)
public interface DVRecognitionDraftClient extends BaseClient<DVRecognitionDraft, String> {
}
