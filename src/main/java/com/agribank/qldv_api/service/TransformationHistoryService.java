package com.agribank.qldv_api.service;

import com.agribank.qldv_api.enums.Constants;
import com.agribank.qldv_api.enums.EApprovalStatus;
import com.agribank.qldv_api.enums.EForm;
import com.agribank.qldv_api.gateway.OrganizationClient;
import com.agribank.qldv_api.gateway.RequestClient;
import com.agribank.qldv_api.gateway.TransformationHistoryDraftClient;
import com.agribank.qldv_api.jwt.UserDetailsImpl;
import com.agribank.qldv_api.request.ApproveRequest;
import com.agribank.qldv_api.request.organizationTransform.OrganizationTransformRequest;
import com.agribank.qldvutils.entity.Organization;
import com.agribank.qldvutils.entity.Request;
import com.agribank.qldvutils.entity.TransformationHistoryDraft;
import com.agribank.qldvutils.exception.CommonException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class TransformationHistoryService {
    private final TransformationHistoryDraftClient historyDraftClient;
    private final OrganizationClient organizationClient;
    private final RequestClient requestClient;
    private final CheckAuthorityService checkAuthorityService;
    private final OrganizationService organizationService;
    private final RequestService requestService;
    private final ModelMapper modelMapper;

    private final EForm form = EForm.BIEU_02;
    private final Set<String> ignoredProperties = Set.of("id", "createdBy", "approvedBy", "created_at", "updated_at");

    public TransformationHistoryDraft createTransformRequest(OrganizationTransformRequest request) {
        Organization organization = organizationService.findByCode(request.getOrganizationCode());
        if (Objects.isNull(organization)){
            throw new CommonException("Không tồn tại TCD có mã: " + request.getOrganizationCode());
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        checkAuthorityService.hasAuthorityOverOrganization(request.getOrganizationCode());

        TransformationHistoryDraft draft = modelMapper.map(request, TransformationHistoryDraft.class);
        draft.setSubmitter(userDetails.getStaffCode());
        draft.setStatus(EApprovalStatus.PENDING.getId());
        TransformationHistoryDraft transformationHistoryDraft = historyDraftClient.save(draft).getData();

        Request transformRequest = requestService.initializeRequest(transformationHistoryDraft, null, TransformationHistoryDraft.class, ignoredProperties);
        transformRequest.setCreatedBy(userDetails.getStaffCode());
        requestClient.save(transformRequest);

        return transformationHistoryDraft;
    }

    public List<TransformationHistoryDraft> getDrafttList(Integer status) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        return historyDraftClient.getList(userDetails.getOrganizationCode(), status).getData();
    }

    public TransformationHistoryDraft getDraft(String id) {
        return historyDraftClient.findById(id).getData();
    }

    public List<String> update(List<ApproveRequest> requestList) {
        try {
            List<String> idList = new ArrayList<>();
            Map<String, Integer> idAndStatusMap = new HashMap<>();

            for (ApproveRequest request : requestList) {
                idList.add(request.getId());
                idAndStatusMap.put(request.getId(), request.getStatus());
            }

            List<TransformationHistoryDraft> draftList = historyDraftClient.findAllById(idList);

            List<String> codeList = new ArrayList<>();
            for (TransformationHistoryDraft draft : draftList) {
                codeList.add(draft.getOrganizationCode());
            }

            List<Organization> organizationList = organizationClient.findAllByCode(codeList).getData();
            Map<String, Organization> codeAndOrganizationMap = new HashMap<>();
            for (Organization organization: organizationList) {
                codeAndOrganizationMap.put(organization.getCode(), organization);
            }

            List<String> response = new ArrayList<>();
            List<TransformationHistoryDraft> updatedDrafts = new ArrayList<>();
            List<Organization> updatedOrganizations = new ArrayList<>();
            UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            for (TransformationHistoryDraft draft : draftList) {
                if (
                        Objects.equals(userDetails.getOrganizationCode(), Constants.BTCDU_CODE) || draft.getOrganizationCode().contains(userDetails.getOrganizationCode())
                ) {
                    draft.setStatus(idAndStatusMap.get(draft.getId()));
                    draft.setApprover(userDetails.getStaffCode());
                    updatedDrafts.add(draft);

                    Organization organization = codeAndOrganizationMap.get(draft.getOrganizationCode());
                    organization.setName(draft.getNewName());
                    organization.setForm(draft.getNewForm());
                    updatedOrganizations.add(organization);

                    response.add("Phe duyet thanh cong yeu cau ma to chuc dang " + draft.getOrganizationCode());
                    continue;
                }

                response.add("Phe duyet that bai yeu cau ma to chuc dang " + draft.getOrganizationCode());
            }

            organizationClient.saveAll(updatedOrganizations);
            historyDraftClient.saveAll(updatedDrafts);

            return response;
        } catch (Exception e) {
            throw new CommonException("Phe duyet that bai");
        }
    }
}
