package com.agribank.qldv_api.service.dv_report;

import com.agribank.qldv_api.service.export.ExcelColumnInfoService;
import com.agribank.qldv_api.service.export.ExportPDFReportService;
import com.agribank.qldv_api.service.export.ExportService;
import com.agribank.qldv_api.service.export.ZipHelper;
import com.agribank.qldvutils.entity.ExcelColumnInfo;
import com.agribank.qldvutils.request.bcsl_report.tcd.SearchRpRequest;
import com.agribank.qldvutils.response.PageResponse;
import com.agribank.qldvutils.response.dv_report.DvRp30Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ExportDVRp30Service extends ExportService {
    @Autowired
    private DVReportService dvReportService;
    @Value("${app.max.rows.export}")
    private Integer MAX_ROWS_EXPORT;

    public ExportDVRp30Service(ExcelColumnInfoService excelColumnInfoService, ZipHelper zipHelper, ExportPDFReportService exportPDFReportService) {
        super(excelColumnInfoService, zipHelper, exportPDFReportService);
    }


    public int handleGetTotalRecord(Object serviceParam) {
        SearchRpRequest requestParam = (SearchRpRequest) serviceParam;
        requestParam.setPageSize(MAX_ROWS_EXPORT);
        PageResponse<DvRp30Response> dataDto = dvReportService.searchRp30(requestParam);
        if(dataDto == null){
            return 0;
        }
        return Integer.parseInt(String.valueOf(dataDto.getTotalItems()));
    }


    public List<Map<String, Object>> handleGetDataExport(Object serviceParam, int pageIndex) {
        SearchRpRequest requestParam = (SearchRpRequest) serviceParam;
        requestParam.setPage(pageIndex);
        requestParam.setPageSize(MAX_ROWS_EXPORT);
        PageResponse<DvRp30Response> dvRp30ResponsePageResponse = dvReportService.searchRp30(requestParam);
        if(Objects.isNull(dvRp30ResponsePageResponse)){
            return new ArrayList<>();
        }

        List<DvRp30Response> dvRp30Responses = dvRp30ResponsePageResponse.getData();
        List<Map<String, Object>> exportData = new ArrayList<>();
        for(DvRp30Response rp30Response : dvRp30Responses){
            Map<String, Object> item = new HashMap<>();
            item.put("organizationCode", rp30Response.getOrganizationCode());
            item.put("oldOrganizationName", rp30Response.getOldOrganizationName());
            item.put("staffCode", rp30Response.getStaffCode());
            item.put("fullName", rp30Response.getFullName());
            item.put("birthDay", rp30Response.getBirthDay());
            item.put("mainJob", rp30Response.getMainJob());
            item.put("recruitBrcd", rp30Response.getRecruitBrcd());
            item.put("partyCommitteeJob", rp30Response.getPartyCommitteeJob());
            item.put("newOrganizationName", rp30Response.getNewOrganizationName());
            item.put("decisionDate", rp30Response.getDecisionDate());
            item.put("transferDate", rp30Response.getTransferDate());
            item.put("transferStatus", rp30Response.getTransferStatus());
            item.put("implementationStaff", rp30Response.getImplementationStaff());
            item.put("controller", rp30Response.getController());
            item.put("boardLeader", rp30Response.getBoardLeader());
            exportData.add(item);
        }
        return exportData;
    }

    public Object handleGetCellValue(Object data, ExcelColumnInfo columnExport) {
        return super.handleGetCellValue(data, columnExport);
    }
}
