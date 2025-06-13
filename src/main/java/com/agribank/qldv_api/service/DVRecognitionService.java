package com.agribank.qldv_api.service;

import com.agribank.qldv_api.enums.EApprovalStatus;
import com.agribank.qldv_api.enums.EForm;
import com.agribank.qldv_api.enums.ERecordStatus;
import com.agribank.qldv_api.gateway.DVClient;
import com.agribank.qldv_api.gateway.DVRecognitionClient;
import com.agribank.qldv_api.gateway.DVRecognitionDraftClient;
import com.agribank.qldv_api.gateway.RequestClient;
import com.agribank.qldv_api.jwt.UserDetailsImpl;
import com.agribank.qldv_api.request.dvRecognition.DVRecognitionRequest;
import com.agribank.qldv_api.response.dvRecognition.DVRecognitionResponse;
import com.agribank.qldv_api.service.handler.EntityHandler;
import com.agribank.qldvutils.dto.DVRecognitionDto;
import com.agribank.qldvutils.entity.*;
import com.agribank.qldvutils.exception.CommonException;
import com.agribank.qldvutils.request.SearchDVRecognitionRequest;
import com.agribank.qldvutils.response.PageResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DVRecognitionService implements EntityHandler {
    private final ModelMapper modelMapper;
    private final UserService userService;
    ObjectMapper objectMapper;
    private final DVRecognitionDraftClient draftClient;
    private final DVRecognitionClient client;
    private final DVClient dvClient;
    private final DVService dvService;
    private final CheckAuthorityService checkAuthorityService;
    private final RequestService requestService;
    private final RequestClient requestClient;
    private final EForm form = EForm.BIEU_21;
    private final DVRecognitionDraftService dvRecognitionDraftService;

    private Map<String, String> getCombinedFieldMap() {
        return new LinkedHashMap<>(DVRecognitionDraft.FIELD_MAP);
    }

    public PageResponse<DVRecognitionResponse> search(SearchDVRecognitionRequest request){
        try {
            request = Objects.nonNull(request) ? request : new SearchDVRecognitionRequest();
            checkAuthorityService.hasAuthorityOverOrganization(request.getOrganizationCode());

            PageResponse<DVRecognitionDto> requestDtoPageResponse = client.search(request).getData();
            List<DVRecognitionDto> dvRecognitionDtos = requestDtoPageResponse.getData();

            List<DVRecognitionResponse> responseList = new ArrayList<>();
            for (DVRecognitionDto dto : dvRecognitionDtos) {
                DVRecognitionResponse response = modelMapper.map(dto, DVRecognitionResponse.class);
                responseList.add(response);
            }

            PageResponse<DVRecognitionResponse> response = new PageResponse<>();
            response.setTotalPages(requestDtoPageResponse.getTotalPages());
            response.setCurrentPage(requestDtoPageResponse.getCurrentPage());
            response.setTotalItems(requestDtoPageResponse.getTotalItems());
            response.setData(responseList);

            return response;
        } catch (Exception e) {
            throw new CommonException(e.getMessage());
        }
    }

    public String createOrUpdateDraft(DVRecognitionRequest request) {
        UserDetailsImpl userRequested = userService.getUserRequested();

        DVRecognition dvRecognition = null;
        if (Objects.nonNull(request.getId())){
            dvRecognition = client.findById(request.getId()).getData().orElse(null);
        }

        DVRecognitionDraft draft = new DVRecognitionDraft();

        draft.setStaffCode(request.getDvCode());
        draft.setStaffName(request.getDvName());
        draft.setConclusionNumber(request.getConclusionNumber());
        draft.setConclusionDate(request.getConclusionDate());
        draft.setDecisionNumber(request.getDecisionNumber());
        draft.setDecisionDate(request.getDecisionDate());
        draft.setCreatedBy(userRequested.getId());
        draft.setStatus(EApprovalStatus.PENDING.getId());
        if (Objects.nonNull(dvRecognition)){
            draft.setRefId(request.getId());
        }

        DVRecognitionDraft dvRecognitionDraft = draftClient.save(draft).getData();

        Request dvRecognitionRequest = requestService.initializeRequest(dvRecognitionDraft, dvRecognition, form, getCombinedFieldMap());
        dvRecognitionRequest.setOrganizationCode(dvClient.findByStaffCode(request.getDvCode()).getData().getOrganizationCode());
        dvRecognitionRequest.setStaffCode(request.getDvCode());
        dvRecognitionRequest.setReferenceId(dvRecognitionDraft.getId());
        dvRecognitionRequest.setCreatedBy(userRequested.getId());
        requestClient.save(dvRecognitionRequest);

        return "Tạo yêu cầu thành công";
    }

    public String createRequestDelete(String id){
        DVRecognition dvRecognition = client.findById(id).getData()
                .orElseThrow(()->new CommonException("Sai id, vui lòng kiểm tra lại"));

        UserDetailsImpl userRequested = userService.getUserRequested();
        Request request = requestService.initializeRequest(null, dvRecognition, form, getCombinedFieldMap());
        request.setCreatedBy(userRequested.getId());
        request.setReferenceId(id);
        request.setOrganizationCode(dvClient.findByStaffCode(dvRecognition.getStaffCode()).getData().getOrganizationCode());
        request.setStaffCode(dvRecognition.getStaffCode());
        requestClient.save(request);

        return "Tạo yêu cầu thành công";
    }
    @Override
    public boolean applyCreate(String referenceId, UserDetailsImpl userDetails) {

        DVRecognitionDraft dvRecognitionDraft = dvRecognitionDraftService.findById(referenceId);

        dvRecognitionDraft.setStatus(EApprovalStatus.APPROVED.getId());
        dvRecognitionDraft.setApprovedBy(userDetails.getId());

        DVRecognition dvRecognition = modelMapper.map(dvRecognitionDraft, DVRecognition.class);

        client.save(dvRecognition);
        dvRecognitionDraftService.save(dvRecognitionDraft);

        DV dv = dvClient.findByStaffCode(dvRecognitionDraft.getStaffCode()).getData();
        dv.setOfficialRecognitionDay(dvRecognitionDraft.getDecisionDate());
        dvClient.save(dv);
        return true;
    }

    @Override
    public boolean applyUpdate(String referenceId) {
        UserDetailsImpl userRequested = userService.getUserRequested();

        DVRecognitionDraft dvRecognitionDraft = dvRecognitionDraftService.findById(referenceId);
        dvRecognitionDraft.setStatus(EApprovalStatus.APPROVED.getId());
        dvRecognitionDraft.setApprovedBy(userRequested.getId());

        dvRecognitionDraftService.save(dvRecognitionDraft);

        DVRecognition dvRecognition = client.findById(dvRecognitionDraft.getRefId()).getData()
                .orElseThrow(() -> new CommonException("Không tìm thấy bản ghi"));

        dvRecognition.setStaffCode(dvRecognitionDraft.getStaffCode());
        dvRecognition.setConclusionNumber(dvRecognitionDraft.getConclusionNumber());
        dvRecognition.setConclusionDate(dvRecognitionDraft.getConclusionDate());
        dvRecognition.setDecisionNumber(dvRecognitionDraft.getDecisionNumber());
        dvRecognition.setDecisionDate(dvRecognitionDraft.getDecisionDate());
        client.save(dvRecognition);

        DV dv = dvClient.findByStaffCode(dvRecognitionDraft.getStaffCode()).getData();
        dv.setOfficialRecognitionDay(dvRecognitionDraft.getDecisionDate());
        dvClient.save(dv);
        return true;
    }

    @Override
    public boolean applyDelete(String referenceId) {
        DVRecognition dvRecognition = client.findById(referenceId).getData()
                .orElseThrow(()->new CommonException("Lỗi"));
        dvRecognition.setDeleted(ERecordStatus.DELETED.getStatus());

        client.save(dvRecognition);
        return true;
    }

    @Override
    public void setDenied(String referenceId) {
        DVRecognitionDraft dvRecognitionDraft = dvRecognitionDraftService.findById(referenceId);
        dvRecognitionDraft.setStatus(EApprovalStatus.DENIED.getId());
        dvRecognitionDraftService.save(dvRecognitionDraft);
    }

    public DVRecognitionResponse getDetail(String id){
        DVRecognition dvRecognition = client.findById(id).getData().orElse(null);

        if (Objects.isNull(dvRecognition)){
            throw new CommonException("Không tìm thấy dữ liệu");
        }

        DV dv = dvService.findByStaffCode(dvRecognition.getStaffCode());
        DVRecognitionResponse response = new DVRecognitionResponse();
        response.setDvCode(dvRecognition.getStaffCode());
        response.setConclusionNumber(dvRecognition.getConclusionNumber());
        response.setConclusionDate(dvRecognition.getConclusionDate());
        response.setDecisionNumber(dvRecognition.getDecisionNumber());
        response.setDecisionDate(dvRecognition.getDecisionDate());
        if (Objects.nonNull(dv)){
            response.setDvName(dv.getFullName());
        }
        return response;
    }

}
