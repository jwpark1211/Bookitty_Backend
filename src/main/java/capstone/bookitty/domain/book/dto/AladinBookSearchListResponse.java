package capstone.bookitty.domain.book.dto;

import java.util.List;

public record AladinBookSearchListResponse(
        List<DetailBook> item
) {
    public record DetailBook(
            String title,
            String link,
            String author,
            String pubDate,
            String description,
            String isbn13,
            int priceStandard,
            String cover,
            String publisher,
            String categoryName,
            String mallType
    ){}
}
