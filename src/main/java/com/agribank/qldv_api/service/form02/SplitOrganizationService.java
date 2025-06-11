package com.agribank.qldv_api.service.form02;

import com.agribank.qldv_api.enums.*;
import com.agribank.qldv_api.exception.ExceptionMessage;
import com.agribank.qldv_api.gateway.OrganizationClient;
import com.agribank.qldv_api.gateway.RequestClient;
import com.agribank.qldv_api.gateway.form02.split.OrganizationSplitDetailDraftClient;
import com.agribank.qldv_api.gateway.form02.split.OrganizationSplitDraftClient;
import com.agribank.qldv_api.jwt.UserDetailsImpl;
import com.agribank.qldv_api.request.form02.SplitOrganizationRequest;
import com.agribank.qldv_api.service.RequestService;
import com.agribank.qldv_api.service.handler.EntityHandler;
import com.agribank.qldv_api.utils.CommonUtils;
import com.agribank.qldvutils.entity.BaseFormEntity;
import com.agribank.qldvutils.entity.Organization;
import com.agribank.qldvutils.entity.Request;
import com.agribank.qldvutils.entity.form02.split.OrganizationSplit;
import com.agribank.qldvutils.entity.form02.split.OrganizationSplitDetailDraft;
import com.agribank.qldvutils.entity.form02.split.OrganizationSplitDraft;
import com.agribank.qldvutils.exception.CommonException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.modelmapper.ModelMapper;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SplitOrganizationService implements EntityHandler {
    final ModelMapper modelMapper;
    final ObjectMapper objectMapper;

    final RequestClient requestClient;
    final OrganizationClient organizationClient;
    final OrganizationSplitDraftClient splitDraftClient;
    final OrganizationSplitDetailDraftClient splitDetailDraftClient;

    final RequestService requestService;

    static final EForm form = EForm.BIEU_02_SPLIT;

    public Map<String, String> getCombinedFieldMap() {
        Map<String, String> combinedFieldMap = new HashMap<>();
        combinedFieldMap.putAll(BaseFormEntity.BASE_FIELD_MAP);
        combinedFieldMap.putAll(OrganizationSplit.FIELD_MAP);

        return combinedFieldMap;
    }

    public String createSplitRequest(SplitOrganizationRequest request) {
        OrganizationSplitDraft existSplitDraft = splitDraftClient.findPendingRequest(request.getOldCode()).getData();

        if (Objects.nonNull(existSplitDraft)) {
            throw new CommonException("Yêu cầu chia tách tổ chức đảng đã tồn tại");
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Organization oldOrganization = organizationClient.findByCode(request.getOldCode()).getData();

        if (Objects.isNull(oldOrganization)) {
            throw new CommonException(ExceptionMessage.ORGANIZATION_NOT_FOUND);
        }

        OrganizationSplitDraft splitDraft = modelMapper.map(request, OrganizationSplitDraft.class);
        splitDraft.setOldName(oldOrganization.getName());
        splitDraft.setCreatedBy(userDetails.getId());
        splitDraft = splitDraftClient.save(splitDraft).getData();

        List<OrganizationSplitDetailDraft> detailDraftList = new ArrayList<>();
        for (SplitOrganizationRequest.SplitDetailRequest detailRequest: request.getDetailRequests()) {
            OrganizationSplitDetailDraft detailDraft = modelMapper.map(detailRequest, OrganizationSplitDetailDraft.class);
            detailDraft.setSplitId(splitDraft.getId());
            detailDraftList.add(detailDraft);
        }
        splitDetailDraftClient.saveAll(detailDraftList);

        Request splitRequest = requestService.initializeRequest(splitDraft, null, form, getCombinedFieldMap());
        String jsonData = createJsonData(splitDraft, detailDraftList);
        splitRequest.setNewData(jsonData);
        splitRequest.setOrganizationCode(request.getOldCode());
        splitRequest.setReferenceId(splitDraft.getId());
        splitRequest.setCreatedBy(userDetails.getId());
        requestClient.save(splitRequest);

        return "Tạo mới yêu cầu chia tách tổ chức đảng thành công";
    }

    private String createJsonData(OrganizationSplitDraft splitDraft, List<OrganizationSplitDetailDraft> detailDraftList) {
        try {
            Map<String, Object> draftDataMap = CommonUtils.createFilteredDataMap(splitDraft, getCombinedFieldMap());
            Map<String, String> fieldMap = OrganizationSplitDetailDraft.FIELD_MAP;
            int i = 1;

            for (OrganizationSplitDetailDraft detailDraft : detailDraftList) {
                BeanWrapper wrapper = new BeanWrapperImpl(detailDraft);

                // Iterate over fieldMap keys (entity fields)
                for (String fieldName : fieldMap.keySet()) {
                    if (wrapper.isReadableProperty(fieldName)) {
                        Object value = wrapper.getPropertyValue(fieldName);
                        draftDataMap.put(fieldMap.get(fieldName) + " " + i, value);
                    }
                }
                i++;
            }

            return objectMapper.writeValueAsString(draftDataMap);
        } catch (Exception e) {
            throw new CommonException(e.getMessage());
        }
    }

    private String getParentCode(String code) {
        if (!Constants.ORGANIZATION_NAME_LENGHT.contains(code.length())) {
            throw new CommonException("Mã tổ chức đảng không hợp lệ");
        }

        if (code.length() == Constants.ORGANIZATION_NAME_LENGHT.get(0)) {
            return Constants.DANG_UY_AGRIBANK_CODE;
        }

        return code.substring(0, code.length() - 2);
    }

    @Override
    @Transactional
    public boolean applyCreate(String splitDraftId, UserDetailsImpl userDetails) {
        OrganizationSplitDraft splitDraft = splitDraftClient.findById(splitDraftId).getData().orElse(null);

        if (Objects.isNull(splitDraft)) {
            return false;
        }

        Organization oldOrganization = organizationClient.findByCode(splitDraft.getOldCode()).getData();
        oldOrganization.setStatus(EOrganizationStatus.NO.getStatus());

        List<OrganizationSplitDetailDraft> detailDrafts = splitDetailDraftClient.findBySplitId(splitDraftId).getData();
        List<Organization> newOrganizationList = new ArrayList<>();

        for (OrganizationSplitDetailDraft detailDraft: detailDrafts) {
            String code = detailDraft.getNewCode();

            Organization organization = Organization.builder()
                    .name(detailDraft.getNewName())
                    .form(detailDraft.getForm())
                    .status(EOrganizationStatus.YES.getStatus())
                    .build();

            organization.setCode(code);
            organization.setParentCode(getParentCode(code));

            newOrganizationList.add(organization);
        }

        organizationClient.save(oldOrganization);
        organizationClient.saveAll(newOrganizationList);

        splitDraft.setApprovedBy(userDetails.getId());
        splitDraft.setStatus(EApprovalStatus.APPROVED.getId());
        splitDraftClient.save(splitDraft);

        return true;
    }

    @Override
    public boolean applyUpdate(String referenceId) {
        return false;
    }

    @Override
    public boolean applyDelete(String referenceId) {
        return false;
    }

    @Override
    public void setDenied(String splitDraftId) {
        OrganizationSplitDraft splitDraft = splitDraftClient.findById(splitDraftId).getData().orElse(null);
        if (Objects.nonNull(splitDraft)) {
            splitDraft.setStatus(EApprovalStatus.DENIED.getId());
        }
        splitDraftClient.save(splitDraft);
    }
}
