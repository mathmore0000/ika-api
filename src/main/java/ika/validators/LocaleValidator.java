// LocaleValidator.java
package ika.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;
import java.util.List;

public class LocaleValidator implements ConstraintValidator<LocaleConstraint, String> {

    private final List<String> supportedLocales = Arrays.asList("en", "pt", "es");

    @Override
    public boolean isValid(String locale, ConstraintValidatorContext context) {
        return supportedLocales.contains(locale);
    }
}
