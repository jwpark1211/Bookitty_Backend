package capstone.bookitty.domain.dto;

import capstone.bookitty.domain.entity.Comment;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

public class CommentDTO {

    @Data
    public static class CommentSaveRequest{
        @NotBlank(message = "Isbn is a required entry value.")
        private String isbn;
        @NotNull(message = "memberId is a required entry value.")
        private Long memberId;
        @NotEmpty(message = "content is a required entry value.")
        @Size(min = 1, max = 100, message = "Name must be between 1 and 100 characters")
        private String content;
    }

    @Data
    public static class CommentUpdateRequest{
        @NotEmpty(message = "content is a required entry value.")
        @Size(min = 1, max = 100, message = "Name must be between 1 and 100 characters")
        private String content;
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class IdResponse{
        private Long id;
        public static IdResponse of(Comment comment){
            return new IdResponse(comment.getId());
        }
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CommentInfoResponse{
        private Long id;
        private Long memberId;
        private String isbn;
        private String content;
        private int like_count;
        @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime createdAt;
        @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime modifiedAt;
        private String memberName;
        private String memberProfileImg;

        public static CommentInfoResponse of(Comment comment, int like_count,
                                             String memberName, String memberProfileImg){
            return new CommentInfoResponse(comment.getId(), comment.getMember().getId(),comment.getIsbn(),
                    comment.getContent(), like_count ,comment.getCreatedAt(),
                    comment.getModifiedAt(),memberName,memberProfileImg);
        }

    }
}
