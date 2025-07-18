package capstone.bookitty.domain.star.api.dto;

import capstone.bookitty.domain.star.domain.Star;

public record StarInfoResponse(
        Long id,
        Long memberId,
        String isbn,
        double score
) {

    public static StarInfoResponse from(Star star) {
        return new StarInfoResponse(
                star.getId(),
                star.getMemberId(),
                star.getIsbn(),
                star.getScore()
        );
    }
}
