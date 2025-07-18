package capstone.bookitty.domain.book.api;

import capstone.bookitty.domain.book.api.dto.AladinBookSearchListResponse;
import capstone.bookitty.domain.book.application.BookRecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "book", description = "도서 개인 추천 api 입니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/books")
public class BookRecommendationApi {

    private final BookRecommendationService bookRecommendationService;

    @Operation(summary = "사용자별 도서 추천 Top 10")
    @GetMapping(path = "/members/{member-id}/recommendation")
    public List<AladinBookSearchListResponse> getRecommendations(
            @PathVariable("member-id") long memberId) {
        return bookRecommendationService.getRecommendationsForUser(memberId);
    }

}
