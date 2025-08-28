package com.agribank.qldv_api.request.form02;

import com.agribank.qldvutils.entity.DvOrgHistoryDraft;
import com.agribank.qldvutils.entity.form02.merge.OrganizationMergeDetailDraft;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MergeDraftTempRequest {
    @NotNull
    List<OrganizationMergeDetailDraft> mergeDetailDrafts;
    @NotNull
    List<DvOrgHistoryDraft> membersDraft;
}
