package capstone.bookitty.domain.book.application;

import capstone.bookitty.domain.book.api.dto.AladinBestSellerListResponse;
import capstone.bookitty.domain.book.api.dto.AladinBookSearchListResponse;
import capstone.bookitty.domain.book.client.AladinOpenApiClient;
import capstone.bookitty.global.config.RedisHealthIndicator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookAladinService {

    private final AladinOpenApiClient aladinOpenApi;
    private final RedisHealthIndicator redisHealthIndicator;

    // ISBN으로 책 검색
    @Cacheable(value = "bookDetail", 
               key = "#root.methodName + ':' + #isbn", 
               unless = "#result == null",
               condition = "@redisHealthIndicator.isHealthy()")
    public Mono<AladinBookSearchListResponse> searchByBookISBN(String isbn) {
        return aladinOpenApi.searchByBookISBN(isbn);
    }

    // 키워드로 책 검색 (캐시 제외)
    public Mono<AladinBookSearchListResponse> searchByKeyword(String keyword) {
        return aladinOpenApi.searchByKeyword(keyword);
    }

    // 베스트셀러
    @Cacheable(value = "bestsellers", 
               key = "#root.methodName",
               condition = "@redisHealthIndicator.isHealthy()")
    public Mono<AladinBestSellerListResponse> getBestSeller() {
        return aladinOpenApi.getAllBestSeller();
    }

    // 장르별 베스트셀러
    @Cacheable(value = "bestsellersByGenre", 
               key = "#root.methodName + ':' + #cid", 
               unless = "#result == null",
               condition = "@redisHealthIndicator.isHealthy()")
    public Mono<AladinBestSellerListResponse> getBestSellerByGenre(int cid) {
        return aladinOpenApi.getBestSellerByGenre(cid);
    }

    // 신간 베스트셀러
    @Cacheable(value = "newBooks", 
               key = "#root.methodName",
               condition = "@redisHealthIndicator.isHealthy()")
    public Mono<AladinBestSellerListResponse> getBestSellerNewBook() {
        return aladinOpenApi.getNewBook();
    }

    // 블로그 추천 도서
    @Cacheable(value = "blogChoices", 
               key = "#root.methodName",
               condition = "@redisHealthIndicator.isHealthy()")
    public Mono<AladinBestSellerListResponse> getBlogChoice() {
        return aladinOpenApi.getBlogChoice();
    }

}
