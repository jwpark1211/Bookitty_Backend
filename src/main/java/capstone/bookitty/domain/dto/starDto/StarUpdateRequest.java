package capstone.bookitty.domain.dto.starDto;

import capstone.bookitty.domain.annotation.ValidScore;
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