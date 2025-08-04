package com.agribank.qldv_api.service.dv_report;

import com.agribank.qldv_api.service.export.ExcelColumnInfoService;
import com.agribank.qldv_api.service.export.ExportPDFReportService;
import com.agribank.qldv_api.service.export.ExportService;
import com.agribank.qldv_api.service.export.ZipHelper;
import com.agribank.qldvutils.entity.ExcelColumnInfo;
import com.agribank.qldvutils.request.bcsl_report.tcd.SearchRpRequest;
import com.agribank.qldvutils.response.PageResponse;
import com.agribank.qldvutils.response.dv_report.DvRp23Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ExportDVRp23Service extends ExportService {
    @Autowired
    private DVReportService dvReportService;
    @Value("${app.max.rows.export}")
    private Integer MAX_ROWS_EXPORT;

    public ExportDVRp23Service(ExcelColumnInfoService excelColumnInfoService, ZipHelper zipHelper, ExportPDFReportService exportPDFReportService) {
        super(excelColumnInfoService, zipHelper, exportPDFReportService);
    }


    public int handleGetTotalRecord(Object serviceParam) {
        SearchRpRequest requestParam = (SearchRpRequest) serviceParam;
        requestParam.setPageSize(MAX_ROWS_EXPORT);
        PageResponse<DvRp23Response> dataDto = dvReportService.searchRp23(requestParam);
        if(dataDto == null){
            return 0;
        }
        return Integer.parseInt(String.valueOf(dataDto.getTotalItems()));
    }


    public List<Map<String, Object>> handleGetDataExport(Object serviceParam, int pageIndex) {
        SearchRpRequest requestParam = (SearchRpRequest) serviceParam;
        requestParam.setPage(pageIndex);
        requestParam.setPageSize(MAX_ROWS_EXPORT);
        PageResponse<DvRp23Response> dvRp23ResponsePageResponse = dvReportService.searchRp23(requestParam);
        if(Objects.isNull(dvRp23ResponsePageResponse)){
            return new ArrayList<>();
        }

        List<DvRp23Response> dvRp23Responses = dvRp23ResponsePageResponse.getData();
        List<Map<String, Object>> exportData = new ArrayList<>();
        for(DvRp23Response dvRp23Response : dvRp23Responses){
            Map<String, Object> item = new HashMap<>();
            item.put("organizationCode", dvRp23Response.getOrganizationCode());
            item.put("organizationGroupBName", dvRp23Response.getOrganizationGroupBName());
            item.put("organizationGroupCName", dvRp23Response.getOrganizationGroupCName());
            item.put("staffCode", dvRp23Response.getStaffCode());
            item.put("fullName", dvRp23Response.getFullName());
            item.put("birthDay", dvRp23Response.getBirthDay());
            item.put("mainJob", dvRp23Response.getMainJob());
            item.put("recruitBrcd", dvRp23Response.getRecruitBrcd());
            item.put("admissionDate", dvRp23Response.getAdmissionDate());
            item.put("admissionDate2", dvRp23Response.getAdmissionDate2());
            item.put("reason", dvRp23Response.getReason());
            item.put("resolutionNumber", dvRp23Response.getResolutionNumber());
            item.put("resolutionDate", dvRp23Response.getResolutionDate());
            item.put("decisionNumber", dvRp23Response.getDecisionNumber());
            item.put("decisionDate", dvRp23Response.getDecisionDate());
            item.put("implementationStaff", dvRp23Response.getImplementationStaff());
            item.put("controller", dvRp23Response.getController());
            item.put("boardLeader", dvRp23Response.getBoardLeader());
            exportData.add(item);
        }
        return exportData;
    }

    public Object handleGetCellValue(Object data, ExcelColumnInfo columnExport) {
        return super.handleGetCellValue(data, columnExport);
    }
}
