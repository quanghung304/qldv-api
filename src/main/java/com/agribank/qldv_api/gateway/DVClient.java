package com.agribank.qldv_api.gateway;

import com.agribank.qldv_api.response.DefaultListResponse;
import com.agribank.qldv_api.response.DefaultResponse;
import com.agribank.qldvutils.dto.DVCodeNameDto;
import com.agribank.qldvutils.entity.DV;
import com.agribank.qldvutils.request.SearchDVRequest;
import com.agribank.qldvutils.request.report_tcd.SearchRp17Request;
import com.agribank.qldvutils.response.PageResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "dvClient", url = "${qldv.database.url}", configuration = DatabaseFeignConfiguration.class)
public interface DVClient {

    @GetMapping("api/v1/dv/find-by-id/{id}")
    DefaultResponse<DV> findById(
            @PathVariable("id") String id
    );

    @PostMapping("api/v1/dv/search")
    DefaultResponse<PageResponse<DV>> search(
            @RequestBody SearchDVRequest request
    );

    @PostMapping("api/v1/dv/save")
    DefaultResponse<DV> save(
            @RequestBody DV request
    );

    @PostMapping("api/v1/dv/save/all")
    DefaultResponse<List<DV>> saveAll(
            @RequestBody List<DV> dvs
    );

    @GetMapping("api/v1/dv/find-un-official-dv")
    DefaultResponse<List<DVCodeNameDto>> findUnOfficialDV(
            @RequestParam(name = "code") String code
    );

    @GetMapping("api/v1/dv/find-by-organization")
    DefaultResponse<List<DV>> findByOrganizationCode(
            @RequestParam(name = "organization") String organization
    );

    @GetMapping("api/v1/dv/find-by-staff-code")
    DefaultResponse<DV> findByStaffCode(
            @RequestParam(name = "staffCode") String staffCode
    );

    @GetMapping("api/v1/dv/party-reinstatement")
    DefaultResponse<List<DV>> getDVByPartyReinstatement();

    @PostMapping("api/v1/dv/search-report-17")
    DefaultResponse<PageResponse<DV>> searchRp17(
            @RequestBody SearchRp17Request request
    );

    @GetMapping("api/v1/dv/find-by-codes")
    DefaultListResponse<DV> findByOrganizationCodes(
            @RequestParam List<String> codes
    );
}
