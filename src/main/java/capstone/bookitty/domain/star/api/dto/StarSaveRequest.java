package capstone.bookitty.domain.star.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record StarSaveRequest(
        @NotBlank(message = "Isbn is a required entry value")
        String isbn,

        @NotNull(message = "Member Id is a required entry value")
        Long memberId,

        @NotNull(message = "Score is a required entry value")
        double score
) {
}
