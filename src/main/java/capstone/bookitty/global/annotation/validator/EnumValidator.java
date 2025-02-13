package capstone.bookitty.global.annotation.validator;

import capstone.bookitty.global.annotation.ValidEnum;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class EnumValidator implements ConstraintValidator<ValidEnum, Object> {
    private Class<? extends Enum<?>> enumClass;

    @Override
    public void initialize(ValidEnum constraintAnnotation) {
        this.enumClass = constraintAnnotation.enumClass();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        Object[] enumValues = this.enumClass.getEnumConstants();
        for (Object enumValue : enumValues) {
            if (value.equals(enumValue)) {
                return true;
            }
        }
        return false;
    }
}
