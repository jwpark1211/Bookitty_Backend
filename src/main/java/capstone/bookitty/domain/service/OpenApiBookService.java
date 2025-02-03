package capstone.bookitty.domain.service;

import capstone.bookitty.domain.entity.BookSimilarity;
import capstone.bookitty.domain.entity.Star;
import capstone.bookitty.domain.repository.BookSimilarityRepository;
import capstone.bookitty.domain.repository.MemberRepository;
import capstone.bookitty.domain.repository.StarRepository;
import capstone.bookitty.domain.dto.openApiDto.AladinBestSellerListResponse;
import capstone.bookitty.domain.dto.openApiDto.AladinBookSearchListResponse;
import capstone.bookitty.client.AladinOpenApiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class OpenApiBookService {

    private final AladinOpenApiClient aladinOpenApi;
    private final MemberRepository memberRepository;
    private final StarRepository starRepository;
    private final BookSimilarityRepository bookSimilarityRepository;

    public Mono<AladinBookSearchListResponse> searchByBookISBN(String isbn) {
        return aladinOpenApi.searchByBookISBN(isbn);
    }

    public Mono<AladinBookSearchListResponse> searchByKeyword(String keyword) {
        return aladinOpenApi.searchByKeyword(keyword);
    }

    @Cacheable(value = "bestsellers", cacheManager = "cacheManager")
    public Mono<AladinBestSellerListResponse> getBestSeller() {
        return aladinOpenApi.getAllBestSeller();
    }

    @Cacheable(value = "bestsellersByGenre", key = "#a0", unless = "#result == null")
    public Mono<AladinBestSellerListResponse> getBestSellerByGenre(int cid) {
        return aladinOpenApi.getBestSellerByGenre(cid);
    }

    @Cacheable(value = "newBooks", cacheManager = "cacheManager")
    public Mono<AladinBestSellerListResponse> getBestSellerNewBook() {
        return aladinOpenApi.getNewBook();
    }

    @Cacheable(value = "blogChoices", cacheManager = "cacheManager")
    public Mono<AladinBestSellerListResponse> getBlogChoice() {
        return aladinOpenApi.getBlogChoice();
    }

    @Transactional(readOnly = true)
    //@Cacheable(value = "bookRecommendations", key = "#a0", unless = "#result == null")
    public List<String> getRecommendationsForUser(Long memberId) {
        log.info("1. 사용자의 평점 기록 조회");
        List<Star> userRatings = starRepository.findByMemberId(memberId);

        log.info("사용자가 평가한 ISBN 목록 (추천 제외용)");
        Set<String> ratedIsbns = userRatings.stream()
                .map(Star::getIsbn)
                .collect(Collectors.toSet());
        for(String isbn : ratedIsbns) System.out.print(isbn + ", ");

        log.info("2. 후보 ISBN별 누적 가중치 점수를 저장할 맵 (Key: 후보 ISBN, Value: 누적 점수)");
        Map<String, Double> recommendationMap = new HashMap<>();

        log.info("3. 사용자가 남긴 평점이 높은 도서만 대상으로 추천 점수 누적");
        for (Star star : userRatings) {
            if (star.getScore() < 4.0 ) continue;
            String sourceIsbn = star.getIsbn();

            // 해당 도서와 유사한 도서 조회
            List<BookSimilarity> similarBooks = bookSimilarityRepository.findTopSimilarBooks(sourceIsbn);
            for (BookSimilarity similarity : similarBooks) {
                // source와 다른 ISBN 결정
                String candidateIsbn = sourceIsbn.equals(similarity.getIsbn1())
                        ? similarity.getIsbn2()
                        : similarity.getIsbn1();
                // 이미 사용자가 평가한 도서는 추천 대상에서 제외
                if (ratedIsbns.contains(candidateIsbn)) continue;
                // 가중치 점수 계산: 유사도 * 사용자의 평점
                double weightedScore = similarity.getSimilarity() * star.getScore();
                // 같은 ISBN이면 점수를 누적, 처음이면 새로운 항목 추가 (람다식 merge 사용)
                recommendationMap.merge(candidateIsbn, weightedScore, Double::sum);
            }
        }

        log.info("4. 후보 ISBN에 대해 전체 평점 수(또는 읽힌 횟수)를 반영하여 최종 스코어 보정");
        //    예: 최종점수 = 누적점수 * log(1 + ratingCount)
        Map<String, Double> adjustedRecommendationMap = new HashMap<>();
        for (Map.Entry<String, Double> entry : recommendationMap.entrySet()) {
            String candidateIsbn = entry.getKey();
            double weightedScore = entry.getValue();
            // 후보 도서의 전체 평점(읽힘) 횟수를 조회
            long ratingCount = starRepository.countByIsbn(candidateIsbn);
            // 평점 수에 대한 로그 스케일 보정 (값이 0인 경우에도 안전하도록 1을 더함)
            double popularityFactor = Math.log(1 + ratingCount);
            double finalScore = weightedScore * popularityFactor;
            adjustedRecommendationMap.put(candidateIsbn, finalScore);
        }

        log.info("5. 최종 스코어가 높은 순으로 정렬 후 상위 N개의 ISBN만 반환");
        return adjustedRecommendationMap.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(5)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
}
