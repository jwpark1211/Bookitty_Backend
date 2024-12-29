package capstone.bookitty.domain.dto.commentDto;

import capstone.bookitty.domain.entity.Comment;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CommentSaveRequest(
    @NotBlank(message = "Isbn is a required entry value.")
    String isbn,
    @NotNull(message = "memberId is a required entry value.")
    Long memberId,
    @NotEmpty(message = "content is a required entry value.")
    @Size(min = 1, max = 100, message = "Name must be between 1 and 100 characters")
    String content
){
    public static CommentSaveRequest of(String isbn, Long memberId, String content){
        return new CommentSaveRequest(isbn, memberId, content);
    }
}
