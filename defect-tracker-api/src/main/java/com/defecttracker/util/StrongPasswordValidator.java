package com.defecttracker.util;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class StrongPasswordValidator implements ConstraintValidator<StrongPassword, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // Optional field: if null or empty, let @NotBlank or callers decide requirement
        if (value == null || value.trim().isEmpty()) {
            return true;
        }
        return PasswordPolicy.isValid(value);
    }
}
