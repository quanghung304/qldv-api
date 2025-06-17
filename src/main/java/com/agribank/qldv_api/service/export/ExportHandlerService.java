package com.agribank.qldv_api.service.export;

import com.agribank.qldv_api.enums.EExcelColumnInfo;
import com.agribank.qldv_api.response.export.ExportResponse;
import com.agribank.qldv_api.service.tcd.TcdReportService;
import com.agribank.qldvutils.request.organization.OrganizationRpSearchRequest;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ExportHandlerService {
    private final TcdReportService tcdReportService;

    public ExportResponse handleExport(Object param, String type) {
        ExportResponse exportResult = new ExportResponse();
        Gson gson = new Gson();
        String paramString = gson.toJson(param);
        try {
            if(Objects.equals(type, EExcelColumnInfo.BC_03_04_DS.name())){
                OrganizationRpSearchRequest paramExport = gson.fromJson(paramString, OrganizationRpSearchRequest.class);
                exportResult = tcdReportService.exportExcelRp0304(paramExport);
            }
        }
        catch (Exception exception){
            System.out.println(exception.getMessage());
        }

        return exportResult;
    }
}
