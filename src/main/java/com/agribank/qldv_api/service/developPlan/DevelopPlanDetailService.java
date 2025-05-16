package com.agribank.qldv_api.service.developPlan;

import com.agribank.qldv_api.gateway.DevelopmentPlanDetailClient;
import com.agribank.qldv_api.request.developPlan.*;
import com.agribank.qldv_api.response.DefaultResponse;
import com.agribank.qldv_api.response.developPlan.DevelopPlanDetailResponse;
import com.agribank.qldv_api.response.developPlan.DevelopPlanResponse;
import com.agribank.qldv_api.service.CheckAuthorityService;
import com.agribank.qldvutils.entity.DevelopmentPlanDetail;
import com.agribank.qldvutils.exception.CommonException;
import com.agribank.qldvutils.request.developPlan.DevelopPlanUpdateRequest;
import com.agribank.qldvutils.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DevelopPlanDetailService {
    private final DevelopmentPlanDetailClient developmentPlanDetailClient;
    private final DevelopPlanService developPlanService;
    private final CheckAuthorityService checkAuthorityService;
    private final ModelMapper modelMapper;


    public List<DevelopPlanDetailResponse> getPlanDetail(GetDevelopmentPlanRequest request) {
        try {
            PageResponse<DevelopPlanResponse> planPageResponse = developPlanService.getPlan(request);
            if (planPageResponse.getData().isEmpty()) {
                return null;
            }
            DefaultResponse<List<DevelopmentPlanDetail>> response = developmentPlanDetailClient.getByRefId(planPageResponse.getData().get(0).getId());
            if (!response.getSuccess()) {
                return null;
            }
            if (response.getData().isEmpty()) {
                return null;
            }
            return response.getData().stream()
                    .map(role -> modelMapper.map(role, DevelopPlanDetailResponse.class))
                    .toList();
        } catch (Exception e) {
            throw new CommonException(e.getMessage());
        }
    }

    public String addDevelopPlanDetail(DevelopPlanDataRequest dataRequest) {
        checkAuthorityService.hasAuthorityOverOrganization(dataRequest.getOrganizationCode());
        String id = UUID.randomUUID().toString();
        GetDevelopmentPlanRequest dupRequest = new GetDevelopmentPlanRequest(dataRequest.getOrganizationCode(), dataRequest.getName(), dataRequest.getStart(), dataRequest.getEnd());
        dupRequest.setPage(0);
        dupRequest.setPageSize(1);
        dupRequest.setSort("ASC");
        try {
            List<DevelopPlanDetailResponse> duplicate = getPlanDetail(dupRequest);
            if (duplicate == null) {
                duplicate = new ArrayList<>();
            }
            if (!duplicate.isEmpty()) {
                return "Kế hoạch phát triển đã tồn tại";
            }
            List<DevelopmentPlanDetail> listDetail = dataRequest.getData().stream().map(year -> {
                DevelopmentPlanDetail temp = new DevelopmentPlanDetail(id, year.getTarget(), year.getMin(), year.getYear());
                temp.setId(UUID.randomUUID().toString());
                return temp;
            }).toList();
            DefaultResponse<List<DevelopmentPlanDetail>> response = developmentPlanDetailClient.saveDetail(listDetail);
            if (!response.getSuccess()) {
                return "Có lỗi trong quá trình thêm kế hoạch phát triển!";
            }
            int totalTarget = dataRequest.getData().stream().mapToInt(DevelopPlanDetailRequest::getTarget).sum();
            return developPlanService.addDevelopPlan(new DevelopPlanRequest(id, dataRequest.getOrganizationCode(), dataRequest.getName(), dataRequest.getStart(), dataRequest.getEnd(), totalTarget, 0));
        } catch (Exception e) {
            throw new CommonException(e.getMessage());
        }
    }

    public String updateDevelopPlanDetail(DevelopPlanDetailUpdateRequest dataRequest) {
        int totalTarget = 0;
        int oldTotal = 0;
        List<DevelopPlanDetailRequest> listDetail = new ArrayList<>();
        List<DevelopPlanDetailRequest> listMatch = new ArrayList<>();
        List<DevelopmentPlanDetail> listAdd = new ArrayList<>();
        GetDevelopmentPlanRequest planRequest = new GetDevelopmentPlanRequest(dataRequest.getOrganizationCode(), dataRequest.getName(), dataRequest.getStart(), dataRequest.getEnd());
        planRequest.setPage(0);
        planRequest.setPageSize(1);
        planRequest.setSort("ASC");
        checkAuthorityService.hasAuthorityOverOrganization(dataRequest.getOrganizationCode());
        try {
            PageResponse<DevelopPlanResponse> developPlanResponse = developPlanService.getPlan(planRequest);
            if (developPlanResponse.getData().isEmpty()) {
                return "Không tồn tại kế hoạch phát triển!";
            }
            DefaultResponse<List<DevelopmentPlanDetail>> response = developmentPlanDetailClient.getByRefId(dataRequest.getRefId());
            if (!response.getSuccess()) {
                return "Có lỗi trong quá trình cập nhật kế hoạch phát triển!";
            }
            if (response.getData().isEmpty()) {
                return "Không tồn tại chi kế hoạch phát triển!";
            }
            for (DevelopmentPlanDetail detail: response.getData()) {
                listDetail.add(new DevelopPlanDetailRequest(detail.getYear(), detail.getTarget(), detail.getMin()));
                oldTotal = oldTotal + detail.getTarget();
            }
            for (DevelopPlanDetailRequest request : dataRequest.getData()) {
                if (listDetail.contains(request)) {
                    listMatch.add(request);
                } else {
                    DevelopmentPlanDetail temp = new DevelopmentPlanDetail(dataRequest.getRefId(), request.getTarget(), request.getMin(), request.getYear());
                    temp.setId(UUID.randomUUID().toString());
                    listAdd.add(temp);
                }
                totalTarget = totalTarget + request.getTarget();
            }
            List<Integer> listDel = listDetail.stream().filter(data -> !listMatch.contains(data))
                    .map(DevelopPlanDetailRequest::getYear)
                    .toList();
            DefaultResponse<DevelopPlanUpdateRequest> delResponse = developmentPlanDetailClient
                    .deleteById(new DevelopPlanUpdateRequest(dataRequest.getRefId(), listDel));
            if (!delResponse.getSuccess()) {
                return "Có lỗi trong quá trình xóa cập nhật kế hoạch phát triển!";
            }
            DefaultResponse<List<DevelopmentPlanDetail>> addResponse = developmentPlanDetailClient.saveDetail(listAdd);
            if (!addResponse.getSuccess()) {
                return "Có lỗi trong quá trình thêm cập nhật kế hoạch phát triển!";
            }
            if (totalTarget == oldTotal) {
                return "Thêm kế hoạch phát triển thành công!";
            }
            return developPlanService.addDevelopPlan(new DevelopPlanRequest(dataRequest.getRefId(), dataRequest.getOrganizationCode(),
                    dataRequest.getName(), dataRequest.getStart(), dataRequest.getEnd(), totalTarget,
                    developPlanResponse.getData().get(0).getHasChild()));
        } catch (Exception e) {
            throw new CommonException(e.getMessage());
        }
    }
}
