package capstone.bookitty.domain.dto.commentDto;

import capstone.bookitty.domain.entity.Comment;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

public record CommentInfoResponse(
        Long id,
        Long memberId,
        String isbn,
        String content,
        int like_count,
        @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime createdAt,
        @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime modifiedAt,
        String memberName,
        String memberProfileImg
) {
    public static CommentInfoResponse of(Comment comment, int like_count,
                                                    String memberName, String memberProfileImg){
        return new CommentInfoResponse(comment.getId(), comment.getMember().getId(),comment.getIsbn(),
                comment.getContent(), like_count ,comment.getCreatedAt(),
                comment.getModifiedAt(),memberName,memberProfileImg);
    }
}