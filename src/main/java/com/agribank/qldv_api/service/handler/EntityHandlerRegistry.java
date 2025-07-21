package com.agribank.qldv_api.service.handler;

import com.agribank.qldv_api.enums.EForm;
import com.agribank.qldv_api.service.*;
import com.agribank.qldv_api.service.DVService;
import com.agribank.qldv_api.service.EstablishmentDissolveDraftService;
import com.agribank.qldv_api.service.form02.OrganizationMergeService;
import com.agribank.qldv_api.service.form02.OrganizationUnifyService;
import com.agribank.qldv_api.service.form02.PartyOrganizationTransferService;
import com.agribank.qldv_api.service.organization.OrganizationService;
import com.agribank.qldv_api.service.MembershipProposalService;
import com.agribank.qldv_api.service.form02.OrganizationUpDownService;
import com.agribank.qldv_api.service.form02.SplitOrganizationService;
import com.agribank.qldv_api.service.party_reinstatement.PartyReinstatementService;
import com.agribank.qldv_api.service.party_transfer.TransferOutAgribankService;
import com.agribank.qldv_api.service.party_transfer.TransferTemporaryService;
import com.agribank.qldv_api.service.party_transfer.TransferToAgribankService;
import com.agribank.qldv_api.service.party_transfer.TransferWithinAgribankService;
import com.agribank.qldv_api.service.party_transfer.TransferWithinBaseService;
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
            OrganizationUpDownService historyService,
            EstablishmentDissolveDraftService establishmentDissolveDraftService,
            DVService dvService,
            MembershipProposalService membershipProposalService,
            PartyActivityExemptionService partyActivityExemptionService,
            LeavePartyService leavePartyService,
            RemoveNamePartyService removeNamePartyService,
            DeceasedService deceasedService,
            DevelopPlanDetailService developPlanDetailService,
            PartyReinstatementService partyReinstatementService,
            DVRecognitionService dvRecognitionService,
            SplitOrganizationService splitOrganizationService,
            OrganizationMergeService organizationMergeService,
            TransferToAgribankService transferToAgribankService,
            TransferOutAgribankService transferOutAgribankService,
            TransferWithinAgribankService transferWithinAgribankService,
            TransferWithinBaseService transferWithinBaseService,
            TransferTemporaryService transferTemporaryService,
            OrganizationUnifyService organizationUnifyService,
            PartyOrganizationTransferService partyOrganizationTransferService
    ) {
        this.handlers = Map.ofEntries(
                entry(EForm.BIEU_01.getCode(), organizationService),
                entry(EForm.BIEU_02_UP.getCode(), historyService),
                entry(EForm.BIEU_02_DOWN.getCode(), historyService),
                entry(EForm.BIEU_02_ESTA.getCode(), establishmentDissolveDraftService),
                entry(EForm.BIEU_02_SPLIT.getCode(), splitOrganizationService),
                entry(EForm.BIEU_02_MERGE.getCode(), organizationMergeService),
                entry(EForm.BIEU_02_UNION.getCode(), organizationUnifyService),
                entry(EForm.BIEU_02_TRANSFER.getCode(), partyOrganizationTransferService),
                entry(EForm.BIEU_12.getCode(), developPlanDetailService),
                entry(EForm.BIEU_15.getCode(), dvService),
                entry(EForm.BIEU_20.getCode(), membershipProposalService),
                entry(EForm.BIEU_21.getCode(), dvRecognitionService),
                entry(EForm.BIEU_22.getCode(), partyReinstatementService),
                entry(EForm.BIEU_25_TRANSFER_TO_AGRIBANK.getCode(), transferToAgribankService),
                entry(EForm.BIEU_25_TRANSFER_TEMPORARY.getCode(), transferTemporaryService),
                entry(EForm.BIEU_25_TRANSFER_OUT_AGRIBANK.getCode(), transferOutAgribankService),
                entry(EForm.BIEU_25_TRANSFER_WITHIN_AGRIBANK.getCode(), transferWithinAgribankService),
                entry(EForm.BIEU_25_TRANSFER_WITHIN_BASE.getCode(), transferWithinBaseService),
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
