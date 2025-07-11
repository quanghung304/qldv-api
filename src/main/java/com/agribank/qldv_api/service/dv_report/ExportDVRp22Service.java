package com.agribank.qldv_api.service.dv_report;

import com.agribank.qldv_api.service.export.ExcelColumnInfoService;
import com.agribank.qldv_api.service.export.ExportPDFReportService;
import com.agribank.qldv_api.service.export.ExportService;
import com.agribank.qldv_api.service.export.ZipHelper;
import com.agribank.qldvutils.entity.ExcelColumnInfo;
import com.agribank.qldvutils.request.report_dv.SearchRp22Request;
import com.agribank.qldvutils.response.PageResponse;
import com.agribank.qldvutils.response.dv_report.DvRp22Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ExportDVRp22Service extends ExportService {
    @Autowired
    private DVReportService dvReportService;
    @Value("${app.max.rows.export}")
    private Integer MAX_ROWS_EXPORT;

    public ExportDVRp22Service(ExcelColumnInfoService excelColumnInfoService, ZipHelper zipHelper, ExportPDFReportService exportPDFReportService) {
        super(excelColumnInfoService, zipHelper, exportPDFReportService);
    }


    public int handleGetTotalRecord(Object serviceParam) {
        SearchRp22Request requestParam = (SearchRp22Request) serviceParam;
        requestParam.setPageSize(MAX_ROWS_EXPORT);
        PageResponse<DvRp22Response> dataDto = dvReportService.searchRp22(requestParam);
        if(dataDto == null){
            return 0;
        }
        return Integer.parseInt(String.valueOf(dataDto.getTotalItems()));
    }


    public List<Map<String, Object>> handleGetDataExport(Object serviceParam, int pageIndex) {
        SearchRp22Request requestParam = (SearchRp22Request) serviceParam;
        requestParam.setPage(pageIndex);
        requestParam.setPageSize(MAX_ROWS_EXPORT);
        PageResponse<DvRp22Response> dvRp22ResponsePageResponse = dvReportService.searchRp22(requestParam);
        if(Objects.isNull(dvRp22ResponsePageResponse)){
            return new ArrayList<>();
        }

        List<DvRp22Response> dvRp22Responses = dvRp22ResponsePageResponse.getData();
        List<Map<String, Object>> exportData = new ArrayList<>();
        for(DvRp22Response dvRp22Response : dvRp22Responses){
            Map<String, Object> item = new HashMap<>();
            item.put("organizationCode", dvRp22Response.getOrganizationCode());
            item.put("organizationGroupBName", dvRp22Response.getOrganizationGroupBName());
            item.put("organizationGroupCName", dvRp22Response.getOrganizationGroupCName());
            item.put("staffCode", dvRp22Response.getStaffCode());
            item.put("fullName", dvRp22Response.getFullName());
            item.put("birthDay", dvRp22Response.getBirthDay());
            item.put("mainJob", dvRp22Response.getMainJob());
            item.put("recruitBrcd", dvRp22Response.getRecruitBrcd());
            item.put("admissionDate", dvRp22Response.getAdmissionDate());
            item.put("suggested", dvRp22Response.getSuggested());
            item.put("resolutionNumber", dvRp22Response.getResolutionNumber());
            item.put("resolutionDate", dvRp22Response.getResolutionDate());
            item.put("decisionNumber", dvRp22Response.getDecisionNumber());
            item.put("decisionDate", dvRp22Response.getDecisionDate());
            item.put("implementationStaff", dvRp22Response.getImplementationStaff());
            item.put("controller", dvRp22Response.getController());
            item.put("boardLeader", dvRp22Response.getBoardLeader());
            exportData.add(item);
        }
        return exportData;
    }

    public Object handleGetCellValue(Object data, ExcelColumnInfo columnExport) {
        return super.handleGetCellValue(data, columnExport);
    }
}
