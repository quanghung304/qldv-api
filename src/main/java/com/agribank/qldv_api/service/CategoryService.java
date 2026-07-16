package com.agribank.qldv_api.service;

import com.agribank.qldv_api.gateway.CaseTypeClient;
import com.agribank.qldv_api.gateway.DocumentTypeClient;
import com.agribank.qldv_api.gateway.OrganizationTypeClient;
import com.agribank.qldv_api.gateway.StatusClient;
import com.agribank.qldv_api.response.category.CaseTypeResponse;
import com.agribank.qldv_api.response.category.DocumentTypeResponse;
import com.agribank.qldv_api.response.category.OrganizationTypeResponse;
import com.agribank.qldv_api.response.category.StatusResponse;
import com.agribank.qldvutils.enums.EStatusType;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final OrganizationTypeClient organizationTypeClient;
    private final CaseTypeClient caseTypeClient;
    private final StatusClient statusClient;
    private final DocumentTypeClient documentTypeClient;
    private final ModelMapper modelMapper;

    public List<OrganizationTypeResponse> getOrganizationTypes() {
        List<com.agribank.qldvutils.entity.OrganizationType> items = organizationTypeClient.findAll().getData();
        if (items == null || items.isEmpty()) {
            return Collections.emptyList();
        }
        return items.stream().map(e -> modelMapper.map(e, OrganizationTypeResponse.class)).toList();
    }

    public List<CaseTypeResponse> getCaseTypes() {
        List<com.agribank.qldvutils.entity.CaseType> items = caseTypeClient.findAll().getData();
        if (items == null || items.isEmpty()) {
            return Collections.emptyList();
        }
        return items.stream().map(e -> modelMapper.map(e, CaseTypeResponse.class)).toList();
    }

    public List<StatusResponse> getStatuses() {
        List<com.agribank.qldvutils.entity.Status> items = statusClient.findAll().getData();
        if (items == null || items.isEmpty()) {
            return Collections.emptyList();
        }
        return items.stream().map(e -> {
            StatusResponse response = modelMapper.map(e, StatusResponse.class);
            response.setStatusTypeLabel(EStatusType.getLabel(e.getStatusType()));
            return response;
        }).toList();
    }

    public List<DocumentTypeResponse> getDocumentTypes() {
        List<com.agribank.qldvutils.entity.DocumentType> items = documentTypeClient.findAll().getData();
        if (items == null || items.isEmpty()) {
            return Collections.emptyList();
        }
        return items.stream().map(e -> modelMapper.map(e, DocumentTypeResponse.class)).toList();
    }
}
