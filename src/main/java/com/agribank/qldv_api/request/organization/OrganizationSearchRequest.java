package com.agribank.qldv_api.request.organization;

import com.agribank.qldvutils.request.PagingRequest;
import lombok.*;
import lombok.experimental.FieldDefaults;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrganizationSearchRequest extends PagingRequest {
    String name;
    String status;
    String code;

    @Override
    public void validate() {
        super.validate();
    }
}
