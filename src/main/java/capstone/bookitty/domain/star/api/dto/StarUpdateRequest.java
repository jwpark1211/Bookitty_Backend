package capstone.bookitty.domain.star.api.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record StarUpdateRequest(
        @NotNull(message = "Score is a required entry value.")
        double score
) {
}