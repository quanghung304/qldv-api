package com.agribank.qldv_api.service;

import com.agribank.qldv_api.enums.EOrganizationReference;
import com.agribank.qldv_api.gateway.OrganizationReferenceClient;
import com.agribank.qldv_api.jwt.UserDetailsImpl;
import com.agribank.qldv_api.response.organizationReference.OrganizationReferenceResponse;
import com.agribank.qldvutils.entity.OrganizationReference;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrganizationReferenceService {
    private final OrganizationReferenceClient client;
    private final ModelMapper modelMapper;

    public List<OrganizationReferenceResponse> getAll(){
        List<OrganizationReference> organizationReferences = client.getListChild(EOrganizationReference.GROUP_A.getCode()).getData();
        if(organizationReferences.isEmpty()){
            return null;
        }

        return organizationReferences.stream()
                .map(organizationReference -> modelMapper.map(organizationReference, OrganizationReferenceResponse.class))
                .toList();
    }

    public List<OrganizationReferenceResponse> getChild(){
        UserDetailsImpl userRequested = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<OrganizationReference> organizationReferences = client.getListChild(userRequested.getFormOrganization()).getData();
        if(organizationReferences.isEmpty()){
            return null;
        }

        return organizationReferences.stream()
                .map(organizationReference -> modelMapper.map(organizationReference, OrganizationReferenceResponse.class))
                .toList();
    }

    public OrganizationReference findById(String code){
        return client.findById(code).getData();
    }
}
