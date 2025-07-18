package capstone.bookitty.domain.book.application;

import capstone.bookitty.domain.book.api.dto.AladinBookSearchListResponse;
import capstone.bookitty.domain.bookSimilarity.domain.BookSimilarity;
import capstone.bookitty.domain.bookSimilarity.repository.BookSimilarityRepository;
import capstone.bookitty.domain.star.domain.Star;
import capstone.bookitty.domain.star.repository.StarRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookRecommendationService {

    private final StarRepository starRepository;
    private final BookSimilarityRepository bookSimilarityRepository;
    private final BookAladinService bookAladinApiService;

    //@Cacheable(value = "bookRecommendations", key = "'user:' + #memberId", unless = "#result == null")
    public List<AladinBookSearchListResponse> getRecommendationsForUser(Long memberId) {
        // 1. 평점 ≥ 4.0인 사용자 평점만 DB에서 조회
        List<Star> highRatedStars = starRepository.findByMemberIdAndScoreGreaterThanEqual(memberId, 4.0);
        if (highRatedStars.isEmpty()) {
            return Collections.emptyList();
        }
        Set<String> highRatedIsbns = highRatedStars.stream()
                .map(Star::getIsbn)
                .collect(Collectors.toSet());

        // 2. 모든 유사도 관계를 한 번에 조회
        List<BookSimilarity> allSimilar = bookSimilarityRepository.findByIsbn1InOrIsbn2In(highRatedIsbns, highRatedIsbns);

        // 3. 후보 ISBN별 가중치 점수 누적
        Map<String, Double> recommendationMap = new HashMap<>();
        for (Star star : highRatedStars) {
            String src = star.getIsbn();
            double score = star.getScore();
            for (BookSimilarity bs : allSimilar) {
                boolean isSrc1 = src.equals(bs.getIsbn1());
                boolean isSrc2 = src.equals(bs.getIsbn2());
                if (!isSrc1 && !isSrc2) continue;

                String candidate = isSrc1 ? bs.getIsbn2() : bs.getIsbn1();
                if (highRatedIsbns.contains(candidate)) continue; // 이미 평점한 도서 제외

                double weighted = bs.getSimilarity() * score;
                recommendationMap.merge(candidate, weighted, Double::sum);
            }
        }

        // 4. 후보 ISBN별 평점 수 조회
        Map<String, Long> countMap = new HashMap<>();
        for (String candidate : recommendationMap.keySet()) {
            long cnt = starRepository.countByIsbn(candidate);
            countMap.put(candidate, cnt);
        }

        // 5. 로그 스케일 반영하여 최종 점수 계산 및 상위 10개 추출
        List<String> top10 = recommendationMap.entrySet().stream()
                .map(e -> Map.entry(e.getKey(), e.getValue() * Math.log(1 + countMap.getOrDefault(e.getKey(), 0L))))
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(10)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        // 6. 최종 도서 상세정보를 반환
        return top10.parallelStream()
                .map(bookAladinApiService::searchByBookISBN)
                .map(Mono::block)
                .collect(Collectors.toList());
    }
}
