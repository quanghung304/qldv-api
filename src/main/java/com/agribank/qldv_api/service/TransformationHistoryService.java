package com.agribank.qldv_api.service;

import com.agribank.qldv_api.enums.Constants;
import com.agribank.qldv_api.enums.EApprovalStatus;
import com.agribank.qldv_api.gateway.OrganizationClient;
import com.agribank.qldv_api.gateway.TransformationHistoryDraftClient;
import com.agribank.qldv_api.jwt.UserDetailsImpl;
import com.agribank.qldv_api.request.ApproveRequest;
import com.agribank.qldv_api.request.organizationTransform.OrganizationTransformRequest;
import com.agribank.qldvutils.entity.Organization;
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
    private final CheckAuthorityService checkAuthorityService;
    private final ModelMapper modelMapper;

    public TransformationHistoryDraft createTransformRequest(OrganizationTransformRequest request) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        checkAuthorityService.hasAuthorityOverOrganization(request.getOrganizationCode());
        TransformationHistoryDraft draft = modelMapper.map(request, TransformationHistoryDraft.class);
        draft.setSubmitter(userDetails.getStaffCode());
        draft.setStatus(EApprovalStatus.PENDING.getId());

        return historyDraftClient.save(draft).getData();
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
