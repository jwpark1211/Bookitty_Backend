package capstone.bookitty.domain.comment.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public record CommentUpdateRequest(
    @NotEmpty(message = "content is a required entry value.")
    @Size(min = 1, max = 100, message = "Name must be between 1 and 100 characters")
    String content
) {
    public static CommentUpdateRequest of(String content){
        return new CommentUpdateRequest(content);
    }
}