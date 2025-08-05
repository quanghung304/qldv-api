package com.agribank.qldv_api.service.dv_report;

import com.agribank.qldv_api.service.export.ExcelColumnInfoService;
import com.agribank.qldv_api.service.export.ExportPDFReportService;
import com.agribank.qldv_api.service.export.ExportService;
import com.agribank.qldv_api.service.export.ZipHelper;
import com.agribank.qldvutils.dto.Report31Dto;
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

@Service("ExportDVRp31")
public class ExportDVRp31Service extends ExportService {
    @Autowired
    private DVReportService dvReportService;
    @Value("${app.max.rows.export}")
    private Integer MAX_ROWS_EXPORT;

    public ExportDVRp31Service(ExcelColumnInfoService excelColumnInfoService, ZipHelper zipHelper, ExportPDFReportService exportPDFReportService) {
        super(excelColumnInfoService, zipHelper, exportPDFReportService);
    }

    public int handleGetTotalRecord(Object serviceParam) {
        SearchRpRequest requestParam = (SearchRpRequest) serviceParam;
        requestParam.setPageSize(MAX_ROWS_EXPORT);
        PageResponse<Report31Dto> dataDto = dvReportService.search31(requestParam);
        if(dataDto == null){
            return 0;
        }
        return Integer.parseInt(String.valueOf(dataDto.getTotalItems()));
    }


    public List<Map<String, Object>> handleGetDataExport(Object serviceParam, int pageIndex) {
        SearchRpRequest requestParam = (SearchRpRequest) serviceParam;
        requestParam.setPage(pageIndex);
        requestParam.setPageSize(MAX_ROWS_EXPORT);
        PageResponse<Report31Dto> dvRp31ResponsePageResponse = dvReportService.search31(requestParam);
        if(Objects.isNull(dvRp31ResponsePageResponse)){
            return new ArrayList<>();
        }

        List<Report31Dto> dvRp31Responses = dvRp31ResponsePageResponse.getData();
        List<Map<String, Object>> exportData = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setDateFormat(new StdDateFormat().withColonInTimeZone(true));
        for(Report31Dto rp31Response : dvRp31Responses){
            Map<String, Object> item = objectMapper.convertValue(rp31Response, new TypeReference<Map<String, Object>>() {});
            exportData.add(item);
        }
        return exportData;
    }

    public Object handleGetCellValue(Object data, ExcelColumnInfo columnExport) {
        return super.handleGetCellValue(data, columnExport);
    }
}
