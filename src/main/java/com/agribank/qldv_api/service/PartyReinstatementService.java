package com.agribank.qldv_api.service;

import com.agribank.qldv_api.gateway.*;
import com.agribank.qldv_api.jwt.UserDetailsImpl;
import com.agribank.qldv_api.service.handler.EntityHandler;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PartyReinstatementService implements EntityHandler {
    private final RequestClient requestClient;
    private final CheckAuthorityService checkAuthorityService;
    private final PartyReinstatementDraftService partyReinstatementDraftService;
    private final RequestService requestService;
    private final ModelMapper modelMapper;


    @Override
    public boolean applyCreate(String referenceId, UserDetailsImpl userDetails) {
        return false;
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
    public void setDenied(String referenceId) {

    }
}
