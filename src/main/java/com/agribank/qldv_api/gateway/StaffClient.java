package com.agribank.qldv_api.gateway;

import com.agribank.qldv_api.gateway.config.DatabaseFeignConfiguration;
import com.agribank.qldvutils.entity.Staff;
import com.agribank.qldvutils.response.BaseResponse;
import com.agribank.qldvutils.response.DefaultListResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "staffClient", url = "${qldv.database.url}" + "/api/v1/staff", configuration = DatabaseFeignConfiguration.class)
public interface StaffClient extends BaseClient<Staff, String> {
    @GetMapping("/find-by-staff-code")
    BaseResponse<Staff> findByStaffCode(@RequestParam(name = "staffCode") String staffCode);

    @PostMapping("/find-by-staff-codes")
    DefaultListResponse<Staff> findByStaffCodes(@RequestBody List<String> staffCodes);
}
