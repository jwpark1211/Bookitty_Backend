package capstone.bookitty.global.api.openApi;

import capstone.bookitty.global.api.dto.AladinBestSellerResponseDTO;
import capstone.bookitty.global.api.dto.AladinBookListResponseDTO;
import capstone.bookitty.global.api.dto.AladinBookSearchResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;


@Component
public class AladinOpenApi {

    @Value("${api.aladin.ttb-key}")
    private String ttb;

    private final WebClient aladinWebClientApi;

    public AladinOpenApi(@Qualifier("AladinWebClient") WebClient aladinWebClientApi){
        this.aladinWebClientApi = aladinWebClientApi;
    }

    private static final String BOOK_ISBN_URI = "/ItemLookUp.aspx";
    private static final String BOOK_SEARCH_URI = "/ItemSearch.aspx";
    private static final String BOOK_BESTSELLER_URI = "/ItemList.aspx";

    public Mono<AladinBookListResponseDTO> searchByBookISBN(String isbn) {
        return aladinWebClientApi
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(BOOK_ISBN_URI)
                        .queryParam("ttbkey", ttb)
                        .queryParam("ItemId", isbn)
                        .queryParam("ItemIdType","ISBN13")
                        .queryParam("Version",20131101)
                        .queryParam("Cover","Big")
                        .queryParam("OptResult","ratingInfo,cardReviewImgList")
                        .queryParam("output","js")
                        .build())
                .retrieve()
                .bodyToMono(AladinBookListResponseDTO.class);
    }

    public Mono<AladinBookSearchResponseDTO> searchByKeyword(String keyword){
        return aladinWebClientApi
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(BOOK_SEARCH_URI)
                        .queryParam("ttbkey", ttb)
                        .queryParam("Query", keyword)
                        .queryParam("Version",20131101)
                        .queryParam("Cover","Big")
                        .queryParam("OptResult","ratingInfo,cardReviewImgList")
                        .queryParam("output","js")
                        .build())
                .retrieve()
                .bodyToMono(AladinBookSearchResponseDTO.class);
    }

    public Mono<AladinBestSellerResponseDTO> getAllBestSeller() {
        return aladinWebClientApi
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(BOOK_BESTSELLER_URI)
                        .queryParam("ttbkey", ttb)
                        .queryParam("QueryType", "Bestseller")
                        .queryParam("SearchTarget","Book")
                        .queryParam("Version",20131101)
                        .queryParam("Cover","Big")
                        .queryParam("output","js")
                        .build())
                .retrieve()
                .bodyToMono(AladinBestSellerResponseDTO.class);
    }


    /*  170 : 국내 경제경영
        987 : 과학
        2551 : 만화
        798 : 사회
        1 : 소설/시/희곡
        656 : 인문
        336 : 자기계발
        351 : 컴퓨터/모바일 */
    public Mono<AladinBestSellerResponseDTO> getBestSellerByGenre(int category) {
        return aladinWebClientApi
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(BOOK_BESTSELLER_URI)
                        .queryParam("ttbkey", ttb)
                        .queryParam("QueryType", "Bestseller")
                        .queryParam("SearchTarget","Book")
                        .queryParam("Version",20131101)
                        .queryParam("CategoryId",category)
                        .queryParam("Cover","Big")
                        .queryParam("output","js")
                        .build())
                .retrieve()
                .bodyToMono(AladinBestSellerResponseDTO.class);
    }

    public Mono<AladinBestSellerResponseDTO> getNewBook(){
        return aladinWebClientApi
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(BOOK_BESTSELLER_URI)
                        .queryParam("ttbkey", ttb)
                        .queryParam("QueryType", "ItemNewSpecial")
                        .queryParam("SearchTarget","Book")
                        .queryParam("Version",20131101)
                        .queryParam("Cover","Big")
                        .queryParam("output","js")
                        .build())
                .retrieve()
                .bodyToMono(AladinBestSellerResponseDTO.class);
    }

    public Mono<AladinBestSellerResponseDTO> getBlogChoice(){
        return aladinWebClientApi
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(BOOK_BESTSELLER_URI)
                        .queryParam("ttbkey", ttb)
                        .queryParam("QueryType", "BlogBest")
                        .queryParam("SearchTarget","Book")
                        .queryParam("Version",20131101)
                        .queryParam("Cover","Big")
                        .queryParam("output","js")
                        .build())
                .retrieve()
                .bodyToMono(AladinBestSellerResponseDTO.class);
    }

}
