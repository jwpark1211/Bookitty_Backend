package capstone.bookitty.domain.star.dto;

import capstone.bookitty.domain.star.annotation.ValidScore;
import capstone.bookitty.domain.star.domain.Star;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record StarSaveRequest(
        @NotBlank(message = "Isbn is a required entry value.")
        String isbn,
        @NotNull(message = "memberId is a required entry value.")
        Long memberId,
        @ValidScore
        double score
) {
    public static StarSaveRequest of(String isbn, Long memberId, double score) {
        return new StarSaveRequest(isbn, memberId, score);
    }

    public static StarSaveRequest from(Star star) {
        return new StarSaveRequest(
                star.getIsbn(),
                star.getMember().getId(),
                star.getScore()
        );
    }
}

