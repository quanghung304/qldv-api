package com.agribank.qldv_api.service.tcd_report;

import com.agribank.qldv_api.response.tcd.Rp17Response;
import com.agribank.qldv_api.service.DVService;
import com.agribank.qldv_api.service.export.ExcelColumnInfoService;
import com.agribank.qldv_api.service.export.ExportPDFReportService;
import com.agribank.qldv_api.service.export.ExportService;
import com.agribank.qldv_api.service.export.ZipHelper;
import com.agribank.qldvutils.entity.ExcelColumnInfo;
import com.agribank.qldvutils.request.report_tcd.SearchRp17Request;
import com.agribank.qldvutils.response.PageResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ExportTcdRp17Service extends ExportService {
    @Lazy
    @Autowired
    private DVService dvService;
    @Value("${app.max.rows.export}")
    private Integer MAX_ROWS_EXPORT;

    public ExportTcdRp17Service(ExcelColumnInfoService excelColumnInfoService, ZipHelper zipHelper, ExportPDFReportService exportPDFReportService) {
        super(excelColumnInfoService, zipHelper, exportPDFReportService);
    }

    public int handleGetTotalRecord(Object serviceParam) {
        SearchRp17Request requestParam = (SearchRp17Request) serviceParam;
        requestParam.setPageSize(MAX_ROWS_EXPORT);
        PageResponse<Rp17Response> dataDto = dvService.searchRp17(requestParam);
        if(dataDto == null){
            return 0;
        }
        return Integer.parseInt(String.valueOf(dataDto.getTotalItems()));
    }


    public List<Map<String, Object>> handleGetDataExport(Object serviceParam, int pageIndex) {
        SearchRp17Request requestParam = (SearchRp17Request) serviceParam;
        requestParam.setPage(pageIndex);
        requestParam.setPageSize(MAX_ROWS_EXPORT);
        PageResponse<Rp17Response> rp17ResponsePageResponse = dvService.searchRp17(requestParam);
        if(Objects.isNull(rp17ResponsePageResponse)){
            return new ArrayList<>();
        }

        List<Rp17Response> rp17Responses = rp17ResponsePageResponse.getData();
        List<Map<String, Object>> exportData = new ArrayList<>();
        for(Rp17Response rp17Response : rp17Responses){
            Map<String, Object> item = new HashMap<>();
            item.put("organizationCode", rp17Response.getOrganizationCode());
            item.put("fullName", rp17Response.getFullName());
            item.put("mainJob", rp17Response.getMainJob());
            item.put("ethnic", rp17Response.getEthnic());
            item.put("religion", rp17Response.getReligion());
            item.put("admissionDate", rp17Response.getAdmissionDate());
            item.put("officialRecognitionDay", rp17Response.getOfficialRecognitionDay());
            item.put("partyAge", rp17Response.getPartyAge());
            item.put("currentJob", rp17Response.getCurrentJob());
            item.put("planningJob", rp17Response.getPlanningJob());
            item.put("degree", rp17Response.getDegree());
            item.put("education", rp17Response.getEducation());
            item.put("disabledType", rp17Response.getDisabledType());
            item.put("enlistmentDate", rp17Response.getEnlistmentDate());
            item.put("dischargeDate", rp17Response.getDischargeDate());
            item.put("martyrsFamily", rp17Response.getMartyrsFamily());
            item.put("revolution", rp17Response.getRevolution());
            item.put("oldRegime", rp17Response.getOldRegime());
            item.put("formerWorker", rp17Response.getFormerWorker());
            exportData.add(item);
        }
        return exportData;
    }

    public Object handleGetCellValue(Object data, ExcelColumnInfo columnExport) {
        return super.handleGetCellValue(data, columnExport);
    }
}
