package com.agribank.qldv_api.service.developPlan;

import com.agribank.qldv_api.gateway.DevelopmentPlanClient;
import com.agribank.qldv_api.request.developPlan.*;
import com.agribank.qldv_api.request.organization.OrganizationSearchRequest;
import com.agribank.qldv_api.response.DefaultResponse;
import com.agribank.qldv_api.response.developPlan.DevelopPlanResponse;
import com.agribank.qldv_api.response.organization.OrganizationResponse;
import com.agribank.qldv_api.service.OrganizationService;
import com.agribank.qldvutils.entity.DevelopmentPlan;
import com.agribank.qldvutils.exception.CommonException;
import com.agribank.qldvutils.request.developPlan.DevelopPrntBrcdRequest;
import com.agribank.qldvutils.request.developPlan.GetChildPlanRequest;
import com.agribank.qldvutils.request.developPlan.GetPlanRequest;
import com.agribank.qldvutils.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class DevelopPlanService {
    private final DevelopmentPlanClient developmentPlanClient;
    private final OrganizationService organizationService;
    private final ModelMapper modelMapper;

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

    public String addDevelopPlan(DevelopPlanRequest planRequest) {
        DefaultResponse<DevelopmentPlan> validPrntBrcd = new DefaultResponse<>(true, null, null);
        DefaultResponse<DevelopmentPlan> updatePrntBrcd = new DefaultResponse<>(true, null, null);
        DevelopmentPlan dataUpdate = DevelopmentPlan.builder()
                .hasChild(1)
                .build();
        try {
            String prntBrcd = searchPrntBrcd(planRequest.getOrganizationCode());

            if (prntBrcd != null && !prntBrcd.equals(planRequest.getOrganizationCode())) {
                validPrntBrcd = developmentPlanClient.searchPrntBrcd(new DevelopPrntBrcdRequest(prntBrcd, planRequest.getStart(), planRequest.getEnd()));
                dataUpdate = validPrntBrcd.getData();
            }
            if ((!validPrntBrcd.getSuccess()) || (prntBrcd != null && !prntBrcd.equals(planRequest.getOrganizationCode()) && dataUpdate == null)) {
                return "Không tìm thấy dữ liệu chi, đảng bộ cha. Vui lòng tạo kế hoạch phát triển theo đúng quy trình!";
            }
            if (dataUpdate.getHasChild() == 0) {
                dataUpdate.setHasChild(1);
                updatePrntBrcd = developmentPlanClient.savePlan(dataUpdate);
            }
            if (!updatePrntBrcd.getSuccess()) {
                return "Có lỗi trong quá trình thêm kế hoạch phát triển!";
            }
            DevelopmentPlan addPlan = new DevelopmentPlan(planRequest.getId(), planRequest.getOrganizationCode(), planRequest.getName(), planRequest.getStart(),
                    planRequest.getEnd(), planRequest.getTarget(), prntBrcd, planRequest.getHasChild(), new Timestamp(System.currentTimeMillis()),
                    new Timestamp(System.currentTimeMillis()));
            DefaultResponse<DevelopmentPlan> planDefaultResponse = developmentPlanClient.savePlan(addPlan);
            if (!planDefaultResponse.getSuccess()) {
                return "Có lỗi trong quá trình thêm kế hoạch phát triển!";
            }
            return "Thêm kế hoạch phát triển thành công!";
        } catch (Exception e) {
            throw new CommonException(e.getMessage());
        }
    }

    private String searchPrntBrcd(String brcd) {
       String prnt = null;
       OrganizationSearchRequest request =  new OrganizationSearchRequest("", "", brcd);
       request.setPage(0);
       request.setPageSize(1);
       request.setSort("ASC");
       try {
           OrganizationResponse prntBrcds = organizationService.get(brcd);
           if (prntBrcds != null) {
               prnt = prntBrcds.getParentCode();
           }
           if (!Objects.equals(brcd, prnt) && prnt != null && !prnt.equals("1000")) {
               return prnt;
           }
          return null;
       } catch(Exception e) {
           throw new CommonException(e.getMessage());
       }
    }
}
