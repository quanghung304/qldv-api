package com.agribank.qldv_api.service.export;

import com.agribank.qldv_api.enums.EExcelColumnInfo;
import com.agribank.qldv_api.response.export.ExportResponse;
import com.agribank.qldv_api.service.dv_report.ExportDVRp24Service;
import com.agribank.qldv_api.service.dv_report.ExportDVRp25Service;
import com.agribank.qldv_api.service.tcd.TcdReportService;
import com.agribank.qldvutils.exception.CommonException;
import com.agribank.qldvutils.request.organization.OrganizationRpSearchRequest;
import com.agribank.qldvutils.request.report_dv.SearchRp24Request;
import com.agribank.qldvutils.request.report_dv.SearchRp25Request;
import com.agribank.qldvutils.request.report_tcd.SearchRp17Request;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;

import static com.agribank.qldv_api.enums.EExcelColumnInfo.*;

@Service
@RequiredArgsConstructor
public class ExportHandlerService {
    private final TcdReportService tcdReportService;
    private final ExportDVRp24Service exportDVRp24Service;
    private final ExportDVRp25Service exportDVRp25Service;

    public ExportResponse handleExport(Object param, String type) {
        ExportResponse exportResult = new ExportResponse();
        Gson gson = new Gson();
        String paramString = gson.toJson(param);
        try {
            EExcelColumnInfo eExcelColumnInfo = EExcelColumnInfo.getType(type);
            if (Objects.isNull(eExcelColumnInfo)) {
                throw new CommonException("Không tìm thấy loại báo cáo");
            }
            switch (eExcelColumnInfo) {
                case BC_03_04_DS:
                    OrganizationRpSearchRequest paramExport = gson.fromJson(paramString, OrganizationRpSearchRequest.class);
                    exportResult = tcdReportService.exportExcelRp0304(paramExport);
                    break;
                case BC_17_DSDV:
                    SearchRp17Request searchRp17Request = gson.fromJson(paramString, SearchRp17Request.class);
                    exportResult = tcdReportService.exportExcelRp17(searchRp17Request);
                    break;
                case BC_24_DSDV:
                    SearchRp24Request searchRp24Request = gson.fromJson(paramString, SearchRp24Request.class);
                    exportResult = exportDVRp24Service.exportData(searchRp24Request, BC_24_DSDV.getName(), "", BC_24_DSDV.name(), 1, BC_24_DSDV.getName());
                    break;
                case BC_25_DSDV:
                    SearchRp25Request searchRp25Request = gson.fromJson(paramString, SearchRp25Request.class);
                    exportResult = exportDVRp25Service.exportData(searchRp25Request, EExcelColumnInfo.BC_25_DSDV.getName(), "", EExcelColumnInfo.BC_25_DSDV.name(), 1, EExcelColumnInfo.BC_25_DSDV.getName());
                    break;
                default:
                    break;
            }

        }
        catch (Exception exception){
            System.out.println(exception.getMessage());
        }

        return exportResult;
    }
}
