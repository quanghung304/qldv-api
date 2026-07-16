package com.agribank.qldv_api.gateway;

import com.agribank.qldv_api.gateway.config.DatabaseFeignConfiguration;
import com.agribank.qldvutils.entity.Attachment;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "attachmentClient", url = "${qldv.database.url}" + "/api/v1/attachment", configuration = DatabaseFeignConfiguration.class)
public interface AttachmentClient extends BaseClient<Attachment, String> {
}
