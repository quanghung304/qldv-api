package com.agribank.qldv_api.service.bcsl_report.tcd;


import com.agribank.qldv_api.service.export.ExcelColumnInfoService;
import com.agribank.qldv_api.service.export.ExportPDFReportService;
import com.agribank.qldv_api.service.export.ExportService;
import com.agribank.qldv_api.service.export.ZipHelper;
import com.agribank.qldvutils.entity.ExcelColumnInfo;
import com.agribank.qldvutils.request.bcsl_report.tcd.SearchRpRequest;
import com.agribank.qldvutils.response.PageResponse;
import com.agribank.qldvutils.response.bcsl_report.tcd.BcslTcdRp09Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ExportBcslTcdRp09Service extends ExportService {
    @Autowired
    private BcslTcdReportService bcslTcdReportService;
    @Value("${app.max.rows.export}")
    private Integer MAX_ROWS_EXPORT;

    public ExportBcslTcdRp09Service(ExcelColumnInfoService excelColumnInfoService, ZipHelper zipHelper, ExportPDFReportService exportPDFReportService) {
        super(excelColumnInfoService, zipHelper, exportPDFReportService);
    }


    public int handleGetTotalRecord(Object serviceParam) {
        SearchRpRequest requestParam = (SearchRpRequest) serviceParam;
        requestParam.setPageSize(MAX_ROWS_EXPORT);
        PageResponse<BcslTcdRp09Response> dataDto = bcslTcdReportService.searchRp09(requestParam);
        if(dataDto == null){
            return 0;
        }

        List<BcslTcdRp09Response> data = dataDto.getData();
        if (!data.isEmpty()){
            data.add(countTotalRp09(data));
        }
        return data.size();
    }


    public List<Map<String, Object>> handleGetDataExport(Object serviceParam, int pageIndex) {
        SearchRpRequest requestParam = (SearchRpRequest) serviceParam;
        requestParam.setPage(pageIndex);
        requestParam.setPageSize(MAX_ROWS_EXPORT);
        PageResponse<BcslTcdRp09Response> dataDto = bcslTcdReportService.searchRp09(requestParam);
        if(Objects.isNull(dataDto)){
            return new ArrayList<>();
        }

        List<BcslTcdRp09Response> data = dataDto.getData();
        if (!data.isEmpty()){
            data.add(countTotalRp09(data));
        }

        List<Map<String, Object>> exportData = new ArrayList<>();
        for(BcslTcdRp09Response bcslRp09Response : data){
            Map<String, Object> item = new HashMap<>();
            item.put("organizationName", bcslRp09Response.getOrganizationName());
            item.put("partyMember", bcslRp09Response.getPartyMember());
            item.put("fullPartyMember", bcslRp09Response.getFullPartyMember());
            item.put("preliminaryPartyMember", bcslRp09Response.getPreliminaryPartyMember());
            item.put("newPartyMember", bcslRp09Response.getNewPartyMember());
            item.put("reissuePartyCard1", bcslRp09Response.getReissuePartyCard1());
            item.put("reissuePartyCard2", bcslRp09Response.getReissuePartyCard2());
            item.put("reissuePartyCard3", bcslRp09Response.getReissuePartyCard3());
            item.put("lost", bcslRp09Response.getLost());
            item.put("broken", bcslRp09Response.getBroken());
            item.put("otherReason", bcslRp09Response.getOtherReason());
            item.put("notPartyCard", bcslRp09Response.getNotPartyCard());
            item.put("admission2", bcslRp09Response.getAdmission2());
            item.put("reinstatement", bcslRp09Response.getReinstatement());
            item.put("suggestionUnion", bcslRp09Response.getSuggestionUnion());
            item.put("suggestionYouthUnion", bcslRp09Response.getSuggestionYouthUnion());
            item.put("student", bcslRp09Response.getStudent());
            item.put("nonAgribankParty", bcslRp09Response.getNonAgribankParty());
            exportData.add(item);
        }
        return exportData;
    }

    public Object handleGetCellValue(Object data, ExcelColumnInfo columnExport) {
        return super.handleGetCellValue(data, columnExport);
    }

    private BcslTcdRp09Response countTotalRp09(List<BcslTcdRp09Response> bcslTcdRp09Responses){
        BcslTcdRp09Response bcslTcdRp09Response = BcslTcdRp09Response.builder()
                .typeName("Total")
                .build();

        for (BcslTcdRp09Response bcslTcd : bcslTcdRp09Responses) {
            bcslTcdRp09Response.setPartyMember(bcslTcdRp09Response.getPartyMember() + bcslTcd.getPartyMember());
            bcslTcdRp09Response.setPreliminaryPartyMember(bcslTcdRp09Response.getPreliminaryPartyMember() + bcslTcd.getPreliminaryPartyMember());
            bcslTcdRp09Response.setFullPartyMember(bcslTcdRp09Response.getFullPartyMember() + bcslTcd.getFullPartyMember());
            bcslTcdRp09Response.setNewPartyMember(bcslTcdRp09Response.getNewPartyMember() + bcslTcd.getNewPartyMember());
            bcslTcdRp09Response.setReinstatement(bcslTcdRp09Response.getReinstatement() + bcslTcd.getReinstatement());
            bcslTcdRp09Response.setReissuePartyCard1(bcslTcdRp09Response.getReissuePartyCard1() + bcslTcd.getReissuePartyCard1());
            bcslTcdRp09Response.setReissuePartyCard2(bcslTcdRp09Response.getReissuePartyCard2() + bcslTcd.getReissuePartyCard2());
            bcslTcdRp09Response.setReissuePartyCard3(bcslTcdRp09Response.getReissuePartyCard3() + bcslTcd.getReissuePartyCard3());
            bcslTcdRp09Response.setLost(bcslTcdRp09Response.getLost() + bcslTcd.getLost());
            bcslTcdRp09Response.setBroken(bcslTcdRp09Response.getBroken() + bcslTcd.getBroken());
            bcslTcdRp09Response.setOtherReason(bcslTcdRp09Response.getOtherReason() + bcslTcd.getOtherReason());
            bcslTcdRp09Response.setNotPartyCard(bcslTcdRp09Response.getNotPartyCard() + bcslTcd.getNotPartyCard());
            bcslTcdRp09Response.setAdmission2(bcslTcdRp09Response.getAdmission2() + bcslTcd.getAdmission2());
            bcslTcdRp09Response.setSuggestionUnion(bcslTcdRp09Response.getSuggestionUnion() + bcslTcd.getSuggestionUnion());
            bcslTcdRp09Response.setSuggestionYouthUnion(bcslTcdRp09Response.getSuggestionYouthUnion() + bcslTcd.getSuggestionYouthUnion());
            bcslTcdRp09Response.setStudent(bcslTcdRp09Response.getStudent() + bcslTcd.getStudent());
            bcslTcdRp09Response.setNonAgribankParty(bcslTcdRp09Response.getNonAgribankParty() + bcslTcd.getNonAgribankParty());
        }

        return bcslTcdRp09Response;
    }
}
