package com.agribank.qldv_api.service.report26;

import com.agribank.qldv_api.gateway.report26.Report26Client;
import com.agribank.qldv_api.service.CheckAuthorityService;
import com.agribank.qldvutils.entity.report26.Report26;
import com.agribank.qldvutils.request.report26.Report26SearchRequest;
import com.agribank.qldvutils.request.report_dv.SearchRp34Request;
import com.agribank.qldvutils.response.PageResponse;
import com.agribank.qldvutils.response.report26.Report26DtoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class Report26Service {
    private final Report26Client client;
    private final CheckAuthorityService checkAuthorityService;

    public void saveAll(List<Report26> report26s) {
        client.saveAll(report26s);
    }

    public void save(Report26 report26) {
        client.save(report26);
    }

    public PageResponse<Report26DtoResponse> search(Report26SearchRequest request){
        if (Objects.isNull(request.getOrderBy())){
            request.setOrderBy("staffCode");
        }
        checkAuthorityService.hasAuthorityOverOrganization(request.getOrganizationCode());

        return client.search(request).getData();
    }

    public Report26 findByRefId(String refId) {
        return client.findByRefId(refId).getData();
    }

    public PageResponse<Report26> search34(SearchRp34Request request){
        return client.searchRp34(request).getData();
    }
}
