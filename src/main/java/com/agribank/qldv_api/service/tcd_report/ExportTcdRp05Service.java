package com.agribank.qldv_api.service.tcd_report;

import com.agribank.qldv_api.enums.EExportType;
import com.agribank.qldv_api.service.export.ExcelColumnInfoService;
import com.agribank.qldv_api.service.export.ExportPDFReportService;
import com.agribank.qldv_api.service.export.ExportService;
import com.agribank.qldv_api.service.export.ZipHelper;
import com.agribank.qldvutils.dto.Report05BcdsDto;
import com.agribank.qldvutils.entity.ExcelColumnInfo;
import com.agribank.qldvutils.request.bcsl_report.tcd.SearchRpRequest;
import com.agribank.qldvutils.response.PageResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ExportTcdRp05Service extends ExportService {
    @Autowired
    private TcdReportService tcdReportService;

    @Value("${app.max.rows.export}")
    private Integer MAX_ROWS_EXPORT;
    @Value("${app.max.rows.pdf.export}")
    private Integer MAX_ROWS_PDF_EXPORT;

    public ExportTcdRp05Service(ExcelColumnInfoService excelColumnInfoService, ZipHelper zipHelper, ExportPDFReportService exportPDFReportService) {
        super(excelColumnInfoService, zipHelper, exportPDFReportService);
    }

    public int handleGetTotalRecord(Object serviceParam, String type) {
        SearchRpRequest requestParam = (SearchRpRequest) serviceParam;

        if (Objects.equals(type, EExportType.PDF.getType())) {
            requestParam.setPageSize(MAX_ROWS_PDF_EXPORT);
        } else {
            requestParam.setPageSize(MAX_ROWS_EXPORT);
        }

        PageResponse<Report05BcdsDto> dataDto = tcdReportService.searchRp05(requestParam);
        if(dataDto == null){
            return 0;
        }
        return Integer.parseInt(String.valueOf(dataDto.getTotalItems()));
    }


    public List<Map<String, Object>> handleGetDataExport(Object serviceParam, int pageIndex, String type) {
        SearchRpRequest requestParam = (SearchRpRequest) serviceParam;
        requestParam.setPage(pageIndex);

        if (Objects.equals(type, EExportType.PDF.getType())) {
            requestParam.setPageSize(MAX_ROWS_PDF_EXPORT);
        } else {
            requestParam.setPageSize(MAX_ROWS_EXPORT);
        }

        PageResponse<Report05BcdsDto> dataDto = tcdReportService.searchRp05(requestParam);
        if(Objects.isNull(dataDto)){
            return new ArrayList<>();
        }

        List<Map<String, Object>> exportData = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setDateFormat(new StdDateFormat().withColonInTimeZone(true));
        List<Report05BcdsDto> rp05Responses = dataDto.getData();

        for(Report05BcdsDto rp05Response : rp05Responses){
            Map<String, Object> item = objectMapper.convertValue(rp05Response, new TypeReference<Map<String, Object>>() {});
            exportData.add(item);
        }
        return exportData;
    }

    public Object handleGetCellValue(Object data, ExcelColumnInfo columnExport) {
        return super.handleGetCellValue(data, columnExport);
    }
}
