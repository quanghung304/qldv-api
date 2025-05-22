package com.agribank.qldv_api.request.validator;

import com.agribank.qldv_api.enums.EOrganizationReference;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class ValidOrganizationFormValidator implements ConstraintValidator<ValidOrganizationForm, String> {

    private static final Set<String> VALID_FORMS = Arrays.stream(EOrganizationReference.values())
            .flatMap(ref -> Arrays.stream(ref.getCode().split(",\\s*")))
            .collect(Collectors.toSet());

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // Use @NotNull for null checks
        }
        return VALID_FORMS.contains(value);
    }
}
