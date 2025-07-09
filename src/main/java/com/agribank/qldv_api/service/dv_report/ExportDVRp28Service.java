package com.agribank.qldv_api.service.dv_report;

import com.agribank.qldv_api.service.export.ExcelColumnInfoService;
import com.agribank.qldv_api.service.export.ExportPDFReportService;
import com.agribank.qldv_api.service.export.ExportService;
import com.agribank.qldv_api.service.export.ZipHelper;
import com.agribank.qldvutils.entity.ExcelColumnInfo;
import com.agribank.qldvutils.request.report_dv.SearchRp28Request;
import com.agribank.qldvutils.response.PageResponse;
import com.agribank.qldvutils.response.dv_report.DvRp28Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ExportDVRp28Service extends ExportService {
    @Autowired
    private DVReportService dvReportService;
    @Value("${app.max.rows.export}")
    private Integer MAX_ROWS_EXPORT;

    public ExportDVRp28Service(ExcelColumnInfoService excelColumnInfoService, ZipHelper zipHelper, ExportPDFReportService exportPDFReportService) {
        super(excelColumnInfoService, zipHelper, exportPDFReportService);
    }

    public int handleGetTotalRecord(Object serviceParam) {
        SearchRp28Request requestParam = (SearchRp28Request) serviceParam;
        requestParam.setPageSize(MAX_ROWS_EXPORT);
        PageResponse<DvRp28Response> dataDto = dvReportService.searchRp28(requestParam);
        if(dataDto == null){
            return 0;
        }
        return Integer.parseInt(String.valueOf(dataDto.getTotalItems()));
    }


    public List<Map<String, Object>> handleGetDataExport(Object serviceParam, int pageIndex) {
        SearchRp28Request requestParam = (SearchRp28Request) serviceParam;
        requestParam.setPage(pageIndex);
        requestParam.setPageSize(MAX_ROWS_EXPORT);
        PageResponse<DvRp28Response> dvRp28ResponsePageResponse = dvReportService.searchRp28(requestParam);
        if(Objects.isNull(dvRp28ResponsePageResponse)){
            return new ArrayList<>();
        }

        List<DvRp28Response> dvRp28Responses = dvRp28ResponsePageResponse.getData();
        List<Map<String, Object>> exportData = new ArrayList<>();
        for(DvRp28Response rp28Response : dvRp28Responses){
            Map<String, Object> item = new HashMap<>();
            item.put("organizationCode", rp28Response.getOrganizationCode());
            item.put("organizationName", rp28Response.getOrganizationName());
            item.put("staffCode", rp28Response.getStaffCode());
            item.put("fullName", rp28Response.getFullName());
            item.put("birthDay", rp28Response.getBirthDay());
            item.put("mainJob", rp28Response.getMainJob());
            item.put("recruitBrcd", rp28Response.getRecruitBrcd());
            item.put("partyCommitteeJob", rp28Response.getPartyCommitteeJob());
            item.put("organizationalJob", rp28Response.getOrganizationalJob());
            item.put("transferringPartyName", rp28Response.getTransferringPartyName());
            item.put("firstIntroDate", rp28Response.getFirstIntroDate());
            item.put("issueDate", rp28Response.getIssueDate());
            item.put("transferDate", rp28Response.getTransferDate());
            item.put("transferStatus", rp28Response.getTransferStatus());
            item.put("implementationStaff", rp28Response.getImplementationStaff());
            item.put("controller", rp28Response.getController());
            item.put("boardLeader", rp28Response.getBoardLeader());
            exportData.add(item);
        }
        return exportData;
    }

    public Object handleGetCellValue(Object data, ExcelColumnInfo columnExport) {
        return super.handleGetCellValue(data, columnExport);
    }
}
