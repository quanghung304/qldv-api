package com.agribank.qldv_api.service;

import com.agribank.qldv_api.enums.EApprovalStatus;
import com.agribank.qldv_api.enums.EForm;
import com.agribank.qldv_api.enums.EReport01Type;
import com.agribank.qldv_api.gateway.EstablishmentDissolveDraftClient;
import com.agribank.qldv_api.jwt.UserDetailsImpl;
import com.agribank.qldv_api.request.DraftRequest;
import com.agribank.qldv_api.request.establishmentDissolve.EstablishmentDissolveRequest;
import com.agribank.qldv_api.request.establishmentDissolveDraft.EDDraftSearchRequest;
import com.agribank.qldv_api.response.DraftResponse;
import com.agribank.qldv_api.response.establishmentDissolveDraft.EDDraftResponse;
import com.agribank.qldvutils.entity.EstablishmentDissolve;
import com.agribank.qldvutils.entity.EstablishmentDissolveDraft;
import com.agribank.qldvutils.entity.Organization;
import com.agribank.qldvutils.exception.CommonException;
import com.agribank.qldvutils.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.agribank.qldv_api.enums.Constants.BRANCH_CODE_HEAD_QUARTER;

@Service
@RequiredArgsConstructor
public class EstablishmentDissolveDraftService {
    private final ModelMapper modelMapper;
    private final EstablishmentDissolveDraftClient client;
    private final OrganizationService organizationService;
    private final EstablishmentDissolveService establishmentDissolveService;
    private final UserService userService;
    private final CheckAuthorityService checkAuthorityService;

    private final EForm form = EForm.BIEU_02_ESTA;

    public EstablishmentDissolveDraft createOrUpdate(EstablishmentDissolveRequest request) {
        checkAuthorityService.hasAuthorityOverOrganization(request.getCode());
        EstablishmentDissolveDraft establishmentDissolve = null;
        if (Objects.nonNull(request.getId())) {
            establishmentDissolve = client.findById(request.getId()).getData();
        }

        if (Objects.isNull(establishmentDissolve)){
            establishmentDissolve = new EstablishmentDissolveDraft();
            establishmentDissolve.setId(UUID.randomUUID().toString());
        }

        if (EReport01Type.getValue(request.getType()) != EReport01Type.ESTABLISH.getId()
                && EReport01Type.getValue(request.getType()) != EReport01Type.DISSOLVE.getId()){
            throw new CommonException("Kiểm tra lại type");
        }

        Organization organization = organizationService.findByCode(request.getCode());
        if (Objects.isNull(organization)){
            throw new CommonException("Không tồn tại TCD có mã: " + request.getCode());
        }

        UserDetailsImpl userRequested = userService.getUserRequested();

        establishmentDissolve.setCode(request.getCode());
        establishmentDissolve.setName(request.getName());
        establishmentDissolve.setForm(request.getForm());
        establishmentDissolve.setType(request.getType());
        establishmentDissolve.setConclusionNumber(request.getConclusionNumber());
        establishmentDissolve.setConclusionDate(request.getConclusionDate());
        establishmentDissolve.setDecisionNumber(request.getDecisionNumber());
        establishmentDissolve.setDecisionDate(request.getDecisionDate());
        establishmentDissolve.setEffectiveDate(request.getEffectiveDate());
        establishmentDissolve.setStatus(EApprovalStatus.PENDING.getId());
        establishmentDissolve.setUserBrcdCreated(userRequested.getBrcd());
        establishmentDissolve.setUsernameCreated(userRequested.getUsername());

        return client.save(establishmentDissolve).getData();
//        return "Tạo yêu cầu thành công!";
    }

    public PageResponse<EDDraftResponse> search(EDDraftSearchRequest request){
        PageResponse<EDDraftResponse> response = new PageResponse<>();
        UserDetailsImpl userRequested = userService.getUserRequested();

        List<String> codeChild = organizationService.getChildCode(userRequested.getOrganizationCode());
        if (Objects.nonNull(request.getCode()) && !codeChild.contains(request.getCode())
                && BRANCH_CODE_HEAD_QUARTER < Integer.parseInt(userRequested.getOrganizationCode())){
            return response;
        }

        if (BRANCH_CODE_HEAD_QUARTER < userRequested.getBrcd() && Objects.isNull(request.getCode())){
            request.setCode(userRequested.getOrganizationCode());
        }

        request.setOrderBy("code");

        PageResponse<EstablishmentDissolveDraft> draftPageResponse = client.search(request).getData();
        if (Objects.isNull(draftPageResponse)) {
            return response;
        }

        response.setTotalPages(draftPageResponse.getTotalPages());
        response.setCurrentPage(draftPageResponse.getCurrentPage());
        response.setTotalItems(draftPageResponse.getTotalItems());

        if (Objects.nonNull(draftPageResponse.getData())) {
            response.setData(draftPageResponse.getData().stream()
                    .map(organization -> modelMapper.map(organization, EDDraftResponse.class)
                    ).toList()
            );
        }

        return response;
    }

