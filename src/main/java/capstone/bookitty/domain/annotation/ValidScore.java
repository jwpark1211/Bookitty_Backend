package capstone.bookitty.domain.annotation;

import capstone.bookitty.domain.annotation.validator.ScoreValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = ScoreValidator.class)
@Target({ ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidScore {
    String message() default "Invalid score";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}