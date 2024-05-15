package capstone.bookitty.domain.dto;

import capstone.bookitty.domain.annotation.ValidEnum;
import capstone.bookitty.domain.entity.BookState;
import capstone.bookitty.domain.entity.State;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

public class BookStateDTO {

    @Data
    public static class SaveRequest {
        @NotBlank(message = "ISBN is a requred entry value.")
        private String isbn;
        @NotNull(message = "memberId is a required entry value.")
        private Long memberId;
        @ValidEnum(enumClass = State.class, message = "State is not valid.")
        private State state;
        private String categoryName;
        private String bookTitle;
        private String bookAuthor;
        private String bookImgUrl;
    }

    @Data
    public static class UpdateRequest{
        @ValidEnum(enumClass = State.class, message = "State is not valid.")
        private State state;
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class IdResponse{
        private Long id;
        public static IdResponse of (BookState bookState){
            return new IdResponse((bookState.getId()));
        }
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class InfoResponse{
        private Long id;
        private Long memberId;
        private String isbn;
        private State state;
        private String categoryName;
        private String bookTitle;
        private String bookAuthor;
        private String bookImgUrl;
        @DateTimeFormat(pattern = "yyyy-mm-dd'T'HH:mm:ss")
        private LocalDateTime readAt;

        public static InfoResponse of(BookState state){
            return new InfoResponse(state.getId(), state.getMember().getId(), state.getIsbn(),
                    state.getState(),state.getCategoryName(),state.getBookTitle(),state.getBookAuthor(),
                    state.getBookImgUrl(),state.getReadAt());
        }
    }

}
