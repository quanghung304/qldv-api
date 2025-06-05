package com.agribank.qldv_api.service.log;

import com.agribank.qldv_api.gateway.ApiLogClient;
import com.agribank.qldv_api.request.api_log.SearchApiLogRequest;
import com.agribank.qldv_api.response.apiLog.ApiLogResponse;
import com.agribank.qldvutils.entity.ApiLog;
import com.agribank.qldvutils.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ApiLogService {
    private final ApiLogClient apiLogClient;
    private final ModelMapper modelMapper;

    public PageResponse<ApiLogResponse> search(SearchApiLogRequest request) {
        PageResponse<ApiLog> apiLogPageResponse = apiLogClient.search(request).getData();
        PageResponse<ApiLogResponse> response = new PageResponse<>();
        if (Objects.isNull(apiLogPageResponse)) {
            return response;
        }

        response.setTotalPages(apiLogPageResponse.getTotalPages());
        response.setCurrentPage(apiLogPageResponse.getCurrentPage());
        response.setTotalItems(apiLogPageResponse.getTotalItems());

        if (Objects.nonNull(apiLogPageResponse.getData())) {
            response.setData(apiLogPageResponse.getData().stream()
                    .map( apiLog -> modelMapper.map(apiLog, ApiLogResponse.class)
                    ).toList()
            );
        }

        return response;
    }

}
