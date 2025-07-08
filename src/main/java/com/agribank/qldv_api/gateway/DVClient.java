package com.agribank.qldv_api.gateway;

import com.agribank.qldv_api.response.DefaultListResponse;
import com.agribank.qldv_api.response.DefaultResponse;
import com.agribank.qldvutils.dto.DVCodeNameDto;
import com.agribank.qldvutils.entity.DV;
import com.agribank.qldvutils.request.SearchDVRequest;
import com.agribank.qldvutils.request.report_dv.SearchRp23Request;
import com.agribank.qldvutils.request.report_dv.SearchRp21Request;
import com.agribank.qldvutils.request.report_dv.SearchRp24Request;
import com.agribank.qldvutils.request.report_tcd.SearchRp17Request;
import com.agribank.qldvutils.response.DVSearchResponse;
import com.agribank.qldvutils.response.PageResponse;
import com.agribank.qldvutils.response.dv_report.DvRp23Response;
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
    DefaultResponse<PageResponse<DVSearchResponse>> search(
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

    @PostMapping("api/v1/dv/search-report-24")
    DefaultResponse<PageResponse<DV>> searchRp24(
            @RequestBody SearchRp24Request request
    );

    @PostMapping("api/v1/dv/find-by-vneids")
    DefaultListResponse<DV> findByVneids(@RequestBody List<String> vneids);

    @PostMapping("api/v1/dv/find-by-party-card-numbers")
    DefaultListResponse<DV> findByPartyCardNumbers(@RequestBody List<String> partyCardNumbers);

    @PostMapping("api/v1/dv/find-by-staff-codes")
    DefaultListResponse<DV> findByStaffCodes(@RequestBody List<String> staffCodes);

    @PostMapping("api/v1/dv/find-by-staff-code-actives")
    DefaultListResponse<DV> findByStaffCodeActiveIn(@RequestBody List<String> staffCodes);

    @PostMapping("api/v1/dv/find-organization-code-actives")
    DefaultListResponse<DV> findByOrganizationCodeActiveIn(@RequestBody List<String> organizationCodes);

    @PostMapping("api/v1/dv/search-report-21")
    DefaultResponse<PageResponse<DV>> searchRp21(
            @RequestBody SearchRp21Request request
    );
}
