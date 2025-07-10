package com.agribank.qldv_api.gateway.form02.updown;

import com.agribank.qldv_api.gateway.BaseClient;
import com.agribank.qldv_api.gateway.DatabaseFeignConfiguration;
import com.agribank.qldv_api.response.DefaultListResponse;
import com.agribank.qldvutils.entity.form02.updown.OrganizationUpDown;
import com.agribank.qldvutils.request.form02.AprroveUpDownRequest;
import com.agribank.qldvutils.request.form02.OrganizationUpDownRpRequest;
import com.agribank.qldvutils.request.form02.UpdownOrganizationFilterRequest;
import com.agribank.qldvutils.response.BaseResponse;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "OrganizationUpDownClient",
        url = "${qldv.database.url}" + "/api/v1/updown",
        configuration = DatabaseFeignConfiguration.class
)
public interface OrganizationUpDownClient extends BaseClient<OrganizationUpDown, String> {

    @PostMapping("/organization-code-and-date")
    DefaultListResponse<OrganizationUpDown> findByOrganizationCodeAndDate(
            @RequestBody OrganizationUpDownRpRequest request
    );

    @PostMapping("list")
    BaseResponse<Page<OrganizationUpDown>> getList(
            @RequestBody UpdownOrganizationFilterRequest request
    );

    @PutMapping("/save-entities")
    BaseResponse<Boolean> saveEntities(@RequestBody @Valid AprroveUpDownRequest request);
}
