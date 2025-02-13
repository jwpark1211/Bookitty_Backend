package capstone.bookitty.domain.star.dto;

import capstone.bookitty.global.annotation.ValidScore;
import jakarta.validation.constraints.NotNull;

public record StarUpdateRequest(
        @NotNull(message = "score is a required entry value.")
        @ValidScore
        double score
) {
    public static StarUpdateRequest of(double score){
        return new StarUpdateRequest(score);
    }
}