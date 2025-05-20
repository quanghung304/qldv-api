package com.agribank.qldv_api.service.handler;

public interface EntityHandler {
    void applyCreate(String newData);
    void applyUpdate(String newData, String referenceId);
    void applyDelete(String referenceId);
}
