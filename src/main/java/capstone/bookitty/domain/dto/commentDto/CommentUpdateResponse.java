package capstone.bookitty.domain.dto.commentDto;

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