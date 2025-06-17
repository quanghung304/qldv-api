package com.agribank.qldv_api.service.tcd;

import com.agribank.qldv_api.response.tcd.Rp0304Response;
import com.agribank.qldv_api.service.export.ExcelColumnInfoService;
import com.agribank.qldv_api.service.export.ExportService;
import com.agribank.qldv_api.service.export.ZipHelper;
import com.agribank.qldv_api.utils.CommonUtils;
import com.agribank.qldvutils.entity.ExcelColumnInfo;
import com.agribank.qldvutils.request.organization.OrganizationRpSearchRequest;
import com.agribank.qldvutils.response.PageResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.*;

@Service("exportTcd")
public class ExportTcdReport03Service extends ExportService {
    @Lazy
    @Autowired
    private TcdReportService organizationService;
    @Value("${app.max.rows.export}")
    private Integer MAX_ROWS_EXPORT;

    public ExportTcdReport03Service(ExcelColumnInfoService excelColumnInfoService, ZipHelper zipHelper) {
        super(excelColumnInfoService,  zipHelper);
    }


    public int handleGetTotalRecord(Object serviceParam) {
        OrganizationRpSearchRequest requestParam = (OrganizationRpSearchRequest) serviceParam;
        requestParam.setPageSize(MAX_ROWS_EXPORT);
        PageResponse<Rp0304Response> dataDto = organizationService.search0304(requestParam);
        if(dataDto == null){
            return 0;
        }
        return Integer.parseInt(String.valueOf(dataDto.getTotalItems()));
    }


    public List<Map<String, Object>> handleGetDataExport(Object serviceParam, int pageIndex) {
        OrganizationRpSearchRequest requestParam = (OrganizationRpSearchRequest) serviceParam;
        requestParam.setPage(pageIndex);
        requestParam.setPageSize(MAX_ROWS_EXPORT);
        PageResponse<Rp0304Response> dataDto = organizationService.search0304(requestParam);
        if(Objects.isNull(dataDto)){
            return new ArrayList<>();
        }

        List<Rp0304Response> rp0304Responses = dataDto.getData();
        List<Map<String, Object>> exportData = new ArrayList<>();
        for(Rp0304Response rp0304Response : rp0304Responses){
            Map<String, Object> item = new HashMap<>();
            item.put("code", rp0304Response.getCode());
            item.put("name", rp0304Response.getName());
            item.put("form", rp0304Response.getForm());
            item.put("establishmentDate", CommonUtils.dateToString(rp0304Response.getEstablishmentDate()));
            item.put("upgradeDate", CommonUtils.dateToString(
                    CommonUtils.parseDateString(rp0304Response.getUpgradeDate()+"")
            ));
            item.put("downgradeDate", CommonUtils.dateToString(
                    CommonUtils.parseDateString(rp0304Response.getDowngradeDate()+"")
            ));
            exportData.add(item);
        }
        return exportData;
    }

    public Object handleGetCellValue(Object data, ExcelColumnInfo columnExport) {
        return super.handleGetCellValue(data, columnExport);
    }
}
