package capstone.bookitty.domain.book.api;

import capstone.bookitty.domain.book.application.BookService;
import capstone.bookitty.domain.book.dto.AladinBestSellerListResponse;
import capstone.bookitty.domain.book.dto.AladinBookSearchListResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

@Tag(name = "book", description = "도서 정보 api 입니다.")
@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/books")
public class BookApi {

    private final BookService openApiBookService;

    @Operation(summary = "isbn(13)으로 책 세부 정보 확인")
    @GetMapping(path = "/isbn/{isbn}")
    public Mono<AladinBookSearchListResponse> getBookbyISBN(
            @PathVariable("isbn") String isbn
    ) {
        return openApiBookService.searchByBookISBN(isbn);
    }

    @Operation(summary = "keyword로 검색하기(제목, 저자, 출판사 모든 결과 조회 가능)")
    @GetMapping(path = "/keyword/{keyword}")
    public Mono<AladinBookSearchListResponse> getBooksByKeyword(
            @PathVariable("keyword") String keyword
    ){
        return openApiBookService.searchByKeyword(keyword);
    }

    @Operation(summary = "전체 베스트셀러 Top 10")
    @GetMapping(path = "/bestseller")
    public Mono<AladinBestSellerListResponse> getBestSeller(){
        return openApiBookService.getBestSeller();
    }


    @Operation(summary = "카테고리별 베스트셀러 Top 10\n / CategoryId: " +
            "(170 : 경제경영 / 987 : 과학 / 1 : 문학 / 656 : 인문 / 336 : 자기계발)")
    @GetMapping(path = "/bestseller/categories/{category-id}")
    public Mono<AladinBestSellerListResponse> getBestSellerByGenre(
            @PathVariable("category-id") int cid
    ){
        return openApiBookService.getBestSellerByGenre(cid);
    }

    @Operation(summary = "신간 베스트셀러 Top 10")
    @GetMapping(path = "/bestseller/newBook")
    public Mono<AladinBestSellerListResponse> getBestSellerNewBook(){
        return openApiBookService.getBestSellerNewBook();
    }

    @Operation(summary = "blogChoice 베스트셀러 Top 10")
    @GetMapping(path = "/bestseller/blogChoice")
    public Mono<AladinBestSellerListResponse> getBestSellerBlogChoice(){
        return openApiBookService.getBlogChoice();
    }

    @Operation(summary = "사용자별 도서 추천 Top 10")
    @GetMapping(path = "/members/{member-id}/recommendation")
    public List<AladinBookSearchListResponse> getRecommendations(
            @PathVariable("member-id") long memberId) {
        return openApiBookService.getRecommendationsForUser(memberId);
    }
}
