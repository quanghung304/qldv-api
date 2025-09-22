package com.agribank.qldv_api.service.bcsl_report.tcd;

import com.agribank.qldv_api.enums.EExportType;
import com.agribank.qldv_api.service.export.ExcelColumnInfoService;
import com.agribank.qldv_api.service.export.ExportPDFReportService;
import com.agribank.qldv_api.service.export.ExportService;
import com.agribank.qldv_api.service.export.ZipHelper;
import com.agribank.qldvutils.entity.ExcelColumnInfo;
import com.agribank.qldvutils.request.bcsl_report.tcd.SearchRequest;
import com.agribank.qldvutils.response.PageResponse;
import com.agribank.qldvutils.response.bcsl_report.tcd.BcslTcdRp02Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ExportBcslTcdRp02Service extends ExportService {
    @Autowired
    private BcslTcdReportService bcslTcdReportService;
    @Value("${app.max.rows.export}")
    private Integer MAX_ROWS_EXPORT;
    @Value("${app.max.rows.pdf.export}")
    private Integer MAX_ROWS_PDF_EXPORT;

    public ExportBcslTcdRp02Service(ExcelColumnInfoService excelColumnInfoService, ZipHelper zipHelper, ExportPDFReportService exportPDFReportService) {
        super(excelColumnInfoService, zipHelper, exportPDFReportService);
    }

    public int handleGetTotalRecord(Object serviceParam, String type) {
        SearchRequest requestParam = (SearchRequest) serviceParam;

        if (Objects.equals(type, EExportType.PDF.getType())) {
            requestParam.setPageSize(MAX_ROWS_PDF_EXPORT);
        } else {
            requestParam.setPageSize(MAX_ROWS_EXPORT);
        }

        PageResponse<BcslTcdRp02Response> dataDto = bcslTcdReportService.searchRp02(requestParam);
        if(dataDto == null){
            return 0;
        }
        return Integer.parseInt(String.valueOf(dataDto.getTotalItems()));
    }


    public List<Map<String, Object>> handleGetDataExport(Object serviceParam, int pageIndex, String type) {
        SearchRequest requestParam = (SearchRequest) serviceParam;
        requestParam.setPage(pageIndex);

        if (Objects.equals(type, EExportType.PDF.getType())) {
            requestParam.setPageSize(MAX_ROWS_PDF_EXPORT);
        } else {
            requestParam.setPageSize(MAX_ROWS_EXPORT);
        }

        PageResponse<BcslTcdRp02Response> dataDto = bcslTcdReportService.searchRp02(requestParam);

        if(Objects.isNull(dataDto)){
            return new ArrayList<>();
        }

        List<Map<String, Object>> exportData = new ArrayList<>();
        Map<String, Object> totalItem = new HashMap<>();

        for(BcslTcdRp02Response bcslRp01Response : dataDto.getData()){
            Map<String, Object> item = new HashMap<>();
            item.put("code", bcslRp01Response.getCode());
            item.put("name", bcslRp01Response.getName());
            item.put("form", bcslRp01Response.getForm());
            item.put("establish", bcslRp01Response.getEstablish());
            item.put("upgrade", bcslRp01Response.getUpgrade());
            item.put("downgrade", bcslRp01Response.getDowngrade());
            item.put("dissolve", bcslRp01Response.getDissolve());
            item.put("disband", bcslRp01Response.getDisband());

            if (totalItem.isEmpty()) {
                totalItem.put("code", null);
                totalItem.put("name", null);
                totalItem.put("form", null);
                totalItem.put("establish", bcslRp01Response.getEstablish());
                totalItem.put("upgrade", bcslRp01Response.getUpgrade());
                totalItem.put("downgrade", bcslRp01Response.getDowngrade());
                totalItem.put("dissolve", bcslRp01Response.getDissolve());
                totalItem.put("disband", bcslRp01Response.getDisband());
            }

            exportData.add(item);
        }

        exportData.add(totalItem);
        return exportData;
    }

    public Object handleGetCellValue(Object data, ExcelColumnInfo columnExport) {
        return super.handleGetCellValue(data, columnExport);
    }
}
