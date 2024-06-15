package capstone.bookitty.global.api.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NaruPopularBookDto {
    public PopularBook doc;

    @Getter
    @Setter
    public static class PopularBook {
        public String bookname;
        public String authors;
        public String publisher;
        public int ranking;
        public String isbn13;
        public String bookImageURL;
    }
}
