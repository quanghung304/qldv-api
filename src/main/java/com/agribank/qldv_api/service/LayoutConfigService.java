package com.agribank.qldv_api.service;

import com.agribank.qldv_api.gateway.LayoutConfigClient;
import com.agribank.qldv_api.response.DefaultResponse;
import com.agribank.qldvutils.entity.LayoutConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LayoutConfigService {
    private final LayoutConfigClient client;

    public LayoutConfig findByDescription(String description) {
        DefaultResponse<LayoutConfig> layoutConfigDefaultResponse = client.searchByDescription(description);
        return layoutConfigDefaultResponse.getData();
    }
}
