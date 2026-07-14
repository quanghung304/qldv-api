package com.agribank.qldv_api.gateway;

import com.agribank.qldv_api.gateway.config.DatabaseFeignConfiguration;
import com.agribank.qldvutils.entity.CommitteeMember;
import com.agribank.qldvutils.entity.CommitteeMemberId;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "committeeMemberClient", url = "${qldv.database.url}", configuration = DatabaseFeignConfiguration.class)
public interface CommitteeMemberClient extends BaseClient<CommitteeMember, CommitteeMemberId> {
}
