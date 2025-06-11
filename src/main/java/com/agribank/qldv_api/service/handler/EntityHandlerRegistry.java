package com.agribank.qldv_api.service.handler;

import com.agribank.qldv_api.enums.EForm;
import com.agribank.qldv_api.service.*;
import com.agribank.qldv_api.service.DVService;
import com.agribank.qldv_api.service.EstablishmentDissolveDraftService;
import com.agribank.qldv_api.service.organization.OrganizationService;
import com.agribank.qldv_api.service.MembershipProposalService;
import com.agribank.qldv_api.service.TransformationHistoryService;
import com.agribank.qldv_api.service.party_reinstatement.PartyReinstatementService;
import com.agribank.qldv_api.service.report26.DeceasedService;
import com.agribank.qldv_api.service.report26.LeavePartyService;
import com.agribank.qldv_api.service.report26.PartyActivityExemptionService;
import com.agribank.qldv_api.service.report26.RemoveNamePartyService;
import com.agribank.qldv_api.service.development_plan.DevelopPlanDetailService;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

import static java.util.Map.entry;

@Component
public class EntityHandlerRegistry {
    private final Map<String, EntityHandler> handlers;

    public EntityHandlerRegistry(
            OrganizationService organizationService,
            TransformationHistoryService historyService,
            EstablishmentDissolveDraftService establishmentDissolveDraftService,
            DVService dvService,
            MembershipProposalService membershipProposalService,
            PartyActivityExemptionService partyActivityExemptionService,
            LeavePartyService leavePartyService,
            RemoveNamePartyService removeNamePartyService,
            DeceasedService deceasedService,
            DevelopPlanDetailService developPlanDetailService,
            PartyReinstatementService partyReinstatementService,
            DVRecognitionService dvRecognitionService

    ) {
        this.handlers = Map.ofEntries(
                entry(EForm.BIEU_01.getCode(), organizationService),
                entry(EForm.BIEU_02_UP.getCode(), historyService),
                entry(EForm.BIEU_02_DOWN.getCode(), historyService),
                entry(EForm.BIEU_02_ESTA.getCode(), establishmentDissolveDraftService),
                entry(EForm.BIEU_12.getCode(), developPlanDetailService),
                entry(EForm.BIEU_15.getCode(), dvService),
                entry(EForm.BIEU_20.getCode(), membershipProposalService),
                entry(EForm.BIEU_21.getCode(), dvRecognitionService),
                entry(EForm.BIEU_22.getCode(), partyReinstatementService),
                entry(EForm.BIEU_26_PARTY_ACTIVITY_EXEMPTION.getCode(), partyActivityExemptionService),
                entry(EForm.BIEU_26_LEAVE_PARTY.getCode(), leavePartyService),
                entry(EForm.BIEU_26_REMOVE_NAME_PARTY.getCode(), removeNamePartyService),
                entry(EForm.BIEU_26_DECEASED.getCode(), deceasedService)
        );
    }

    public Optional<EntityHandler> getHandler(String formId) {
        return Optional.ofNullable(handlers.get(formId));
    }
}
