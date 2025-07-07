package com.agribank.qldv_api.service.dv_report;

import com.agribank.qldv_api.response.dv_report.DvRp24Response;
import com.agribank.qldv_api.service.export.ExcelColumnInfoService;
import com.agribank.qldv_api.service.export.ExportService;
import com.agribank.qldv_api.service.export.ZipHelper;
import com.agribank.qldv_api.service.export.ExportPDFReportService;
import com.agribank.qldvutils.entity.ExcelColumnInfo;
import com.agribank.qldvutils.request.report_dv.SearchRp24Request;
import com.agribank.qldvutils.response.PageResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ExportDVRp24Service extends ExportService {
    @Autowired
    private DVReportService dvReportService;
    @Value("${app.max.rows.export}")
    private Integer MAX_ROWS_EXPORT;

    public ExportDVRp24Service(ExcelColumnInfoService excelColumnInfoService, ZipHelper zipHelper, ExportPDFReportService exportPDFReportService) {
        super(excelColumnInfoService, zipHelper, exportPDFReportService);
    }

    public int handleGetTotalRecord(Object serviceParam) {
        SearchRp24Request requestParam = (SearchRp24Request) serviceParam;
        requestParam.setPageSize(MAX_ROWS_EXPORT);
        PageResponse<DvRp24Response> dataDto = dvReportService.search24(requestParam);
        if(dataDto == null){
            return 0;
        }
        return Integer.parseInt(String.valueOf(dataDto.getTotalItems()));
    }


    public List<Map<String, Object>> handleGetDataExport(Object serviceParam, int pageIndex) {
        SearchRp24Request requestParam = (SearchRp24Request) serviceParam;
        requestParam.setPage(pageIndex);
        requestParam.setPageSize(MAX_ROWS_EXPORT);
        PageResponse<DvRp24Response> dvRp24ResponsePageResponse = dvReportService.search24(requestParam);
        if(Objects.isNull(dvRp24ResponsePageResponse)){
            return new ArrayList<>();
        }

        List<DvRp24Response> dvRp24Responses = dvRp24ResponsePageResponse.getData();
        List<Map<String, Object>> exportData = new ArrayList<>();
        for(DvRp24Response rp24Response : dvRp24Responses){
            Map<String, Object> item = new HashMap<>();
            item.put("organizationCode", rp24Response.getOrganizationCode());
            item.put("organizationGroupBName", rp24Response.getOrganizationGroupBName());
            item.put("organizationGroupCName", rp24Response.getOrganizationGroupCName());
            item.put("staffCode", rp24Response.getStaffCode());
            item.put("fullName", rp24Response.getFullName());
            item.put("birthDay", rp24Response.getBirthDay());
            item.put("mainJob", rp24Response.getMainJob());
            item.put("recruitBrcd", rp24Response.getRecruitBrcd());
            item.put("admissionDate", rp24Response.getAdmissionDate());
            item.put("conclusionNumber", rp24Response.getConclusionNumber());
            item.put("conclusionDate", rp24Response.getConclusionDate());
            item.put("decisionNumber", rp24Response.getDecisionNumber());
            item.put("decisionDate", rp24Response.getDecisionDate());
            item.put("officialRecognitionDay", rp24Response.getOfficialRecognitionDay());
            item.put("implementationStaff", rp24Response.getImplementationStaff());
            item.put("controller", rp24Response.getController());
            item.put("boardLeader", rp24Response.getBoardLeader());
            exportData.add(item);
        }
        return exportData;
    }

    public Object handleGetCellValue(Object data, ExcelColumnInfo columnExport) {
        return super.handleGetCellValue(data, columnExport);
    }
}
