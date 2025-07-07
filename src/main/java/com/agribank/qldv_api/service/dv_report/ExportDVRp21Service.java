package com.agribank.qldv_api.service.dv_report;

import com.agribank.qldv_api.response.dv_report.DvRp21Response;
import com.agribank.qldv_api.service.export.ExcelColumnInfoService;
import com.agribank.qldv_api.service.export.ExportService;
import com.agribank.qldv_api.service.export.ZipHelper;
import com.agribank.qldv_api.service.export.ExportPDFReportService;
import com.agribank.qldvutils.entity.ExcelColumnInfo;
import com.agribank.qldvutils.request.report_dv.SearchRp21Request;
import com.agribank.qldvutils.response.PageResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ExportDVRp21Service extends ExportService {
    @Autowired
    private DVReportService dvReportService;
    @Value("${app.max.rows.export}")
    private Integer MAX_ROWS_EXPORT;

    public ExportDVRp21Service(ExcelColumnInfoService excelColumnInfoService, ZipHelper zipHelper, ExportPDFReportService exportPDFReportService) {
        super(excelColumnInfoService, zipHelper, exportPDFReportService);
    }

    public int handleGetTotalRecord(Object serviceParam) {
        SearchRp21Request requestParam = (SearchRp21Request) serviceParam;
        requestParam.setPageSize(MAX_ROWS_EXPORT);
        PageResponse<DvRp21Response> dataDto = dvReportService.searchRp21(requestParam);
        if(dataDto == null){
            return 0;
        }
        return Integer.parseInt(String.valueOf(dataDto.getTotalItems()));
    }


    public List<Map<String, Object>> handleGetDataExport(Object serviceParam, int pageIndex) {
        SearchRp21Request requestParam = (SearchRp21Request) serviceParam;
        requestParam.setPage(pageIndex);
        requestParam.setPageSize(MAX_ROWS_EXPORT);
        PageResponse<DvRp21Response> dvRp21ResponsePageResponse = dvReportService.searchRp21(requestParam);
        if(Objects.isNull(dvRp21ResponsePageResponse)){
            return new ArrayList<>();
        }

        List<DvRp21Response> dvRp21Responses = dvRp21ResponsePageResponse.getData();
        List<Map<String, Object>> exportData = new ArrayList<>();
        for(DvRp21Response dvRp21Response : dvRp21Responses){
            Map<String, Object> item = new HashMap<>();
            item.put("organizationCode", dvRp21Response.getOrganizationCode());
            item.put("organizationGroupBName", dvRp21Response.getOrganizationGroupBName());
            item.put("organizationGroupCName", dvRp21Response.getOrganizationGroupCName());
            item.put("staffCode", dvRp21Response.getStaffCode());
            item.put("fullName", dvRp21Response.getFullName());
            item.put("birthDay", dvRp21Response.getBirthDay());
            item.put("mainJob", dvRp21Response.getMainJob());
            item.put("recruitBrcd", dvRp21Response.getRecruitBrcd());
            item.put("admissionDate", dvRp21Response.getAdmissionDate());
            item.put("recognitionDeadline", dvRp21Response.getRecognitionDeadline());
            item.put("skillLevel", dvRp21Response.getSkillLevel());
            item.put("partyMemberLevel", dvRp21Response.getPartyMemberLevel());
            item.put("newPartyMemberClass", dvRp21Response.getNewPartyMemberClass());
            exportData.add(item);
        }
        return exportData;
    }

    public Object handleGetCellValue(Object data, ExcelColumnInfo columnExport) {
        return super.handleGetCellValue(data, columnExport);
    }

}
