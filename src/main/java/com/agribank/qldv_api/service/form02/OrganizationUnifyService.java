package com.agribank.qldv_api.service.form02;

import com.agribank.qldv_api.enums.EForm;
import com.agribank.qldv_api.jwt.UserDetailsImpl;
import com.agribank.qldv_api.service.RequestService;
import com.agribank.qldv_api.service.handler.EntityHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrganizationUnifyService implements EntityHandler {
    ModelMapper modelMapper;
    ObjectMapper objectMapper;

    RequestService requestService;

    static final EForm form = EForm.BIEU_02_UNION;

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
