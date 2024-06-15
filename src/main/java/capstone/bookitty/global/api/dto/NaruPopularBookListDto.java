package capstone.bookitty.global.api.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class NaruPopularBookListDto {
    public PopularBookListResponse response;

    @Getter
    @Setter
    public static class PopularBookListResponse {
        public int resultNum;
        public List<NaruPopularBookDto> docs;
    }
}