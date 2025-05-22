package com.agribank.qldv_api.service.handler;

import com.agribank.qldv_api.jwt.UserDetailsImpl;

public interface EntityHandler {
    boolean applyCreate(String referenceId, UserDetailsImpl userDetails);
    boolean applyUpdate(String referenceId);
    boolean applyDelete(String referenceId);
    void setDenied(String referenceId);
    Object getRequestDetail(String referenceId);
}
