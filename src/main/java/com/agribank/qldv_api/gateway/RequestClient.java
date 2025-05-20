package com.agribank.qldv_api.gateway;

import com.agribank.qldvutils.entity.Request;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient
public interface RequestClient extends BaseClient<Request, String> {
}
