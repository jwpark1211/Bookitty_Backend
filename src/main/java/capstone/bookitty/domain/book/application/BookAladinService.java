package capstone.bookitty.domain.book.application;

import capstone.bookitty.domain.book.api.dto.AladinBestSellerListResponse;
import capstone.bookitty.domain.book.api.dto.AladinBookSearchListResponse;
import capstone.bookitty.domain.book.client.AladinOpenApiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class BookAladinService {

    private final AladinOpenApiClient aladinOpenApi;

    // ISBN으로 책 검색
    @Cacheable(value = "bookDetail", key = "'book:' + #isbn", unless = "#result == null")
    public Mono<AladinBookSearchListResponse> searchByBookISBN(String isbn) {
        return aladinOpenApi.searchByBookISBN(isbn);
    }

    // 키워드로 책 검색
    public Mono<AladinBookSearchListResponse> searchByKeyword(String keyword) {
        return aladinOpenApi.searchByKeyword(keyword);
    }

    // 베스트셀러
    @Cacheable(value = "bestsellers", cacheManager = "cacheManager")
    public Mono<AladinBestSellerListResponse> getBestSeller() {
        return aladinOpenApi.getAllBestSeller();
    }

    // 장르별 베스트셀러
    @Cacheable(value = "bestsellersByGenre", key = "#a0", unless = "#result == null")
    public Mono<AladinBestSellerListResponse> getBestSellerByGenre(int cid) {
        return aladinOpenApi.getBestSellerByGenre(cid);
    }

    // 신간 베스트셀러
    @Cacheable(value = "newBooks", cacheManager = "cacheManager")
    public Mono<AladinBestSellerListResponse> getBestSellerNewBook() {
        return aladinOpenApi.getNewBook();
    }

    // 블로그 추천 도서
    @Cacheable(value = "blogChoices", cacheManager = "cacheManager")
    public Mono<AladinBestSellerListResponse> getBlogChoice() {
        return aladinOpenApi.getBlogChoice();
    }

}
