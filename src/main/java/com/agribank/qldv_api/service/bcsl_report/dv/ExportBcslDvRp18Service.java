package com.agribank.qldv_api.service.bcsl_report.dv;

import com.agribank.qldv_api.enums.EExportType;
import com.agribank.qldv_api.service.export.ExcelColumnInfoService;
import com.agribank.qldv_api.service.export.ExportPDFReportService;
import com.agribank.qldv_api.service.export.ExportService;
import com.agribank.qldv_api.service.export.ZipHelper;
import com.agribank.qldvutils.entity.ExcelColumnInfo;
import com.agribank.qldvutils.request.bcsl_report.tcd.SearchRequest;
import com.agribank.qldvutils.response.PageResponse;
import com.agribank.qldvutils.response.bcsl_report.dv.DvRp18Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ExportBcslDvRp18Service extends ExportService {
    @Autowired
    private BcslDvReportService bcslDvReportService;
    @Value("${app.max.rows.export}")
    private Integer MAX_ROWS_EXPORT;
    @Value("${app.max.rows.pdf.export}")
    private Integer MAX_ROWS_PDF_EXPORT;

    public ExportBcslDvRp18Service(ExcelColumnInfoService excelColumnInfoService, ZipHelper zipHelper, ExportPDFReportService exportPDFReportService) {
        super(excelColumnInfoService, zipHelper, exportPDFReportService);
    }

    public int handleGetTotalRecord(Object serviceParam, String type) {
        SearchRequest requestParam = (SearchRequest) serviceParam;

        if (Objects.equals(type, EExportType.PDF.getType())) {
            requestParam.setPageSize(MAX_ROWS_PDF_EXPORT);
        } else {
            requestParam.setPageSize(MAX_ROWS_EXPORT);
        }

        PageResponse<DvRp18Response> dataDto = bcslDvReportService.searchRp18(requestParam);
        if(dataDto == null){
            return 0;
        }
        List<DvRp18Response> data = dataDto.getData();
        data.add(totalAge(data));
        return data.size();
    }

    public List<Map<String, Object>> handleGetDataExport(Object serviceParam, int pageIndex, String type) {
        SearchRequest requestParam = (SearchRequest) serviceParam;

        if (Objects.equals(type, EExportType.PDF.getType())) {
            requestParam.setPageSize(MAX_ROWS_PDF_EXPORT);
        } else {
            requestParam.setPageSize(MAX_ROWS_EXPORT);
        }

        requestParam.setPageSize(MAX_ROWS_EXPORT);
        PageResponse<DvRp18Response> dataDto = bcslDvReportService.searchRp18(requestParam);
        if(Objects.isNull(dataDto)){
            return new ArrayList<>();
        }

        List<DvRp18Response> data = dataDto.getData();
        data.add(totalAge(data));

        List<Map<String, Object>> exportData = new ArrayList<>();
        for(DvRp18Response rp18Response : data){
            Map<String, Object> item = new HashMap<>();
            item.put("organizationName", rp18Response.getOrganizationName());
            item.put("male1830", rp18Response.getMale1830());
            item.put("female1830", rp18Response.getFemale1830());
            item.put("male3135", rp18Response.getMale3135());
            item.put("female3135", rp18Response.getFemale3135());
            item.put("male3640", rp18Response.getMale3640());
            item.put("female3640", rp18Response.getFemale3640());
            item.put("male4145", rp18Response.getMale4145());
            item.put("female4145", rp18Response.getFemale4145());
            item.put("male4650", rp18Response.getMale4650());
            item.put("female4650", rp18Response.getFemale4650());
            item.put("male5155", rp18Response.getMale5155());
            item.put("female5155", rp18Response.getFemale5155());
            item.put("male5660", rp18Response.getMale5660());
            item.put("female5660", rp18Response.getFemale5660());
            item.put("male6162", rp18Response.getMale6162());
            item.put("female6162", rp18Response.getFemale6162());
            exportData.add(item);
        }
        return exportData;
    }

    public Object handleGetCellValue(Object data, ExcelColumnInfo columnExport) {
        return super.handleGetCellValue(data, columnExport);
    }

    private DvRp18Response totalAge(List<DvRp18Response> dvRp18Responses){
        DvRp18Response dvTotal = new DvRp18Response();
        dvTotal.setForm("Total");
        dvTotal.setOrganizationCode(null);
        dvTotal.setOrganizationName(null);

        for (DvRp18Response inputResponse : dvRp18Responses) {
            Integer male1830 = dvTotal.getMale1830() + inputResponse.getMale1830();
            Integer female1830 = dvTotal.getFemale1830() + inputResponse.getFemale1830();
            dvTotal.setMale1830(male1830);
            dvTotal.setFemale1830(female1830);

            Integer male3135 = dvTotal.getMale3135() + inputResponse.getMale3135();
            Integer female3135 = dvTotal.getFemale3135() + inputResponse.getFemale3135();
            dvTotal.setMale3135(male3135);
            dvTotal.setFemale3135(female3135);

            Integer male3640 = dvTotal.getMale3640() + inputResponse.getMale3640();
            Integer female3640 = dvTotal.getFemale3640() + inputResponse.getFemale3640();
            dvTotal.setMale3640(male3640);
            dvTotal.setFemale3640(female3640);

            Integer male4145 = dvTotal.getMale4145() + inputResponse.getMale4145();
            Integer female4145 = dvTotal.getFemale4145() + inputResponse.getFemale4145();
            dvTotal.setMale4145(male4145);
            dvTotal.setFemale4145(female4145);

            Integer male4650 = dvTotal.getMale4650() + inputResponse.getMale4650();
            Integer female4650 = dvTotal.getFemale4650() + inputResponse.getFemale4650();
            dvTotal.setMale4650(male4650);
            dvTotal.setFemale4650(female4650);

            Integer male5155 = dvTotal.getMale5155() + inputResponse.getMale5155();
            Integer female5155 = dvTotal.getFemale5155() + inputResponse.getFemale5155();
            dvTotal.setMale5155(male5155);
            dvTotal.setFemale5155(female5155);

            Integer male5660 = dvTotal.getMale5660() + inputResponse.getMale5660();
            Integer female5660 = dvTotal.getFemale5660() + inputResponse.getFemale5660();
            dvTotal.setMale5660(male5660);
            dvTotal.setFemale5660(female5660);

            Integer male6162 = dvTotal.getMale6162() + inputResponse.getMale6162();
            Integer feMale6162 = dvTotal.getFemale6162() + inputResponse.getFemale6162();
            dvTotal.setMale6162(male6162);
            dvTotal.setFemale6162(feMale6162);
        }

        return dvTotal;
    }
}
