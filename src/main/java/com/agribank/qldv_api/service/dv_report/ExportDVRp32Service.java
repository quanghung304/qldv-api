package com.agribank.qldv_api.service.dv_report;

import com.agribank.qldv_api.response.dv_report.DvRp34Response;
import com.agribank.qldv_api.service.export.ExcelColumnInfoService;
import com.agribank.qldv_api.service.export.ExportPDFReportService;
import com.agribank.qldv_api.service.export.ExportService;
import com.agribank.qldv_api.service.export.ZipHelper;
import com.agribank.qldvutils.entity.ExcelColumnInfo;
import com.agribank.qldvutils.request.report_dv.SearchRp32Request;
import com.agribank.qldvutils.response.PageResponse;
import com.agribank.qldvutils.response.Report32Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

@Service("ExportDVRp32")
public class ExportDVRp32Service extends ExportService {
    @Autowired
    private DVReportService dvReportService;
    @Value("${app.max.rows.export}")
    private Integer MAX_ROWS_EXPORT;

    public ExportDVRp32Service(ExcelColumnInfoService excelColumnInfoService, ZipHelper zipHelper, ExportPDFReportService exportPDFReportService) {
        super(excelColumnInfoService, zipHelper, exportPDFReportService);
    }

    public int handleGetTotalRecord(Object serviceParam) {
        SearchRp32Request requestParam = (SearchRp32Request) serviceParam;
        requestParam.setPageSize(MAX_ROWS_EXPORT);
        PageResponse<Report32Response> dataDto = dvReportService.searchRp32(requestParam);
        if(dataDto == null){
            return 0;
        }
        return Integer.parseInt(String.valueOf(dataDto.getTotalItems()));
    }


    public List<Map<String, Object>> handleGetDataExport(Object serviceParam, int pageIndex) {
        SearchRp32Request requestParam = (SearchRp32Request) serviceParam;
        requestParam.setPage(pageIndex);
        requestParam.setPageSize(MAX_ROWS_EXPORT);
        PageResponse<Report32Response> dvRp32ResponsePageResponse = dvReportService.searchRp32(requestParam);
        if(Objects.isNull(dvRp32ResponsePageResponse)){
            return new ArrayList<>();
        }

        List<Report32Response> dvRp32Responses = dvRp32ResponsePageResponse.getData();
        List<Map<String, Object>> exportData = new ArrayList<>();
        for(Report32Response rp32Response : dvRp32Responses){
            Map<String, Object> item = new HashMap<>();
            item.put("organizationCode", rp32Response.getOrganizationCode());
            item.put("name", rp32Response.getName());
            item.put("staffCode", rp32Response.getStaffCode());
            item.put("fullName", rp32Response.getFullName());
            item.put("birthDay", rp32Response.getBirthDay());
            item.put("mainJob", rp32Response.getMainJob());
            item.put("recruitBrcd", rp32Response.getRecruitBrcd());
            item.put("partyCommitteeJob", rp32Response.getPartyCommitteeJob());
            item.put("decisionNumber", rp32Response.getDecisionNumber());
            item.put("reason", rp32Response.getReason());
            item.put("transferDate", rp32Response.getTransferDate());
            item.put("expectedExpiryDate", rp32Response.getExpectedExpiryDate());
            item.put("transferType", rp32Response.getTransferType());
            exportData.add(item);
        }
        return exportData;
    }

    public Object handleGetCellValue(Object data, ExcelColumnInfo columnExport) {
        return super.handleGetCellValue(data, columnExport);
    }
}
