package com.agribank.qldv_api.gateway;

import com.agribank.qldv_api.response.DefaultListResponse;
import com.agribank.qldv_api.response.DefaultResponse;
import com.agribank.qldvutils.dto.DVCodeNameDto;
import com.agribank.qldvutils.entity.DV;
import com.agribank.qldvutils.request.dv.SaveDVRequest;
import com.agribank.qldvutils.request.dv.SearchDVRequest;
import com.agribank.qldvutils.request.bcsl_report.tcd.SearchRequest;
import com.agribank.qldvutils.request.bcsl_report.tcd.SearchRpRequest;
import com.agribank.qldvutils.request.report_dv.SearchRp07Request;
import com.agribank.qldvutils.request.report_tcd.SearchRp17Request;
import com.agribank.qldvutils.response.DVSearchResponse;
import com.agribank.qldvutils.response.PageResponse;
import com.agribank.qldvutils.response.bcsl_report.dv.DvRp18Response;
import com.agribank.qldvutils.response.bcsl_report.tcd.BcslTcdRp09Response;
import com.agribank.qldvutils.response.report07.Report07DtoResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "dvClient", url = "${qldv.database.url}" + "/api/v1/dv", configuration = DatabaseFeignConfiguration.class)
public interface DVClient {

    @GetMapping("/find-by-id/{id}")
    DefaultResponse<DV> findById(
            @PathVariable("id") String id
    );

    @PostMapping("/search")
    DefaultResponse<PageResponse<DVSearchResponse>> search(
            @RequestBody SearchDVRequest request
    );

    @PostMapping("/save")
    DefaultResponse<DV> save(
            @RequestBody DV request
    );

    @PostMapping("/save/all")
    DefaultResponse<List<DV>> saveAll(
            @RequestBody List<DV> dvs
    );

    @GetMapping("/find-un-official-dv")
    DefaultResponse<List<DVCodeNameDto>> findUnOfficialDV(
            @RequestParam(name = "code") String code
    );

    @GetMapping("/find-by-organization")
    DefaultResponse<List<DV>> findByOrganizationCode(
            @RequestParam(name = "organization") String organization
    );

    @GetMapping("/find-by-staff-code")
    DefaultResponse<DV> findByStaffCode(
            @RequestParam(name = "staffCode") String staffCode
    );

    @GetMapping("/party-reinstatement")
    DefaultResponse<List<DV>> getDVByPartyReinstatement();

    @PostMapping("/search-report-17")
    DefaultResponse<PageResponse<DV>> searchRp17(
            @RequestBody SearchRp17Request request
    );

    @GetMapping("/find-by-codes")
    DefaultListResponse<DV> findByOrganizationCodes(
            @RequestParam List<String> codes
    );

    @PostMapping("/search-report-24")
    DefaultResponse<PageResponse<DV>> searchRp24(
            @RequestBody SearchRpRequest request
    );

    @PostMapping("/find-by-vneids")
    DefaultListResponse<DV> findByVneids(@RequestBody List<String> vneids);

    @PostMapping("/find-by-party-card-numbers")
    DefaultListResponse<DV> findByPartyCardNumbers(@RequestBody List<String> partyCardNumbers);

    @PostMapping("/find-by-staff-codes")
    DefaultListResponse<DV> findByStaffCodes(@RequestBody List<String> staffCodes);

    @PostMapping("/find-by-staff-code-actives")
    DefaultListResponse<DV> findByStaffCodeActiveIn(@RequestBody List<String> staffCodes);

    @PostMapping("/find-organization-code-actives")
    DefaultListResponse<DV> findByOrganizationCodeActiveIn(@RequestBody List<String> organizationCodes);

    @PostMapping("/search-report-21")
    DefaultResponse<PageResponse<DV>> searchRp21(
            @RequestBody SearchRpRequest request
    );

    @GetMapping("/find-active-dv-by-organization-code")
    DefaultResponse<List<DV>> findActiveDVByOrganizationCode(
            @RequestParam(name = "organization") String organization
    );

    @PostMapping("/search-report-07")
    DefaultResponse<PageResponse<Report07DtoResponse>> searchRp07(
            @RequestBody SearchRp07Request request
    );

    @PostMapping("/search-report-07/total")
    DefaultResponse<Report07DtoResponse> getTotalRp07(
            @RequestBody SearchRp07Request request
    );

    @PostMapping("/search-rp-09")
    DefaultResponse<PageResponse<BcslTcdRp09Response>> searchRp09(@RequestBody SearchRpRequest request);


    @PostMapping("/report-18")
    DefaultResponse<PageResponse<DvRp18Response>> searchRp18(
            @RequestBody SearchRequest request
    );

    @PostMapping("/save-entities")
    DefaultResponse<Boolean> saveEntities(
            @RequestBody SaveDVRequest request
    );
}
