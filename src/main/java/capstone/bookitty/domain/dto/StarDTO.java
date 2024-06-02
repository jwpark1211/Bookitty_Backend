package capstone.bookitty.domain.dto;

import capstone.bookitty.domain.annotation.ValidScore;
import capstone.bookitty.domain.entity.Star;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

public class StarDTO {
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class StarSaveRequest{
        @NotBlank(message = "Isbn is a required entry value.")
        private String isbn;
        @NotNull(message = "memberId is a required entry value.")
        private Long memberId;
        @ValidScore
        private double score;

        public static StarSaveRequest buildForTest(String isbn, Long memberId, double score){
            return new StarSaveRequest(isbn, memberId,score);
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class StarUpdateRequest{
        @NotNull(message = "score is a required entry value.")
        @ValidScore
        private double score;

        public static StarUpdateRequest buildForTest(double score){
            return new StarUpdateRequest(score);
        }
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class IdResponse{
        private Long id;
        public static IdResponse of(Star star){
            return new IdResponse(star.getId());
        }
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class StarInfoResponse{
        private Long id;
        private Long memberId;
        private String isbn;
        private double score;
        @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime createdAt;
        @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime modifiedAt;

        public static StarInfoResponse of(Star star){
            return new StarInfoResponse(star.getId(), star.getMember().getId(),star.getIsbn(),
                    star.getScore(),star.getCreatedAt(),star.getModifiedAt());
        }
    }
}
