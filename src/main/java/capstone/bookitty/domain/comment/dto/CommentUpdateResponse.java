package capstone.bookitty.domain.comment.dto;

import java.time.LocalDateTime;

public record CommentUpdateResponse(
    Long id,
    String content,
    LocalDateTime modifiedAt
){
    public static CommentUpdateResponse of(Long id, String content, LocalDateTime modifiedAt){
        return new CommentUpdateResponse(id, content, modifiedAt);
    }
}