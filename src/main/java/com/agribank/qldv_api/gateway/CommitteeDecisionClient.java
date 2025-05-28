package com.agribank.qldv_api.gateway;

import com.agribank.qldvutils.entity.CommitteeDecision;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "committeeDecisionClient",  url = "${qldv.database.url}" + "/api/v1/committee-decision", configuration = DatabaseFeignConfiguration.class)
public interface CommitteeDecisionClient extends BaseClient<CommitteeDecision, String>{
}
