// LocaleConstraint.java
package ika.validators;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = LocaleValidator.class)
@Target({ ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface LocaleConstraint {
    String message() default "Invalid locale";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
