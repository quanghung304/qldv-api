package com.agribank.qldv_api.service;

import com.agribank.qldv_api.gateway.OrganizationClient;
import com.agribank.qldv_api.jwt.UserDetailsImpl;
import com.agribank.qldv_api.request.organization.OrganizationSearchRequest;
import com.agribank.qldv_api.response.DefaultResponse;
import com.agribank.qldv_api.response.organization.OrganizationResponse;
import com.agribank.qldvutils.entity.Organization;
import com.agribank.qldvutils.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class OrganizationService {
    private final OrganizationClient client;
    private final ModelMapper modelMapper;
    private final UserService userService;
    private final Integer BRANCH_CODE_HEAD_QUARTER = 1002;

    public Organization getByParentCodeMax(String parentCode){
        return  client.getByParentCodeMax(parentCode).getData();
    }

    public Organization findByCode(String organizationId) {
        DefaultResponse<Organization> organizationDefaultResponse = client.findByCode(organizationId);
        return organizationDefaultResponse.getData();
    }

    public OrganizationResponse save(Organization organization) {
        client.save(organization);
        return modelMapper.map(organization, OrganizationResponse.class);
    }

    public void saveAll(List<Organization> organizations) {
        client.saveAll(organizations);
    }

    public PageResponse<OrganizationResponse> search(OrganizationSearchRequest request){
        PageResponse<OrganizationResponse> response = new PageResponse<>();
        UserDetailsImpl userRequested = userService.getUserRequested();

        List<String> codeChild = getChildCode(userRequested.getOrganizationCode());
        if (Objects.nonNull(request.getCode()) && !codeChild.contains(request.getCode())
                && BRANCH_CODE_HEAD_QUARTER < Integer.parseInt(userRequested.getOrganizationCode())){
            return response;
        }

        if (BRANCH_CODE_HEAD_QUARTER < userRequested.getBrcd() && Objects.isNull(request.getCode())){
            request.setCode(userRequested.getOrganizationCode());
        }

        request.setOrderBy("code");

        PageResponse<Organization> organizationPageResponse = client.search(request).getData();
        if (Objects.isNull(organizationPageResponse)) {
            return response;
        }

        response.setTotalPages(organizationPageResponse.getTotalPages());
        response.setCurrentPage(organizationPageResponse.getCurrentPage());
        response.setTotalItems(organizationPageResponse.getTotalItems());

        if (Objects.nonNull(organizationPageResponse.getData())) {
            response.setData(organizationPageResponse.getData().stream()
                    .map(organization -> modelMapper.map(organization, OrganizationResponse.class)
                    ).toList()
            );
        }

        return response;
    }

    public OrganizationResponse get(String code){
        Organization organization = findByCode(code);
        return modelMapper.map(organization, OrganizationResponse.class);
    }

    public List<String> getChildCode(String code){
        List<Organization> organizations = client.findByParent(code).getData();
        List<String> codeChild = new ArrayList<>();
        if (!organizations.isEmpty()){
            codeChild = organizations.stream().map(Organization::getCode).toList();
        }

        return codeChild;
    }

    public List<OrganizationResponse> findByParentCode(String code){
        List<Organization> organizations = client.findByParentCode(code).getData();
        if (Objects.isNull(organizations) || organizations.isEmpty()){
            return new ArrayList<>();
        }
        return organizations.stream().map(organization -> modelMapper.map(organization, OrganizationResponse.class)).toList();
    }

    public OrganizationResponse findByUserId(String userId) {
        Organization organization = client.findByUserId(userId).getData();
        if (Objects.isNull(organization)){
            return null;
        }
        return modelMapper.map(organization, OrganizationResponse.class);
    }
}
