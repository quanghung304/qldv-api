package com.agribank.qldv_api.gateway;

import com.agribank.qldv_api.response.DefaultResponse;
import com.agribank.qldvutils.entity.DVRecognitionDraft;
import com.agribank.qldvutils.entity.EstablishmentDissolveDraft;
import com.agribank.qldvutils.entity.report26.Deceased;
import com.agribank.qldvutils.entity.report26.DeceasedDraft;
import com.agribank.qldvutils.response.BaseResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "dvRecognitionDraftClient",
        url = "${qldv.database.url}" + "/api/v1/dv-recognition-draft",
        configuration = IamFeignConfiguration.class)
public interface DVRecognitionDraftClient extends BaseClient<DVRecognitionDraft, String> {
}
