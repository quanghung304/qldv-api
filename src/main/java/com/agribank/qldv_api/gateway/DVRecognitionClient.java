package com.agribank.qldv_api.gateway;

import com.agribank.qldv_api.response.DefaultResponse;
import com.agribank.qldvutils.dto.DVRecognitionDto;
import com.agribank.qldvutils.entity.DVRecognition;
import com.agribank.qldvutils.request.SearchDVRecognitionRequest;
import com.agribank.qldvutils.response.PageResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "dvRecognitionClient", url = "${qldv.database.url}" + "/api/v1/dv-recognition", configuration = DatabaseFeignConfiguration.class)
public interface DVRecognitionClient extends BaseClient<DVRecognition, String> {
    @PostMapping("/search")
    DefaultResponse<PageResponse<DVRecognitionDto>> search(
            @RequestBody SearchDVRecognitionRequest request
    );
}
