package capstone.bookitty.global.annotation.validator;

import capstone.bookitty.global.annotation.ValidScore;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;
import java.util.List;

public class ScoreValidator implements ConstraintValidator<ValidScore, Double> {

    private final List<Double> validScores = Arrays.asList(0.5, 1.0, 1.5, 2.0, 2.5, 3.0, 3.5, 4.0, 4.5, 5.0);

    @Override
    public boolean isValid(Double value, ConstraintValidatorContext context) {
        return value != null && validScores.contains(value);
    }
}
