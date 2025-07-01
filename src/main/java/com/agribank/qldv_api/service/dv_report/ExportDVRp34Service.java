package com.agribank.qldv_api.service.dv_report;

import com.agribank.qldv_api.response.dv_report.DvRp34Response;
import com.agribank.qldv_api.service.export.ExcelColumnInfoService;
import com.agribank.qldv_api.service.export.ExportService;
import com.agribank.qldv_api.service.export.ZipHelper;
import com.agribank.qldvutils.entity.ExcelColumnInfo;
import com.agribank.qldvutils.request.report_dv.SearchRp34Request;
import com.agribank.qldvutils.response.PageResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

@Service("ExportDVRp34")
public class ExportDVRp34Service extends ExportService {
    @Autowired
    private DVReportService dvReportService;
    @Value("${app.max.rows.export}")
    private Integer MAX_ROWS_EXPORT;

    public ExportDVRp34Service(ExcelColumnInfoService excelColumnInfoService, ZipHelper zipHelper) {
        super(excelColumnInfoService, zipHelper);
    }

    public int handleGetTotalRecord(Object serviceParam) {
        SearchRp34Request requestParam = (SearchRp34Request) serviceParam;
        requestParam.setPageSize(MAX_ROWS_EXPORT);
        PageResponse<DvRp34Response> dataDto = dvReportService.search34(requestParam);
        if(dataDto == null){
            return 0;
        }
        return Integer.parseInt(String.valueOf(dataDto.getTotalItems()));
    }


    public List<Map<String, Object>> handleGetDataExport(Object serviceParam, int pageIndex) {
        SearchRp34Request requestParam = (SearchRp34Request) serviceParam;
        requestParam.setPage(pageIndex);
        requestParam.setPageSize(MAX_ROWS_EXPORT);
        PageResponse<DvRp34Response> dvRp34ResponsePageResponse = dvReportService.search34(requestParam);
        if(Objects.isNull(dvRp34ResponsePageResponse)){
            return new ArrayList<>();
        }

        List<DvRp34Response> dvRp34Responses = dvRp34ResponsePageResponse.getData();
        List<Map<String, Object>> exportData = new ArrayList<>();
        for(DvRp34Response rp34Response : dvRp34Responses){
            Map<String, Object> item = new HashMap<>();
            item.put("organizationCode", rp34Response.getOrganizationCode());
            item.put("organizationGroupBName", rp34Response.getOrganizationGroupBName());
            item.put("organizationGroupCName", rp34Response.getOrganizationGroupCName());
            item.put("staffCode", rp34Response.getStaffCode());
            item.put("fullName", rp34Response.getFullName());
            item.put("birthDay", rp34Response.getBirthDay());
            item.put("mainJob", rp34Response.getMainJob());
            item.put("recruitBrcd", rp34Response.getRecruitBrcd());
            item.put("partyCommitteeJob", rp34Response.getPartyCommitteeJob());
            item.put("decisionNumber", rp34Response.getDecisionNumber());
            item.put("effectiveDate", rp34Response.getEffectiveDate());
            item.put("typeName", rp34Response.getTypeName());
            item.put("implementationStaff", rp34Response.getImplementationStaff());
            item.put("controller", rp34Response.getController());
            item.put("boardLeader", rp34Response.getBoardLeader());
            String reason = "";
            if (Objects.nonNull(rp34Response.getReasonLeaveParty())){
                reason = rp34Response.getReasonLeaveParty();
            } else if (Objects.nonNull(rp34Response.getReasonRemoveNameParty())) {
                reason = rp34Response.getReasonRemoveNameParty();
            } else if (Objects.nonNull(rp34Response.getReasonPartyActivityExemption())) {
                reason = rp34Response.getReasonPartyActivityExemption();
            }
            item.put("reason", reason);
            exportData.add(item);
        }
        return exportData;
    }

    public Object handleGetCellValue(Object data, ExcelColumnInfo columnExport) {
        return super.handleGetCellValue(data, columnExport);
    }
}
