package capstone.bookitty.domain.bookState.dto;

import capstone.bookitty.global.annotation.ValidEnum;
import capstone.bookitty.domain.bookState.domain.State;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record StateSaveRequest(
        @NotBlank(message = "ISBN is a requred entry value.")
        String isbn,
        @NotNull(message = "memberId is a required entry value.")
        Long memberId,
        @ValidEnum(enumClass = State.class, message = "State is not valid.")
        State state,
        String categoryName,
        String bookTitle,
        String bookAuthor,
        String bookImgUrl
) {
        public static StateSaveRequest of(String isbn, Long memberId,State state,String categoryName,
                                          String bookTitle, String bookAuthor, String bookImgUrl){
                return new StateSaveRequest(isbn, memberId, state, categoryName, bookTitle, bookAuthor,bookImgUrl);
        }
}
