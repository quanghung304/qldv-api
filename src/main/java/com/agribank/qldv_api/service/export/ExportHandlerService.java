package com.agribank.qldv_api.service.export;

import com.agribank.qldv_api.enums.EExcelColumnInfo;
import com.agribank.qldv_api.request.dv_report.SearchReport29Request;
import com.agribank.qldv_api.response.export.ExportResponse;
import com.agribank.qldv_api.response.pdf.PDFContentResult;
import com.agribank.qldv_api.service.bcsl_report.dv.ExportBcslDvRp10Service;
import com.agribank.qldv_api.service.dv_report.ExportDVRp24Service;
import com.agribank.qldv_api.service.dv_report.ExportDVRp25Service;
import com.agribank.qldv_api.service.dv_report.ExportDVRp29Service;
import com.agribank.qldv_api.service.dv_report.ExportDVRp34Service;
import com.agribank.qldv_api.service.bcsl_report.tcd.ExportBcslTcdRp01Service;
import com.agribank.qldv_api.service.bcsl_report.tcd.ExportBcslTcdRp09Service;
import com.agribank.qldv_api.service.bcsl_report.dv.ExportBcslDvRp18Service;
import com.agribank.qldv_api.service.bcsl_report.tcd.ExportBcslTcdRp02Service;
import com.agribank.qldv_api.service.dv_report.*;
import com.agribank.qldv_api.service.tcd_report.ExportTcdRp05Service;
import com.agribank.qldv_api.service.tcd_report.TcdReportService;
import com.agribank.qldvutils.exception.CommonException;
import com.agribank.qldvutils.request.bcsl_report.dv.SearchRp18Request;
import com.agribank.qldvutils.request.bcsl_report.tcd.SearchRequest;
import com.agribank.qldvutils.request.bcsl_report.dv.SearchRp10Request;
import com.agribank.qldvutils.request.bcsl_report.tcd.SearchRp01Request;
import com.agribank.qldvutils.request.organization.OrganizationRpSearchRequest;
import com.agribank.qldvutils.request.report_dv.*;
import com.agribank.qldvutils.request.report_dv.SearchRp21Request;
import com.agribank.qldvutils.request.report_dv.SearchRp23Request;
import com.agribank.qldvutils.request.report_dv.SearchRp24Request;
import com.agribank.qldvutils.request.report_dv.SearchRp25Request;
import com.agribank.qldvutils.request.report_dv.SearchRp31Request;
import com.agribank.qldvutils.request.report_dv.SearchRp33Request;
import com.agribank.qldvutils.request.report_dv.SearchRp34Request;
import com.agribank.qldvutils.request.report_tcd.SearchRp09Request;
import com.agribank.qldvutils.request.report_tcd.SearchBCDSRequest;
import com.agribank.qldvutils.request.report_tcd.SearchRp17Request;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.Objects;

