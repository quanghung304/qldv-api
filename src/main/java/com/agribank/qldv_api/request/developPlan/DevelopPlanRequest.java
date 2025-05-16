package com.agribank.qldv_api.request.developPlan;

import com.agribank.qldv_api.exception.ValidationException;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DevelopPlanRequest {
    String id;
    String organizationCode;
    String name;
    Integer start;
    Integer end;
    Integer target;
    Integer hasChild;

    public void validate(){
        if (start > end) {
            throw new ValidationException("start can not bigger than end");
        }
        if (start < 1945 || end > 2145) {
            throw new ValidationException("Not yet info!");
        }
    }
}
