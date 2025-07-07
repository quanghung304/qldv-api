package com.agribank.qldv_api.service.dv_report;

import com.agribank.qldv_api.response.dv_report.DvRp25Response;
import com.agribank.qldv_api.service.export.ExcelColumnInfoService;
import com.agribank.qldv_api.service.export.ExportService;
import com.agribank.qldv_api.service.export.ZipHelper;
import com.agribank.qldv_api.service.export.ExportPDFReportService;
import com.agribank.qldvutils.entity.ExcelColumnInfo;
import com.agribank.qldvutils.request.report_dv.SearchRp25Request;
import com.agribank.qldvutils.response.PageResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

@Service("ExportDVRp25")
public class ExportDVRp25Service extends ExportService {
    @Autowired
    private DVReportService dvReportService;
    @Value("${app.max.rows.export}")
    private Integer MAX_ROWS_EXPORT;

    public ExportDVRp25Service(ExcelColumnInfoService excelColumnInfoService, ZipHelper zipHelper, ExportPDFReportService exportPDFReportService) {
        super(excelColumnInfoService, zipHelper, exportPDFReportService);
    }

    public int handleGetTotalRecord(Object serviceParam) {
        SearchRp25Request requestParam = (SearchRp25Request) serviceParam;
        requestParam.setPageSize(MAX_ROWS_EXPORT);
        PageResponse<DvRp25Response> dataDto = dvReportService.search25(requestParam);
        if(dataDto == null){
            return 0;
        }
        return Integer.parseInt(String.valueOf(dataDto.getTotalItems()));
    }


    public List<Map<String, Object>> handleGetDataExport(Object serviceParam, int pageIndex) {
        SearchRp25Request requestParam = (SearchRp25Request) serviceParam;
        requestParam.setPage(pageIndex);
        requestParam.setPageSize(MAX_ROWS_EXPORT);
        PageResponse<DvRp25Response> dvRp25ResponsePageResponse = dvReportService.search25(requestParam);
        if(Objects.isNull(dvRp25ResponsePageResponse)){
            return new ArrayList<>();
        }

        List<DvRp25Response> dvRp25Responses = dvRp25ResponsePageResponse.getData();
        List<Map<String, Object>> exportData = new ArrayList<>();
        for(DvRp25Response rp25Response : dvRp25Responses){
            Map<String, Object> item = new HashMap<>();
            item.put("organizationCode", rp25Response.getOrganizationCode());
            item.put("organizationGroupBName", rp25Response.getOrganizationGroupBName());
            item.put("organizationGroupCName", rp25Response.getOrganizationGroupCName());
            item.put("staffCode", rp25Response.getStaffCode());
            item.put("fullName", rp25Response.getFullName());
            item.put("birthDay", rp25Response.getBirthDay());
            item.put("mainJob", rp25Response.getMainJob());
            item.put("recruitBrcd", rp25Response.getRecruitBrcd());
            item.put("admissionDate", rp25Response.getAdmissionDate());
            item.put("decisionNumber", rp25Response.getDecisionNumber());
            item.put("effectiveDate", rp25Response.getEffectiveDate());
            item.put("officialRecognitionDay", rp25Response.getOfficialRecognitionDay());
            item.put("implementationStaff", rp25Response.getImplementationStaff());
            item.put("controller", rp25Response.getController());
            item.put("boardLeader", rp25Response.getBoardLeader());
            exportData.add(item);
        }
        return exportData;
    }

    public Object handleGetCellValue(Object data, ExcelColumnInfo columnExport) {
        return super.handleGetCellValue(data, columnExport);
    }
}

