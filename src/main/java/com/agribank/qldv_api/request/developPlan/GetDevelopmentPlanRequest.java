package com.agribank.qldv_api.request.developPlan;

import com.agribank.qldv_api.exception.ValidationException;
import com.agribank.qldvutils.request.PagingRequest;
import lombok.*;
import lombok.experimental.FieldDefaults;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GetDevelopmentPlanRequest extends PagingRequest {
    String organizationCode;
    String name;
    Integer startYear;
    Integer endYear;

    @Override
    public void validate() {
        super.validate();
        if (startYear > endYear) {
            throw new ValidationException("start can not bigger than end");
        }
        if (startYear < 1945 || endYear > 2145) {
            throw new ValidationException("Not yet info!");
        }
    }
}
