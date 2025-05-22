package com.agribank.qldv_api.request.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = ValidOrganizationFormValidator.class)
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidOrganizationForm {
    String message() default "Invalid organization form. Must be one of: A, B1, B2, B3, C1, C2, D";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
