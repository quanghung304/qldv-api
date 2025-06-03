package com.agribank.qldv_api.request.develop_plan;

import com.agribank.qldv_api.exception.ValidationException;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DevelopPlanDetailRequest {
    Integer year;
    Integer target;
    Integer min;

    public void validate(){
        if (min > target) {
            throw new ValidationException("min can not bigger than target");
        }
        if (min < 0) {
            throw new ValidationException("min can not smaller than 0");
        }
    }
}
