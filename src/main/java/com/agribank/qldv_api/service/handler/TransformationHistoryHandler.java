package com.agribank.qldv_api.service.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TransformationHistoryHandler implements EntityHandler {
    @Override
    public void applyCreate(String newData) {

    }

    @Override
    public void applyUpdate(String newData, String referenceId) {

    }

    @Override
    public void applyDelete(String referenceId) {

    }
}
