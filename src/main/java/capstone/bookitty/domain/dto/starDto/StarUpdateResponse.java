package capstone.bookitty.domain.dto.starDto;

import capstone.bookitty.domain.entity.Star;

public record StarUpdateResponse(
    Long id,
    double score
) {
    public static StarUpdateResponse of(Long id, double score){
        return new StarUpdateResponse(id, score);
    }

    public static StarUpdateResponse from(Star star){
        return new StarUpdateResponse(star.getId(),star.getScore());
    }
}