    public List<DraftResponse> approve(List<DraftRequest> requests){
        List<String> ids = requests.stream().map(DraftRequest::getId).distinct().toList();

        List<EstablishmentDissolveDraft> dissolveDraftList = client.findAllByIds(ids).getData();
        Map<String, EstablishmentDissolveDraft> dissolveDraftMap = new HashMap<>();
        if (Objects.isNull(dissolveDraftList) || dissolveDraftList.isEmpty()) {
            throw new CommonException("Kiểm tra lại id request");
        }

        for (EstablishmentDissolveDraft d : dissolveDraftList) {
            dissolveDraftMap.put(d.getId(), d);
        }

        List<DraftResponse> responses = new ArrayList<>();
        List<EstablishmentDissolve> establishmentDissolvesSave = new ArrayList<>();
        List<EstablishmentDissolveDraft> establishmentDissolveDrafts = new ArrayList<>();

        UserDetailsImpl userRequested = userService.getUserRequested();
        for (DraftRequest draftRequest : requests) {
            //kiểm tra giá trị approve
            if (Objects.isNull(draftRequest.getStatus())
                    || EApprovalStatus.getValue(draftRequest.getStatus()) == -1
                    || EApprovalStatus.PENDING.getId() == draftRequest.getStatus()
            ){
                responses.add(fromModel(draftRequest.getId(),
                        draftRequest.getStatus(),
                        "Sai status vui lòng kiểm tra lại!"));
                continue;
            }

            EstablishmentDissolveDraft  establishmentDissolveDraft = dissolveDraftMap.getOrDefault(draftRequest.getId(), null);

            if (Objects.isNull(establishmentDissolveDraft)){
                responses.add(fromModel(draftRequest.getId(),
                        draftRequest.getStatus(),
                        "Không tồn tại bản ghi id: "
                                + draftRequest.getId()));
                continue;
            }


            //chặn duyệt lại những approve đã tùng được thao tác duyệt hoặc từ chối rồi
            if (!establishmentDissolveDraft.getStatus().equals(EApprovalStatus.PENDING.getId())){
                responses.add(
                        fromModel(establishmentDissolveDraft.getId(),
                                draftRequest.getStatus(),
                                "Bản ghi id: "
                                        + establishmentDissolveDraft.getId()
                                        + ", " + establishmentDissolveDraft.getName()
                                        + ", đã được duyệt"));
                continue;
            }

            establishmentDissolveDraft.setUsernameAccepted(userRequested.getUsername());
            establishmentDissolveDraft.setUserBrcdAccepted(userRequested.getBrcd());
            //trường hợp đông ý
            if (draftRequest.getStatus().equals(EApprovalStatus.APPROVED.getId())) {
                establishmentDissolveDraft.setStatus(EApprovalStatus.APPROVED.getId());
                establishmentDissolveDrafts.add(establishmentDissolveDraft);
                responses.add(fromModel(establishmentDissolveDraft.getId(),
                        draftRequest.getStatus(),
                        "Yêu cầu đã được duyệt thành công!"));

                establishmentDissolvesSave.add(modelMapper.map(establishmentDissolveDraft, EstablishmentDissolve.class));
                continue;
            }

            //TH từ chối
            if (draftRequest.getStatus().equals(EApprovalStatus.DENIED.getId())) {
                establishmentDissolveDraft.setStatus(EApprovalStatus.DENIED.getId());
                establishmentDissolveDrafts.add(establishmentDissolveDraft);
                responses.add(fromModel(establishmentDissolveDraft.getId(),
                        draftRequest.getStatus(),
                        "Yêu cầu từ chối đã được duyệt thành công!"));
            }
        }

        if (establishmentDissolveDrafts.isEmpty()){
            return responses;
        }

        establishmentDissolveService.saveAll(establishmentDissolvesSave);
        client.saveAll(establishmentDissolveDrafts);

        return responses;
    }

    private DraftResponse fromModel(String id, Integer approve, String mess) {
        return DraftResponse.builder()
                .id(id)
                .approve(approve+"")
                .message(mess)
                .build();
    }

    public String delete(String id){
        EstablishmentDissolveDraft establishmentDissolveDraft = client.findById(id).getData();
        if (Objects.isNull(establishmentDissolveDraft)) {
            throw new CommonException("Không xóa được yêu cầu! Vui lòng kiểm tra lại sau");
        }

        if (EApprovalStatus.PENDING.getId() != establishmentDissolveDraft.getStatus()){
            throw new CommonException("Yêu cầu đã được duyệt, nên bạn không thể xóa yêu cầu này!");
        }

        client.delete(id);
        return "Xóa yêu cầu thành công!";
    }
}