import static com.agribank.qldv_api.enums.EExcelColumnInfo.*;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ExportHandlerService {
    TcdReportService tcdReportService;
    ExportBcslTcdRp01Service exportBcslTcdRp01Service;
    ExportBcslTcdRp02Service exportBcslTcdRp02Service;
    ExportBcslDvRp18Service exportBcslDvRp18Service;
    ExportBcslTcdRp09Service exportBcslTcdRp09Service;
    ExportBcslDvRp10Service exportBcslDvRp10Service;
    ExportDVRp21Service exportDVRp21Service;
    ExportDVRp22Service exportDVRp22Service;
    ExportDVRp23Service exportDVRp23Service;
    ExportDVRp24Service exportDVRp24Service;
    ExportDVRp25Service exportDVRp25Service;
    ExportDVRp28Service exportDVRp28Service;
    ExportDVRp29Service exportDVRp29Service;
    ExportDVRp30Service exportDVRp30Service;
    ExportDVRp31Service exportDVRp31Service;
    ExportDVRp32Service exportDVRp32Service;
    ExportDVRp33Service exportDVRp33Service;
    ExportDVRp34Service exportDVRp34Service;
    ExportDVRp07Service exportDVRp07Service;
    ExportTcdRp05Service exportTcdRp05Service;

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
                    SearchRp01Request searchRp01Request = gson.fromJson(paramString, SearchRp01Request.class);
                    exportResult = exportBcslTcdRp01Service.exportDataBcsl(searchRp01Request, BC_01_BCSL.getName(), "", BC_01_BCSL.name(), 1, BC_01_BCSL.getName());
                    break;
                case BC_02_BCSL:
                    SearchRequest searchRequest = gson.fromJson(paramString, SearchRequest.class);
                    exportResult = exportBcslTcdRp02Service.exportDataBcsl(searchRequest, BC_02_BCSL.getName(), "", BC_02_BCSL.name(), 1, BC_02_BCSL.getName());
                    break;
                case BC_09_BCSL:
                    SearchRp09Request searchRp09Request = gson.fromJson(paramString, SearchRp09Request.class);
                    exportResult = exportBcslTcdRp09Service.exportDataBcsl(searchRp09Request, BC_09_BCSL.getName(), "", BC_09_BCSL.name(), 1, BC_09_BCSL.getName());
                    break;
                case BC_10_BCSL:
                    SearchRp10Request searchRp10Request = gson.fromJson(paramString, SearchRp10Request.class);
                    exportResult = exportBcslDvRp10Service.exportData(searchRp10Request, BC_10_BCSL.getName(), "", BC_10_BCSL.name(), 1, BC_10_BCSL.getName());
                    break;
                case BC_18_BCSL:
                    SearchRp18Request searchRp18Request = gson.fromJson(paramString, SearchRp18Request.class);
                    if (Objects.isNull(searchRp18Request.getDate())) {
                        throw new CommonException("Vui lòng chọn ngày");
                    }
                    exportResult = exportBcslDvRp18Service.exportDataBcsl(searchRp18Request, BC_18_BCSL.getName(), "", EExcelColumnInfo.BC_18_BCSL.name(), 1, EExcelColumnInfo.BC_18_BCSL.getName());
                    break;
                case BC_03_04_DS:
                    OrganizationRpSearchRequest paramExport = gson.fromJson(paramString, OrganizationRpSearchRequest.class);
                    exportResult = tcdReportService.exportExcelRp0304(paramExport);
                    break;
                case BC_05_DSTCD:
                    SearchBCDSRequest searchRp15Request = gson.fromJson(paramString, SearchBCDSRequest.class);
                    exportResult = exportTcdRp05Service.exportData(searchRp15Request, BC_05_DSTCD.getName(), "", BC_05_DSTCD.name(), 1, BC_05_DSTCD.getName());
                    break;
                case BC_07_DSDV:
                    SearchRp07Request searchRp07Request = gson.fromJson(paramString, SearchRp07Request.class);
                    exportResult = exportDVRp07Service.exportData(searchRp07Request, BC_07_DSDV.getName(), "", BC_07_DSDV.name(), 1, BC_07_DSDV.getName());
                    break;
                case BC_17_DSDV:
                    SearchRp17Request searchRp17Request = gson.fromJson(paramString, SearchRp17Request.class);
                    exportResult = tcdReportService.exportExcelRp17(searchRp17Request);
                    break;
                case BC_21_DSDV:
                    SearchRp21Request searchRp21Request = gson.fromJson(paramString, SearchRp21Request.class);
                    exportResult = exportDVRp21Service.exportData(searchRp21Request, EExcelColumnInfo.BC_21_DSDV.getName(), "", EExcelColumnInfo.BC_21_DSDV.name(), 1, EExcelColumnInfo.BC_21_DSDV.getName());
                    break;
                case BC_22_DSDV:
                    SearchRp22Request searchRp22Request = gson.fromJson(paramString, SearchRp22Request.class);
                    exportResult = exportDVRp22Service.exportData(searchRp22Request, EExcelColumnInfo.BC_22_DSDV.getName(), "", EExcelColumnInfo.BC_22_DSDV.name(), 1, EExcelColumnInfo.BC_22_DSDV.getName());
                    break;
                case BC_23_DSDV:
                    SearchRp23Request searchRp23Request = gson.fromJson(paramString, SearchRp23Request.class);
                    exportResult = exportDVRp23Service.exportData(searchRp23Request, EExcelColumnInfo.BC_23_DSDV.getName(), "", EExcelColumnInfo.BC_23_DSDV.name(), 1, EExcelColumnInfo.BC_23_DSDV.getName());
                    break;
                case BC_24_DSDV:
                    SearchRp24Request searchRp24Request = gson.fromJson(paramString, SearchRp24Request.class);
                    exportResult = exportDVRp24Service.exportData(searchRp24Request, BC_24_DSDV.getName(), "", BC_24_DSDV.name(), 1, BC_24_DSDV.getName());
                    break;
                case BC_25_DSDV:
                    SearchRp25Request searchRp25Request = gson.fromJson(paramString, SearchRp25Request.class);
                    exportResult = exportDVRp25Service.exportData(searchRp25Request, BC_25_DSDV.getName(), "", BC_25_DSDV.name(), 1, BC_25_DSDV.getName());
                    break;
                case BC_28_DSDV:
                    SearchRp28Request searchRp28Request = gson.fromJson(paramString, SearchRp28Request.class);
                    exportResult = exportDVRp28Service.exportData(searchRp28Request, EExcelColumnInfo.BC_28_DSDV.getName(), "", EExcelColumnInfo.BC_28_DSDV.name(), 1, EExcelColumnInfo.BC_28_DSDV.getName());
                    break;
                case BC_29_DSDV:
                    SearchReport29Request searchReport29Request = gson.fromJson(paramString, SearchReport29Request.class);
                    exportResult = exportDVRp29Service.exportData(searchReport29Request, BC_29_DSDV.getName(), "", BC_29_DSDV.name(), 1, BC_29_DSDV.getName());
                    break;
                case BC_30_DSDV:
                    SearchRp30Request searchRp30Request = gson.fromJson(paramString, SearchRp30Request.class);
                    exportResult = exportDVRp30Service.exportData(searchRp30Request, EExcelColumnInfo.BC_30_DSDV.getName(), "", EExcelColumnInfo.BC_30_DSDV.name(), 1, EExcelColumnInfo.BC_30_DSDV.getName());
                    break;
                case BC_31_DSDV:
                    SearchRp31Request searchRp31Request = gson.fromJson(paramString, SearchRp31Request.class);
                    exportResult = exportDVRp31Service.exportData(searchRp31Request, EExcelColumnInfo.BC_31_DSDV.getName(), "", EExcelColumnInfo.BC_31_DSDV.name(), 1, EExcelColumnInfo.BC_31_DSDV.getName());
                    break;
                case BC_32_DSDV:
                    SearchRp32Request searchRp32Request = gson.fromJson(paramString, SearchRp32Request.class);
                    exportResult = exportDVRp32Service.exportData(searchRp32Request, EExcelColumnInfo.BC_32_DSDV.getName(), "", EExcelColumnInfo.BC_32_DSDV.name(), 1, EExcelColumnInfo.BC_32_DSDV.getName());
                    break;
                case BC_33_DSDV:
                    SearchRp33Request searchRp33Request = gson.fromJson(paramString, SearchRp33Request.class);
                    exportResult = exportDVRp33Service.exportData(searchRp33Request, BC_33_DSDV.getName(), "", BC_33_DSDV.name(), 1, BC_33_DSDV.getName());
                    break;
                case BC_34_DSDV:
                    SearchRp34Request searchRp34Request = gson.fromJson(paramString, SearchRp34Request.class);
                    exportResult = exportDVRp34Service.exportData(searchRp34Request, BC_34_DSDV.getName(), "", BC_34_DSDV.name(), 1, BC_34_DSDV.getName());
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

    public PDFContentResult handleExportPDF(Object param, String type) {
        PDFContentResult exportResult = new PDFContentResult();
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
                    exportResult = exportBcslTcdRp01Service.exportPDFData(SearchRp01Request, BC_01_BCSL.getName(), "", BC_01_BCSL.getName(), 1, BC_01_BCSL.getPageType());
                    break;
                case BC_02_BCSL:
                    SearchRequest SearchRp02Request = gson.fromJson(paramString, SearchRequest.class);
                    exportResult = exportBcslTcdRp02Service.exportPDFData(SearchRp02Request, BC_02_BCSL.getName(), "", BC_02_BCSL.getName(), 1, BC_02_BCSL.getPageType());
                    break;
                case BC_09_BCSL:
                    SearchRp09Request searchRp09Request = gson.fromJson(paramString, SearchRp09Request.class);
                    exportResult = exportBcslTcdRp09Service.exportPDFData(searchRp09Request, BC_09_BCSL.getName(), "", BC_09_BCSL.getName(), 1, BC_09_BCSL.getPageType());
                    break;
                case BC_10_BCSL:
                    SearchRp10Request searchRp10Request = gson.fromJson(paramString, SearchRp10Request.class);
                    exportResult = exportBcslDvRp10Service.exportPDFData(searchRp10Request, BC_10_BCSL.getName(), "", BC_10_BCSL.getName(), 1, BC_10_BCSL.getPageType());
                    break;
                case BC_18_BCSL:
                    SearchRp18Request searchRp18Request = gson.fromJson(paramString, SearchRp18Request.class);
                    if (Objects.isNull(searchRp18Request.getDate())) {
                        throw new CommonException("Vui lòng chọn ngày");
                    }
                    exportResult = exportBcslDvRp18Service.exportPDFData(searchRp18Request, EExcelColumnInfo.BC_18_BCSL.getName(), "", EExcelColumnInfo.BC_18_BCSL.getName(), 1, EExcelColumnInfo.BC_18_BCSL.getPageType());
                    break;
                case BC_03_04_DS:
                    OrganizationRpSearchRequest paramExport = gson.fromJson(paramString, OrganizationRpSearchRequest.class);
                    exportResult = tcdReportService.exportPDFRp0304(paramExport);
                    break;
                case BC_05_DSTCD:
                    SearchBCDSRequest searchRp15Request = gson.fromJson(paramString, SearchBCDSRequest.class);
                    exportResult = exportTcdRp05Service.exportPDFData(searchRp15Request, BC_05_DSTCD.getName(), "", BC_05_DSTCD.getName(), 1, BC_05_DSTCD.getPageType());
                    break;
                case BC_07_DSDV:
                    SearchRp07Request SearchRp07Request = gson.fromJson(paramString, SearchRp07Request.class);
                    exportResult = exportDVRp07Service.exportPDFData(SearchRp07Request, BC_07_DSDV.getName(), "", BC_07_DSDV.getName(), 1, BC_07_DSDV.getPageType());
                    break;
                case BC_17_DSDV:
                    SearchRp17Request searchRp17Request = gson.fromJson(paramString, SearchRp17Request.class);
                    JsonObject jsonObject = JsonParser.parseString(paramString).getAsJsonObject();
                    searchRp17Request.setOrganizationCode(jsonObject.get("organizationCode").getAsString());
                    exportResult = tcdReportService.exportPDFRp17(searchRp17Request);
                    break;
                case BC_21_DSDV:
                    SearchRp21Request searchRp21Request = gson.fromJson(paramString, SearchRp21Request.class);
                    exportResult = exportDVRp21Service.exportPDFData(searchRp21Request, EExcelColumnInfo.BC_21_DSDV.getName(), "", EExcelColumnInfo.BC_21_DSDV.getName(), 1, EExcelColumnInfo.BC_21_DSDV.getPageType());
                    break;
                case BC_22_DSDV:
                    SearchRp22Request searchRp22Request = gson.fromJson(paramString, SearchRp22Request.class);
                    exportResult = exportDVRp22Service.exportPDFData(searchRp22Request, EExcelColumnInfo.BC_22_DSDV.getName(), "", EExcelColumnInfo.BC_22_DSDV.getName(), 1, EExcelColumnInfo.BC_22_DSDV.getPageType());
                    break;
                case BC_23_DSDV:
                    SearchRp23Request searchRp23Request = gson.fromJson(paramString, SearchRp23Request.class);
                    exportResult = exportDVRp23Service.exportPDFData(searchRp23Request, EExcelColumnInfo.BC_23_DSDV.getName(), "", EExcelColumnInfo.BC_23_DSDV.getName(), 1, EExcelColumnInfo.BC_23_DSDV.getPageType());
                    break;
                case BC_24_DSDV:
                    SearchRp24Request searchRp24Request = gson.fromJson(paramString, SearchRp24Request.class);
                    exportResult = exportDVRp24Service.exportPDFData(searchRp24Request, BC_24_DSDV.getName(), "", BC_24_DSDV.getName(), 1, BC_24_DSDV.getPageType());
                    break;
                case BC_25_DSDV:
                    SearchRp25Request searchRp25Request = gson.fromJson(paramString, SearchRp25Request.class);
                    exportResult = exportDVRp25Service.exportPDFData(searchRp25Request, EExcelColumnInfo.BC_25_DSDV.getName(), "", EExcelColumnInfo.BC_25_DSDV.getName(), 1, EExcelColumnInfo.BC_25_DSDV.getPageType());
                    break;
                case BC_28_DSDV:
                    SearchRp28Request searchRp28Request = gson.fromJson(paramString, SearchRp28Request.class);
                    exportResult = exportDVRp28Service.exportPDFData(searchRp28Request, EExcelColumnInfo.BC_28_DSDV.getName(), "", EExcelColumnInfo.BC_28_DSDV.getName(), 1, EExcelColumnInfo.BC_28_DSDV.getPageType());
                    break;
                case BC_29_DSDV:
                    SearchReport29Request searchReport29Request = gson.fromJson(paramString, SearchReport29Request.class);
                    exportResult = exportDVRp29Service.exportPDFData(searchReport29Request, EExcelColumnInfo.BC_29_DSDV.getName(), "", EExcelColumnInfo.BC_29_DSDV.getName(), 1, EExcelColumnInfo.BC_29_DSDV.getPageType());
                    break;
                case BC_30_DSDV:
                    SearchRp30Request searchRp30Request = gson.fromJson(paramString, SearchRp30Request.class);
                    exportResult = exportDVRp30Service.exportPDFData(searchRp30Request, EExcelColumnInfo.BC_30_DSDV.getName(), "", EExcelColumnInfo.BC_30_DSDV.getName(), 1, EExcelColumnInfo.BC_30_DSDV.getPageType());
                    break;
                case BC_31_DSDV:
                    SearchRp31Request searchRp31Request = gson.fromJson(paramString, SearchRp31Request.class);
                    exportResult = exportDVRp31Service.exportPDFData(searchRp31Request, EExcelColumnInfo.BC_31_DSDV.getName(), "", EExcelColumnInfo.BC_31_DSDV.getName(), 1, EExcelColumnInfo.BC_31_DSDV.getPageType());
                    break;
                case BC_32_DSDV:
                    SearchRp32Request searchRp32Request = gson.fromJson(paramString, SearchRp32Request.class);
                    exportResult = exportDVRp32Service.exportPDFData(searchRp32Request, EExcelColumnInfo.BC_32_DSDV.getName(), "", EExcelColumnInfo.BC_32_DSDV.getName(), 1, EExcelColumnInfo.BC_32_DSDV.getPageType());
                    break;
                case BC_33_DSDV:
                    SearchRp33Request searchRp33Request = gson.fromJson(paramString, SearchRp33Request.class);
                    exportResult = exportDVRp33Service.exportPDFData(searchRp33Request, EExcelColumnInfo.BC_33_DSDV.getName(), "", EExcelColumnInfo.BC_33_DSDV.getName(), 1, EExcelColumnInfo.BC_33_DSDV.getPageType());
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
            throw exception;
        }

        return exportResult;
    }
}
