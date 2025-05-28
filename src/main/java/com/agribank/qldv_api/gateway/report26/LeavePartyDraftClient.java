package com.agribank.qldv_api.gateway.report26;

import com.agribank.qldv_api.gateway.BaseClient;
import com.agribank.qldv_api.gateway.IamFeignConfiguration;
import com.agribank.qldvutils.entity.report26.LeavePartyDraft;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "leavePartyDraftClient", url = "${qldv.database.url}" + "/api/v1/leave-party-draft", configuration = IamFeignConfiguration.class)
public interface LeavePartyDraftClient  extends BaseClient<LeavePartyDraft, String> {
}
