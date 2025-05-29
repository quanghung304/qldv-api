package com.agribank.qldv_api.service.handler;

import com.agribank.qldv_api.enums.EForm;
import com.agribank.qldv_api.service.DVService;
import com.agribank.qldv_api.service.EstablishmentDissolveDraftService;
import com.agribank.qldv_api.service.OrganizationService;
import com.agribank.qldv_api.service.TransformationHistoryService;
import com.agribank.qldv_api.service.report26.DeceasedService;
import com.agribank.qldv_api.service.report26.LeavePartyService;
import com.agribank.qldv_api.service.report26.PartyActivityExemptionService;
import com.agribank.qldv_api.service.report26.RemoveNamePartyService;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@Component
public class EntityHandlerRegistry {
    private final Map<String, EntityHandler> handlers;

    public EntityHandlerRegistry(
            TransformationHistoryService historyService,
            EstablishmentDissolveDraftService establishmentDissolveDraftService,
            OrganizationService organizationService,
            PartyActivityExemptionService partyActivityExemptionService,
            LeavePartyService leavePartyService,
            RemoveNamePartyService removeNamePartyService,
            DeceasedService deceasedService,
            DVService dvService
    ) {
        this.handlers = Map.of(
                EForm.BIEU_01.getCode(), organizationService,
                EForm.BIEU_02_UP.getCode(), historyService,
                EForm.BIEU_02_DOWN.getCode(), historyService,
                EForm.BIEU_02_ESTA.getCode(), establishmentDissolveDraftService,
                EForm.BIEU_15.getCode(), dvService,
                EForm.BIEU_26_PARTY_ACTIVITY_EXEMPTION.getCode(), partyActivityExemptionService,
                EForm.BIEU_26_LEAVE_PARTY.getCode(), leavePartyService,
                EForm.BIEU_26_REMOVE_NAME_PARTY.getCode(), removeNamePartyService,
                EForm.BIEU_26_DECEASED.getCode(), deceasedService
        );
    }

    public Optional<EntityHandler> getHandler(String formId) {
        return Optional.ofNullable(handlers.get(formId));
    }
}
