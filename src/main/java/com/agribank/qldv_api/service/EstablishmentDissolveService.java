package com.agribank.qldv_api.service;

import com.agribank.qldv_api.gateway.EstablishmentDissolveClient;
import com.agribank.qldv_api.jwt.UserDetailsImpl;
import com.agribank.qldv_api.request.establishment_dissolve.EstablishmentDissolveSearchRequest;
import com.agribank.qldv_api.response.establishment_dissolve.EstablishmentDissolveResponse;
import com.agribank.qldvutils.entity.EstablishmentDissolve;
import com.agribank.qldvutils.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

import static com.agribank.qldv_api.enums.Constants.BRANCH_CODE_HEAD_QUARTER;


@Service
@RequiredArgsConstructor
public class EstablishmentDissolveService {

    private final EstablishmentDissolveClient client;
    private final ModelMapper modelMapper;
    private final UserService userService;
    private final OrganizationService organizationService;

    public void save(EstablishmentDissolve establishmentDissolve) {
        client.save(establishmentDissolve);
    }

    public void saveAll(List<EstablishmentDissolve> establishmentDissolves) {
        client.saveAll(establishmentDissolves);
    }

    public EstablishmentDissolve findByCode(String code) {
        return client.findById(code).getData();
    }

    public PageResponse<EstablishmentDissolveResponse> search(EstablishmentDissolveSearchRequest request) {
        PageResponse<EstablishmentDissolveResponse> response = new PageResponse<>();
        UserDetailsImpl userRequested = userService.getUserRequested();
        List<String> codeChild = organizationService.getChildCode(userRequested.getOrganizationCode());
        if (Objects.nonNull(request.getCode()) && !codeChild.contains(request.getCode()) && BRANCH_CODE_HEAD_QUARTER < userRequested.getBrcd()){
            return response;
        }

        if (BRANCH_CODE_HEAD_QUARTER < userRequested.getBrcd() && Objects.isNull(request.getCode())){
            request.setCode(userRequested.getOrganizationCode());
        }

        PageResponse<EstablishmentDissolve> draftPageResponse = client.search(request).getData();
        if (Objects.isNull(draftPageResponse)) {
            return response;
        }

        response.setTotalPages(draftPageResponse.getTotalPages());
        response.setCurrentPage(draftPageResponse.getCurrentPage());
        response.setTotalItems(draftPageResponse.getTotalItems());

        if (Objects.nonNull(draftPageResponse.getData())) {
            response.setData(draftPageResponse.getData().stream()
                    .map(establishment -> modelMapper.map(establishment, EstablishmentDissolveResponse.class)
                    ).toList()
            );
        }

        return response;
    }

    public EstablishmentDissolveResponse get(String code){
        return modelMapper.map(findByCode(code), EstablishmentDissolveResponse.class);
    }
}
