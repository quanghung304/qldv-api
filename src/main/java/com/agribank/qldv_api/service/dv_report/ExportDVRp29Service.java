package com.agribank.qldv_api.service.dv_report;

import com.agribank.qldv_api.request.dv_report.SearchReport29Request;
import com.agribank.qldv_api.response.dv_report.DvRp29Response;
import com.agribank.qldv_api.service.export.ExcelColumnInfoService;
import com.agribank.qldv_api.service.export.ExportService;
import com.agribank.qldv_api.service.export.ZipHelper;
import com.agribank.qldv_api.service.export.ExportPDFReportService;
import com.agribank.qldvutils.entity.ExcelColumnInfo;
import com.agribank.qldvutils.response.PageResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ExportDVRp29Service extends ExportService {
    @Autowired
    private DVReportService dvReportService;
    @Value("${app.max.rows.export}")
    private Integer MAX_ROWS_EXPORT;

    public ExportDVRp29Service(ExcelColumnInfoService excelColumnInfoService, ZipHelper zipHelper, ExportPDFReportService exportPDFReportService) {
        super(excelColumnInfoService, zipHelper, exportPDFReportService);
    }

    public int handleGetTotalRecord(Object serviceParam) {
        SearchReport29Request requestParam = (SearchReport29Request) serviceParam;
        requestParam.setPageSize(MAX_ROWS_EXPORT);
        PageResponse<DvRp29Response> dataDto = dvReportService.search29(requestParam);
        if(dataDto == null){
            return 0;
        }
        return Integer.parseInt(String.valueOf(dataDto.getTotalItems()));
    }


    public List<Map<String, Object>> handleGetDataExport(Object serviceParam, int pageIndex) {
        SearchReport29Request requestParam = (SearchReport29Request) serviceParam;
        requestParam.setPage(pageIndex);
        requestParam.setPageSize(MAX_ROWS_EXPORT);
        PageResponse<DvRp29Response> dvRp29ResponsePageResponse = dvReportService.search29(requestParam);
        if(Objects.isNull(dvRp29ResponsePageResponse)){
            return new ArrayList<>();
        }

        List<DvRp29Response> dvRp29Responses = dvRp29ResponsePageResponse.getData();
        List<Map<String, Object>> exportData = new ArrayList<>();
        for(DvRp29Response rp29Response : dvRp29Responses){
            Map<String, Object> item = new HashMap<>();
            item.put("organizationCode", rp29Response.getOrganizationCode());
            item.put("organizationName", rp29Response.getOrganizationName());
            item.put("staffCode", rp29Response.getStaffCode());
            item.put("fullName", rp29Response.getFullName());
            item.put("birthDay", rp29Response.getBirthDay());
            item.put("mainJob", rp29Response.getMainJob());
            item.put("recruitBrcd", rp29Response.getRecruitBrcd());
            item.put("partyCommitteeJob", rp29Response.getPartyCommitteeJob());
            item.put("organizationalJob", rp29Response.getOrganizationalJob());
            item.put("receivedOrganization", rp29Response.getReceivedOrganization());
            item.put("reason", rp29Response.getReason());
            item.put("expectedExpiryDate", rp29Response.getExpectedExpiryDate());
            item.put("transferDate", rp29Response.getTransferDate());
            item.put("transferStatus", rp29Response.getTransferStatus());
            item.put("implementationStaff", rp29Response.getImplementationStaff());
            item.put("controller", rp29Response.getController());
            item.put("boardLeader", rp29Response.getBoardLeader());
            exportData.add(item);
        }
        return exportData;
    }

    public Object handleGetCellValue(Object data, ExcelColumnInfo columnExport) {
        return super.handleGetCellValue(data, columnExport);
    }

}
