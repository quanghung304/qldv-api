package com.agribank.qldv_api.service.export;

import com.agribank.qldv_api.enums.EExcelColumnInfo;
import com.agribank.qldv_api.request.dv_report.SearchReport29Request;
import com.agribank.qldv_api.response.export.ExportResponse;
import com.agribank.qldv_api.response.pdf.PDFContentResult;
import com.agribank.qldv_api.service.dv_report.ExportDVRp24Service;
import com.agribank.qldv_api.service.dv_report.ExportDVRp25Service;
import com.agribank.qldv_api.service.dv_report.ExportDVRp29Service;
import com.agribank.qldv_api.service.dv_report.ExportDVRp34Service;
import com.agribank.qldv_api.service.bcsl_report.tcd.ExportBcslTcdRp01Service;
import com.agribank.qldv_api.service.dv_report.*;
import com.agribank.qldv_api.service.tcd.TcdReportService;
import com.agribank.qldvutils.exception.CommonException;
import com.agribank.qldvutils.request.bcsl_report.tcd.SearchRp01Request;
import com.agribank.qldvutils.request.organization.OrganizationRpSearchRequest;
import com.agribank.qldvutils.request.report_dv.SearchRp21Request;
import com.agribank.qldvutils.request.report_dv.SearchRp24Request;
import com.agribank.qldvutils.request.report_dv.SearchRp25Request;
import com.agribank.qldvutils.request.report_dv.SearchRp34Request;
import com.agribank.qldvutils.request.report_tcd.SearchRp17Request;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;

import static com.agribank.qldv_api.enums.EExcelColumnInfo.*;

@Service
@RequiredArgsConstructor
public class ExportHandlerService {
    private final TcdReportService tcdReportService;
    private final ExportBcslTcdRp01Service exportBcslTcdRp01Service;
    private final ExportDVRp21Service exportDVRp21Service;
    private final ExportDVRp24Service exportDVRp24Service;
    private final ExportDVRp25Service exportDVRp25Service;
    private final ExportDVRp29Service exportDVRp29Service;
    private final ExportDVRp34Service exportDVRp34Service;

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
                case BC_01_BCSL:
                    SearchRp01Request SearchRp01Request = gson.fromJson(paramString, SearchRp01Request.class);
                    exportResult = exportBcslTcdRp01Service.exportDataBcsl(SearchRp01Request, BC_01_BCSL.getName(), "", BC_01_BCSL.name(), 1, BC_01_BCSL.getName());
                    break;
                case BC_03_04_DS:
                    OrganizationRpSearchRequest paramExport = gson.fromJson(paramString, OrganizationRpSearchRequest.class);
                    exportResult = tcdReportService.exportExcelRp0304(paramExport);
                    break;
                case BC_17_DSDV:
                    SearchRp17Request searchRp17Request = gson.fromJson(paramString, SearchRp17Request.class);
                    exportResult = tcdReportService.exportExcelRp17(searchRp17Request);
                    break;
                case BC_21_DSDV:
                    SearchRp21Request searchRp21Request = gson.fromJson(paramString, SearchRp21Request.class);
                    exportResult = exportDVRp21Service.exportData(searchRp21Request, EExcelColumnInfo.BC_21_DSDV.getName(), "", EExcelColumnInfo.BC_21_DSDV.name(), 1, EExcelColumnInfo.BC_21_DSDV.getName());
                    break;
                case BC_24_DSDV:
                    SearchRp24Request searchRp24Request = gson.fromJson(paramString, SearchRp24Request.class);
                    exportResult = exportDVRp24Service.exportData(searchRp24Request, BC_24_DSDV.getName(), "", BC_24_DSDV.name(), 1, BC_24_DSDV.getName());
                    break;
                case BC_25_DSDV:
                    SearchRp25Request searchRp25Request = gson.fromJson(paramString, SearchRp25Request.class);
                    exportResult = exportDVRp25Service.exportData(searchRp25Request, EExcelColumnInfo.BC_25_DSDV.getName(), "", EExcelColumnInfo.BC_25_DSDV.name(), 1, EExcelColumnInfo.BC_25_DSDV.getName());
                    break;
                case BC_29_DSDV:
                    SearchReport29Request searchReport29Request = gson.fromJson(paramString, SearchReport29Request.class);
                    exportResult = exportDVRp29Service.exportData(searchReport29Request, EExcelColumnInfo.BC_29_DSDV.getName(), "", EExcelColumnInfo.BC_29_DSDV.name(), 1, EExcelColumnInfo.BC_29_DSDV.getName());
                    break;
                case BC_34_DSDV:
                    SearchRp34Request searchRp34Request = gson.fromJson(paramString, SearchRp34Request.class);
                    exportResult = exportDVRp34Service.exportData(searchRp34Request, EExcelColumnInfo.BC_34_DSDV.getName(), "", EExcelColumnInfo.BC_34_DSDV.name(), 1, EExcelColumnInfo.BC_34_DSDV.getName());
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

    public
    PDFContentResult handleExportPDF(Object param, String type) {
        PDFContentResult exportResult = new PDFContentResult();
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
                    exportResult = tcdReportService.exportPDFRp0304(paramExport);
                    break;
                case BC_17_DSDV:
                    SearchRp17Request searchRp17Request = gson.fromJson(paramString, SearchRp17Request.class);
                    JsonObject jsonObject = JsonParser.parseString(paramString).getAsJsonObject();
                    searchRp17Request.setOrganizationCode(jsonObject.get("code").getAsString());
                    exportResult = tcdReportService.exportPDFRp17(searchRp17Request);
                    break;
                case BC_24_DSDV:
                    SearchRp24Request searchRp24Request = gson.fromJson(paramString, SearchRp24Request.class);
                    exportResult = exportDVRp24Service.exportPDFData(searchRp24Request, BC_24_DSDV.getName(), "", BC_24_DSDV.getName(), 1, BC_24_DSDV.getPageType());
                    break;
                case BC_25_DSDV:
                    SearchRp25Request searchRp25Request = gson.fromJson(paramString, SearchRp25Request.class);
                    exportResult = exportDVRp25Service.exportPDFData(searchRp25Request, EExcelColumnInfo.BC_25_DSDV.getName(), "", EExcelColumnInfo.BC_25_DSDV.getName(), 1, EExcelColumnInfo.BC_25_DSDV.getPageType());
                    break;
                case BC_29_DSDV:
                    SearchReport29Request searchReport29Request = gson.fromJson(paramString, SearchReport29Request.class);
                    exportResult = exportDVRp29Service.exportPDFData(searchReport29Request, EExcelColumnInfo.BC_29_DSDV.getName(), "", EExcelColumnInfo.BC_29_DSDV.getName(), 1, EExcelColumnInfo.BC_29_DSDV.getPageType());
                    break;
                case BC_34_DSDV:
                    SearchRp34Request searchRp34Request = gson.fromJson(paramString, SearchRp34Request.class);
                    exportResult = exportDVRp34Service.exportPDFData(searchRp34Request, EExcelColumnInfo.BC_34_DSDV.getName(), "", EExcelColumnInfo.BC_34_DSDV.getName(), 1, EExcelColumnInfo.BC_34_DSDV.getPageType());
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
