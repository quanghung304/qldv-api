package com.agribank.qldv_api.service.development_plan;

import com.agribank.qldv_api.gateway.development_plan.DevelopmentPlanClient;
import com.agribank.qldv_api.request.develop_plan.*;
import com.agribank.qldv_api.response.developPlan.DevelopPlanResponse;
import com.agribank.qldvutils.entity.development_plan.DevelopmentPlan;
import com.agribank.qldvutils.entity.development_plan.DevelopmentPlanDraft;
import com.agribank.qldvutils.exception.CommonException;
import com.agribank.qldvutils.request.development_plan.DevelopPrntBrcdRequest;
import com.agribank.qldvutils.request.development_plan.GetChildPlanRequest;
import com.agribank.qldvutils.request.development_plan.GetPlanRequest;
import com.agribank.qldvutils.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class DevelopPlanService {
    private final DevelopmentPlanClient developmentPlanClient;
    private final ModelMapper modelMapper;


    public Map<String, String> getCombinedFieldMap() {
        return new HashMap<>(DevelopmentPlanDraft.BASE_FIELD_MAP);
    }

    public DevelopmentPlan searchPrntBrcd(DevelopPrntBrcdRequest request){
        return developmentPlanClient.searchPrntBrcd(request).getData();
    }

    public PageResponse<DevelopPlanResponse> getPlan(GetDevelopmentPlanRequest request) {
        GetPlanRequest planRequest = new GetPlanRequest(request.getOrganizationCode(), request.getName(), request.getStartYear(), request.getEndYear());
        planRequest.setPage(request.getPage());
        planRequest.setPageSize(request.getPageSize());
        planRequest.setOrderBy(request.getOrderBy());
        planRequest.setSort(request.getSort());
        try {
            PageResponse<DevelopmentPlan> planPageResponse = developmentPlanClient.getPlan(planRequest).getData();
            PageResponse<DevelopPlanResponse> response = new PageResponse<>();
            if (Objects.isNull(planPageResponse)) {
                return response;
            }

            response.setTotalPages(planPageResponse.getTotalPages());
            response.setCurrentPage(planPageResponse.getCurrentPage());
            response.setTotalItems(planPageResponse.getTotalItems());

            if (Objects.nonNull(planPageResponse.getData())) {
                response.setData(planPageResponse.getData().stream()
                        .map( user -> modelMapper.map(user, DevelopPlanResponse.class)
                        ).toList()
                );
            }
            return response;
        } catch (Exception e) {
            throw new CommonException(e.getMessage());
        }
    }

    public List<DevelopPlanResponse> getChildPlan(GetDevelopmentPlanRequest request) {
        GetChildPlanRequest planRequest = new GetChildPlanRequest(request.getOrganizationCode(), request.getName(), request.getStartYear(), request.getEndYear());
        try {
            List<DevelopmentPlan> prnctPlanResponse = developmentPlanClient.getChildPlan(planRequest).getData();
            return  prnctPlanResponse.stream()
                    .map(user -> modelMapper.map(user, DevelopPlanResponse.class)
                    ).toList();
        } catch (Exception e) {
            throw new CommonException(e.getMessage());
        }
    }

    public DevelopmentPlan save(DevelopmentPlan developPlan) {
        return developmentPlanClient.save(developPlan).getData();
    }

    public DevelopmentPlan findById(String id) {
        return developmentPlanClient.findById(id).getData().orElse(null);
    }
}
