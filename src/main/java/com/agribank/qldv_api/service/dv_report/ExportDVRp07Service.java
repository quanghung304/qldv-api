package com.agribank.qldv_api.service.dv_report;

import com.agribank.qldv_api.enums.EExportType;
import com.agribank.qldv_api.service.export.ExcelColumnInfoService;
import com.agribank.qldv_api.service.export.ExportPDFReportService;
import com.agribank.qldv_api.service.export.ExportService;
import com.agribank.qldv_api.service.export.ZipHelper;
import com.agribank.qldvutils.entity.ExcelColumnInfo;
import com.agribank.qldvutils.request.report_dv.SearchRp07Request;
import com.agribank.qldvutils.response.PageResponse;
import com.agribank.qldvutils.response.report07.Report07DtoResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ExportDVRp07Service extends ExportService {

    @Autowired
    private DVReportService dvReportService;
    @Value("${app.max.rows.export}")
    private Integer MAX_ROWS_EXPORT;
    @Value("${app.max.rows.pdf.export}")
    private Integer MAX_ROWS_PDF_EXPORT;

    public ExportDVRp07Service(ExcelColumnInfoService excelColumnInfoService, ZipHelper zipHelper, ExportPDFReportService exportPDFReportService) {
        super(excelColumnInfoService, zipHelper, exportPDFReportService);
    }

    public int handleGetTotalRecord(Object serviceParam, String type) {
        SearchRp07Request requestParam = (SearchRp07Request) serviceParam;

        if (Objects.equals(type, EExportType.PDF.getType())) {
            requestParam.setPageSize(MAX_ROWS_PDF_EXPORT);
        } else {
            requestParam.setPageSize(MAX_ROWS_EXPORT);
        }

        PageResponse<Report07DtoResponse> dataDto = dvReportService.search07(requestParam);
        if(dataDto == null){
            return 0;
        }
        return Integer.parseInt(String.valueOf(dataDto.getTotalItems()));
    }


    public List<Map<String, Object>> handleGetDataExport(Object serviceParam, int pageIndex, String type) {
        SearchRp07Request requestParam = (SearchRp07Request) serviceParam;
        requestParam.setPage(pageIndex);

        if (Objects.equals(type, EExportType.PDF.getType())) {
            requestParam.setPageSize(MAX_ROWS_PDF_EXPORT);
        } else {
            requestParam.setPageSize(MAX_ROWS_EXPORT);
        }

        PageResponse<Report07DtoResponse> dvRp07ResponsePageResponse = dvReportService.search07(requestParam);
        if(Objects.isNull(dvRp07ResponsePageResponse)){
            return new ArrayList<>();
        }

        List<Report07DtoResponse> dvRp07Responses = dvRp07ResponsePageResponse.getData();
        List<Map<String, Object>> exportData = new ArrayList<>();
        for(Report07DtoResponse dvRp07Response : dvRp07Responses){
            Map<String, Object> item = new HashMap<>();
            item.put("organizationName", dvRp07Response.getOrganizationName());
            item.put("form", dvRp07Response.getForm());
            item.put("total", dvRp07Response.getTotal());
            item.put("male", dvRp07Response.getMale());
            item.put("female", dvRp07Response.getFemale());
            item.put("totalEthnic", dvRp07Response.getTotalEthnic());
            item.put("chineseEthnic", dvRp07Response.getChineseEthnic());
            item.put("religion", dvRp07Response.getReligion());
            item.put("martyrFamily", dvRp07Response.getMartyrFamily());
            item.put("revolution", dvRp07Response.getRevolution());
            item.put("inArmy", dvRp07Response.getInArmy());
            item.put("disabled", dvRp07Response.getDisabled());
            item.put("formerWorker", dvRp07Response.getFormerWorker());
            item.put("politicalIssue", dvRp07Response.getPoliticalIssue());
            item.put("foreignMarriage", dvRp07Response.getForeignMarriage());
            item.put("foreignRelated", dvRp07Response.getForeignRelated());
            item.put("violateBirthPlan", dvRp07Response.getViolateBirthPlan());
            exportData.add(item);
        }
        return exportData;
    }

    public Object handleGetCellValue(Object data, ExcelColumnInfo columnExport) {
        return super.handleGetCellValue(data, columnExport);
    }
}
