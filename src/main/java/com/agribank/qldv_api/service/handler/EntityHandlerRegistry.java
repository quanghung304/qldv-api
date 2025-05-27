package com.agribank.qldv_api.service.handler;

import com.agribank.qldv_api.enums.EForm;
import com.agribank.qldv_api.service.EstablishmentDissolveDraftService;
import com.agribank.qldv_api.service.OrganizationService;
import com.agribank.qldv_api.service.TransformationHistoryService;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@Component
public class EntityHandlerRegistry {
    private final Map<String, EntityHandler> handlers;

    public EntityHandlerRegistry(
            TransformationHistoryService historyService,
            EstablishmentDissolveDraftService establishmentDissolveDraftService,
            OrganizationService organizationService
    ) {
        this.handlers = Map.of(
                EForm.BIEU_02_HIST.getCode(), historyService,
                EForm.BIEU_02_ESTA.getCode(), establishmentDissolveDraftService,
                EForm.BIEU_01.getCode(), organizationService
        );
    }

    public Optional<EntityHandler> getHandler(String formId) {
        return Optional.ofNullable(handlers.get(formId));
    }
}
