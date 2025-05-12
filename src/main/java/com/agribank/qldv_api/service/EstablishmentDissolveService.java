package com.agribank.qldv_api.service;

import com.agribank.qldv_api.enums.EApprovalStatus;
import com.agribank.qldv_api.enums.EReport01Type;
import com.agribank.qldv_api.gateway.EstablishmentDissolveClient;
import com.agribank.qldv_api.request.establishmentDissolve.EstablishmentDissolveRequest;
import com.agribank.qldv_api.response.establishmentDissolve.EstablishmentDissolveResponse;
import com.agribank.qldvutils.entity.EstablishmentDissolve;
import com.agribank.qldvutils.exception.CommonException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EstablishmentDissolveService {

    private final EstablishmentDissolveClient client;
    private final ModelMapper modelMapper;

    public EstablishmentDissolveResponse createOrUpdate(EstablishmentDissolveRequest request) {
        EstablishmentDissolve establishmentDissolve = null;
        if (Objects.nonNull(request.getId())) {
            establishmentDissolve = client.findById(request.getId()).getData();
        }

        if (Objects.isNull(establishmentDissolve)){
            establishmentDissolve = new EstablishmentDissolve();
            establishmentDissolve.setId(UUID.randomUUID().toString());
        }

        if (EReport01Type.getValue(request.getType()) == -1){
            throw new CommonException("Kiểm tra lại type");
        }

        establishmentDissolve.setCode(request.getCode());
        establishmentDissolve.setName(request.getName());
        establishmentDissolve.setForm(request.getForm());
        establishmentDissolve.setType(request.getType());
        establishmentDissolve.setResolutionNumber(request.getResolutionNumber());
        establishmentDissolve.setResolutionDate(request.getResolutionDate());
        establishmentDissolve.setEstablishmentDecisionNumber(request.getEstablishmentDecisionNumber());
        establishmentDissolve.setDecisionDate(request.getDecisionDate());
        establishmentDissolve.setEffectiveDate(request.getEffectiveDate());
        establishmentDissolve.setStatus(String.valueOf(EApprovalStatus.PENDING.getId()));

        client.save(establishmentDissolve);
        return modelMapper.map(establishmentDissolve, EstablishmentDissolveResponse.class);
    }

//    public PageResponse<EstablishmentDissolveResponse> search(EstablishmentDissolveRequest request) {
//
//    }
}
