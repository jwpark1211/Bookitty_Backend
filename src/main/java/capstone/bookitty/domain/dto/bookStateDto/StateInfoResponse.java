package capstone.bookitty.domain.dto.bookStateDto;

import capstone.bookitty.domain.entity.BookState;
import capstone.bookitty.domain.entity.State;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

public record StateInfoResponse(
        Long id,
        Long memberId,
        String isbn,
        State state,
        String categoryName,
        String bookTitle,
        String bookAuthor,
        String bookImgUrl,
        @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime readAt
) {
    public static StateInfoResponse of(Long id, Long memberId, String isbn, State state,
                                       String categoryName, String bookTitle, String bookAuthor,
                                       String bookImgUrl, LocalDateTime readAt){
        return new StateInfoResponse(id, memberId, isbn, state, categoryName,
                bookTitle, bookAuthor, bookImgUrl, readAt);
    }

    public static StateInfoResponse from(BookState state){
        return new StateInfoResponse(state.getId(), state.getMember().getId(), state.getIsbn(),
                state.getState(),state.getCategoryName(),state.getBookTitle(),state.getBookAuthor(),
                state.getBookImgUrl(),state.getReadAt());
    }
}
