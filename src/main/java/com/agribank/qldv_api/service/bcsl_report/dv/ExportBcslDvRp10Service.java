package com.agribank.qldv_api.service.bcsl_report.dv;

import com.agribank.qldv_api.service.dv_report.DVReportService;
import com.agribank.qldv_api.service.export.ExcelColumnInfoService;
import com.agribank.qldv_api.service.export.ExportPDFReportService;
import com.agribank.qldv_api.service.export.ExportService;
import com.agribank.qldv_api.service.export.ZipHelper;
import com.agribank.qldvutils.entity.ExcelColumnInfo;
import com.agribank.qldvutils.request.bcsl_report.dv.SearchRp10Request;
import com.agribank.qldvutils.response.PageResponse;
import com.agribank.qldvutils.response.bcsl_report.dv.BcslDvRp10Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ExportBcslDvRp10Service extends ExportService {
    @Autowired
    private DVReportService dvReportService;
    @Value("${app.max.rows.export}")
    private Integer MAX_ROWS_EXPORT;

    public ExportBcslDvRp10Service(ExcelColumnInfoService excelColumnInfoService, ZipHelper zipHelper, ExportPDFReportService exportPDFReportService) {
        super(excelColumnInfoService, zipHelper, exportPDFReportService);
    }

    public int handleGetTotalRecord(Object serviceParam) {
        SearchRp10Request requestParam = (SearchRp10Request) serviceParam;
        requestParam.setPageSize(MAX_ROWS_EXPORT);
        PageResponse<BcslDvRp10Response> dataDto = dvReportService.searchRp10(requestParam);
        if(dataDto == null){
            return 0;
        }
        return Integer.parseInt(String.valueOf(dataDto.getTotalItems()));
    }


    public List<Map<String, Object>> handleGetDataExport(Object serviceParam, int pageIndex) {
        SearchRp10Request requestParam = (SearchRp10Request) serviceParam;
        requestParam.setPage(pageIndex);
        requestParam.setPageSize(MAX_ROWS_EXPORT);
        PageResponse<BcslDvRp10Response> dataDto = dvReportService.searchRp10(requestParam);
        if(Objects.isNull(dataDto)){
            return new ArrayList<>();
        }
        List<Map<String, Object>> exportData = new ArrayList<>();
        for(BcslDvRp10Response bcslRp10Response : dataDto.getData()){
            Map<String, Object> item = new HashMap<>();
            item.put("organizationCode", bcslRp10Response.getOrganizationCode());
            item.put("organizationName", bcslRp10Response.getOrganizationName());
            item.put("developPlan", bcslRp10Response.getDevelopPlan());
            item.put("admissionCount", bcslRp10Response.getAdmissionCount());
            item.put("transferToAgribank", bcslRp10Response.getTransferToAgribank());
            item.put("totalBefore", bcslRp10Response.getTotalBefore());
            item.put("totalIncrease", bcslRp10Response.getTotalIncrease());
            item.put("membershipRestore", bcslRp10Response.getMembershipRestore());
            item.put("totalDecrease", bcslRp10Response.getTotalDecrease());
            item.put("leaveCount", bcslRp10Response.getLeaveCount());
            item.put("removeCount", bcslRp10Response.getRemoveCount());
            item.put("disciplineCount", bcslRp10Response.getDisciplineCount());
            item.put("transferOutAgribank", bcslRp10Response.getTransferOutAgribank());
            item.put("decreasedCount", bcslRp10Response.getDecreasedCount());
            item.put("recognizeCount", bcslRp10Response.getRecognizeCount());
            item.put("waitRecognize", bcslRp10Response.getWaitRecognize());
            item.put("transferWithinAgribank", bcslRp10Response.getTransferWithinAgribank());
            item.put("transferTemporary", bcslRp10Response.getTransferTemporary());
            item.put("transferProcessing", bcslRp10Response.getTransferProcessing());
            item.put("exemptionCount", bcslRp10Response.getExemptionCount());
            item.put("totalAfter", bcslRp10Response.getTotalAfter());
            item.put("admissionPercent", (int) Math.ceil(bcslRp10Response.getAdmissionPercent()));
            exportData.add(item);
        }
        return exportData;
    }

    public Object handleGetCellValue(Object data, ExcelColumnInfo columnExport) {
        return super.handleGetCellValue(data, columnExport);
    }
}
