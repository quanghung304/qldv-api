package com.agribank.qldv_api.service;

import com.agribank.qldv_api.gateway.form02.dissolve.DissolveDisbandClient;
import com.agribank.qldv_api.jwt.UserDetailsImpl;
import com.agribank.qldv_api.response.form02.DissolveDisbandResponse;
import com.agribank.qldv_api.service.organization.OrganizationService;
import com.agribank.qldvutils.entity.form02.dissolve.DissolveDisband;
import com.agribank.qldvutils.request.form02.dissolve.DissolveDisbandSearchRequest;
import com.agribank.qldvutils.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

import static com.agribank.qldv_api.enums.Constants.BRANCH_CODE_HEAD_QUARTER;


@Service
@RequiredArgsConstructor
public class DissolveDisbandService {
    private final DissolveDisbandClient client;
    private final ModelMapper modelMapper;
    private final UserService userService;
    private final OrganizationService organizationService;;

    public void save(DissolveDisband establishmentDissolve) {
        client.save(establishmentDissolve);
    }

    public void saveAll(List<DissolveDisband> establishmentDissolves) {
        client.saveAll(establishmentDissolves);
    }

    public PageResponse<DissolveDisbandResponse> search(DissolveDisbandSearchRequest request) {
        PageResponse<DissolveDisbandResponse> response = new PageResponse<>();
        UserDetailsImpl userRequested = userService.getUserRequested();
        List<String> codeChild = organizationService.getChildCode(userRequested.getOrganizationCode());
        if (Objects.nonNull(request.getCode()) && !codeChild.contains(request.getCode()) && BRANCH_CODE_HEAD_QUARTER < userRequested.getBrcd()){
            return response;
        }

        if (BRANCH_CODE_HEAD_QUARTER < userRequested.getBrcd() && Objects.isNull(request.getCode())){
            request.setCode(userRequested.getOrganizationCode());
        }

        PageResponse<DissolveDisband> draftPageResponse = client.search(request).getData();
        if (Objects.isNull(draftPageResponse)) {
            return response;
        }

        response.setTotalPages(draftPageResponse.getTotalPages());
        response.setCurrentPage(draftPageResponse.getCurrentPage());
        response.setTotalItems(draftPageResponse.getTotalItems());

        if (Objects.nonNull(draftPageResponse.getData())) {
            response.setData(draftPageResponse.getData().stream()
                    .map(establishment -> modelMapper.map(establishment, DissolveDisbandResponse.class)
                    ).toList()
            );
        }

        return response;
    }

    public DissolveDisbandResponse get(String id){
        DissolveDisband establishmentDissolve = findById(id);
        if (Objects.isNull(establishmentDissolve)) {
            return null;
        }
        return modelMapper.map(establishmentDissolve, DissolveDisbandResponse.class);
    }

    public DissolveDisband findById(String id){
        return client.findById(id).getData().orElse(null);
    }
}
