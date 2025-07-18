package capstone.bookitty.domain.star.api.dto;

import capstone.bookitty.domain.star.domain.Star;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

public record StarInfoResponse(
        Long id,
        Long memberId,
        String isbn,
        double score,
        @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime createdAt,
        @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime modifiedAt
) {

    public static StarInfoResponse from(Star star) {
        return new StarInfoResponse(
                star.getId(),
                star.getMemberId(),
                star.getIsbn(),
                star.getScore(),
                star.getCreatedAt(),
                star.getModifiedAt()
        );
    }
}
