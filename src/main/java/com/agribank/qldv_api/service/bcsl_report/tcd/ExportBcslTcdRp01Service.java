package com.agribank.qldv_api.service.bcsl_report.tcd;

import com.agribank.qldv_api.service.export.ExcelColumnInfoService;
import com.agribank.qldv_api.service.export.ExportService;
import com.agribank.qldv_api.service.export.ZipHelper;
import com.agribank.qldv_api.service.export.ExportPDFReportService;
import com.agribank.qldvutils.entity.ExcelColumnInfo;
import com.agribank.qldvutils.request.bcsl_report.tcd.SearchRp01Request;
import com.agribank.qldvutils.response.PageResponse;
import com.agribank.qldvutils.response.bcsl_report.tcd.BcslTcdRp01Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ExportBcslTcdRp01Service extends ExportService {
    @Autowired
    private BcslTcdReportService bcslTcdReportService;
    @Value("${app.max.rows.export}")
    private Integer MAX_ROWS_EXPORT;

    public ExportBcslTcdRp01Service(ExcelColumnInfoService excelColumnInfoService, ZipHelper zipHelper, ExportPDFReportService exportPDFReportService) {
        super(excelColumnInfoService, zipHelper, exportPDFReportService);
    }

    public int handleGetTotalRecord(Object serviceParam) {
        SearchRp01Request requestParam = (SearchRp01Request) serviceParam;
        requestParam.setPageSize(MAX_ROWS_EXPORT);
        if (Objects.isNull(requestParam.getForm())){
            requestParam.setForm("A");
        }
        PageResponse<BcslTcdRp01Response> dataDto = bcslTcdReportService.searchRp01(requestParam);
        if(dataDto == null){
            return 0;
        }
        return Integer.parseInt(String.valueOf(dataDto.getTotalItems()));
    }


    public List<Map<String, Object>> handleGetDataExport(Object serviceParam, int pageIndex) {
        SearchRp01Request requestParam = (SearchRp01Request) serviceParam;
        requestParam.setPage(pageIndex);
        requestParam.setPageSize(MAX_ROWS_EXPORT);
        if (Objects.isNull(requestParam.getForm())){
            requestParam.setForm("A");
        }
        PageResponse<BcslTcdRp01Response> dataDto = bcslTcdReportService.searchRp01(requestParam);
        if(Objects.isNull(dataDto)){
            return new ArrayList<>();
        }
        List<Map<String, Object>> exportData = new ArrayList<>();
        for(BcslTcdRp01Response bcslRp01Response : dataDto.getData()){
            Map<String, Object> item = new HashMap<>();
            item.put("code", bcslRp01Response.getCode());
            item.put("name", bcslRp01Response.getName());
            item.put("form", bcslRp01Response.getForm());
            item.put("b1", bcslRp01Response.getB1());
            item.put("b2", bcslRp01Response.getB2());
            item.put("b3", bcslRp01Response.getB3());
            item.put("c1", bcslRp01Response.getC1());
            item.put("c2", bcslRp01Response.getC1());
            item.put("d", bcslRp01Response.getD());
            exportData.add(item);
        }
        return exportData;
    }

    public Object handleGetCellValue(Object data, ExcelColumnInfo columnExport) {
        return super.handleGetCellValue(data, columnExport);
    }
}
