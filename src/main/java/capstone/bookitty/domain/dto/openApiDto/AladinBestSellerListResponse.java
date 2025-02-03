package capstone.bookitty.domain.dto.openApiDto;

import java.util.List;

public record AladinBestSellerListResponse(
        String title,
        String itemPerPage,
        String searchCategoryName,
        List<BestSeller> item
) {
    public record BestSeller(
            String title,
            String link,
            String cover,
            String author,
            String publisher,
            String isbn13,
            String description,
            String pubDate,
            int bestRank
    ){}
}
