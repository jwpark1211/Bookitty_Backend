package capstone.bookitty.global.api.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ResponseWrapper {
    private Response response;

    @Getter
    @Setter
    public static class Response {
        private int resultNum;
        private List<NaruPopularBookDto> docs;
    }
}
