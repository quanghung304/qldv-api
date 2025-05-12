package com.agribank.qldv_api.request.establishmentDissolve;

import com.agribank.qldvutils.request.PagingRequest;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EstablishmentDissolveSearchRequest extends PagingRequest {
    Integer type;
    String status;
}
