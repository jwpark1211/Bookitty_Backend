package capstone.bookitty.domain.dto.starDto;

import capstone.bookitty.domain.entity.Star;
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
    public static StarInfoResponse of(Long id, Long memberId, String isbn, double score,
                                      LocalDateTime createdAt, LocalDateTime modifiedAt){
        return new StarInfoResponse(id, memberId, isbn, score, createdAt, modifiedAt);
    }

    public static StarInfoResponse from(Star star){
        return new StarInfoResponse(
                star.getId(),
                star.getMember().getId(),
                star.getIsbn(),
                star.getScore(),
                star.getCreatedAt(),
                star.getModifiedAt()
        );
    }
}
