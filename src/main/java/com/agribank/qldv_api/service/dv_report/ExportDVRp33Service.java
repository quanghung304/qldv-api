package com.agribank.qldv_api.service.dv_report;

import com.agribank.qldv_api.enums.EExportType;
import com.agribank.qldv_api.service.export.ExcelColumnInfoService;
import com.agribank.qldv_api.service.export.ExportPDFReportService;
import com.agribank.qldv_api.service.export.ExportService;
import com.agribank.qldv_api.service.export.ZipHelper;
import com.agribank.qldvutils.dto.SearchRp33Dto;
import com.agribank.qldvutils.entity.ExcelColumnInfo;
import com.agribank.qldvutils.request.bcsl_report.tcd.SearchRpRequest;
import com.agribank.qldvutils.response.PageResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

@Service("ExportDVRp33")
public class ExportDVRp33Service extends ExportService {
    @Autowired
    private DVReportService dvReportService;
    @Value("${app.max.rows.export}")
    private Integer MAX_ROWS_EXPORT;
    @Value("${app.max.rows.pdf.export}")
    private Integer MAX_ROWS_PDF_EXPORT;

    public ExportDVRp33Service(ExcelColumnInfoService excelColumnInfoService, ZipHelper zipHelper, ExportPDFReportService exportPDFReportService) {
        super(excelColumnInfoService, zipHelper, exportPDFReportService);
    }

    public int handleGetTotalRecord(Object serviceParam, String type) {
        SearchRpRequest requestParam = (SearchRpRequest) serviceParam;

        if (Objects.equals(type, EExportType.PDF.getType())) {
            requestParam.setPageSize(MAX_ROWS_PDF_EXPORT);
        } else {
            requestParam.setPageSize(MAX_ROWS_EXPORT);
        }

        PageResponse<SearchRp33Dto> dataDto = dvReportService.search33(requestParam);
        if(dataDto == null){
            return 0;
        }
        return Integer.parseInt(String.valueOf(dataDto.getTotalItems()));
    }


    public List<Map<String, Object>> handleGetDataExport(Object serviceParam, int pageIndex, String type) {
        SearchRpRequest requestParam = (SearchRpRequest) serviceParam;
        requestParam.setPage(pageIndex);

        if (Objects.equals(type, EExportType.PDF.getType())) {
            requestParam.setPageSize(MAX_ROWS_PDF_EXPORT);
        } else {
            requestParam.setPageSize(MAX_ROWS_EXPORT);
        }

        PageResponse<SearchRp33Dto> dvRp33ResponsePageResponse = dvReportService.search33(requestParam);

        if(Objects.isNull(dvRp33ResponsePageResponse)){
            return new ArrayList<>();
        }

        List<SearchRp33Dto> dvRp33Responses = dvRp33ResponsePageResponse.getData();
        List<Map<String, Object>> exportData = new ArrayList<>();

        for(SearchRp33Dto rp33Response : dvRp33Responses){
            Map<String, Object> item = new HashMap<>();
            item.put("organizationCode", rp33Response.getOrganizationCode());
            item.put("organizationName", rp33Response.getOrganizationName());
            item.put("staffCode", rp33Response.getStaffCode());
            item.put("fullName", rp33Response.getFullName());
            item.put("birthday", rp33Response.getBirthday());
            item.put("mainJob", rp33Response.getMainJob());
            item.put("recruitBrcd", rp33Response.getRecruitBrcd());
            item.put("partyCommitteeJob", rp33Response.getPartyCommitteeJob());
            item.put("decisionNumber", rp33Response.getDecisionNumber());
            item.put("effectiveDate", rp33Response.getEffectiveDate());
            item.put("transferDate", rp33Response.getTransferDate());
            item.put("receivingOrgName", rp33Response.getReceivingOrgName());
            item.put("createdBy", rp33Response.getCreatedBy());
            item.put("approvedBy", rp33Response.getApprovedBy());
            item.put("boardLeader", rp33Response.getBoardLeader());
            exportData.add(item);
        }
        return exportData;
    }

    public Object handleGetCellValue(Object data, ExcelColumnInfo columnExport) {
        return super.handleGetCellValue(data, columnExport);
    }
}
