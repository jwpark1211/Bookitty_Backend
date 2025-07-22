package capstone.bookitty.domain.bookSimilarity.similarityBatch.item;

import capstone.bookitty.domain.bookSimilarity.domain.BookSimilarity;
import capstone.bookitty.domain.bookSimilarity.similarityBatch.item.calculator.CosineSimilarityCalculator;
import capstone.bookitty.domain.bookSimilarity.similarityBatch.item.dto.BookPairDto;
import capstone.bookitty.domain.star.domain.Star;
import capstone.bookitty.domain.star.repository.StarRepository;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookSimilarityProcessor implements ItemProcessor<BookPairDto, BookSimilarity> {

    private final StarRepository starRepository;
    private final CosineSimilarityCalculator similarityCalculator;

    private final Map<String, Map<Long, Double>> cache = new ConcurrentHashMap<>();

    private static final int MIN_COMMON_USERS = 3;
    private static final double MIN_SIMILARITY_THRESHOLD = 0.1;

    @Override
    public BookSimilarity process(BookPairDto bookPair) {
        String isbn1 = bookPair.isbn1();
        String isbn2 = bookPair.isbn2();

        Map<Long, Double> isbn1Ratings = getRatingsMapCached(isbn1);
        Map<Long, Double> isbn2Ratings = getRatingsMapCached(isbn2);

        Set<Long> commonUsers = isbn1Ratings.keySet().stream()
                .filter(isbn2Ratings::containsKey)
                .collect(Collectors.toSet());

        if (commonUsers.size() < MIN_COMMON_USERS) {
            log.debug("공통 사용자 수 부족: {}권과 {}권 (공통 사용자: {}명)", isbn1, isbn2, commonUsers.size());
            return null;
        }

        double similarity = similarityCalculator.calculate(isbn1Ratings, isbn2Ratings, commonUsers);

        if (Math.abs(similarity) < MIN_SIMILARITY_THRESHOLD) {
            log.debug("유사도 임계값 미만: {}권과 {}권 (유사도: {:.4f})", isbn1, isbn2, similarity);
            return null;
        }

        log.debug("유사도 계산 완료: {}권과 {}권 (유사도: {:.4f}, 공통 사용자: {}명)",
                isbn1, isbn2, similarity, commonUsers.size());

        return BookSimilarity.builder()
                .isbn1(isbn1)
                .isbn2(isbn2)
                .similarity(similarity)
                .build();
    }

    private Map<Long, Double> getRatingsMapCached(String isbn) {
        return cache.computeIfAbsent(isbn, this::getRatingsMap);
    }

    private Map<Long, Double> getRatingsMap(String isbn) {
        log.debug("DB 조회: {}", isbn); // 각 책마다 1번만 출력되어야 함
        List<Star> stars = starRepository.findByIsbn(isbn);
        return stars.stream()
                .collect(Collectors.toMap(
                        Star::getMemberId,
                        Star::getScore
                ));
    }

    @PreDestroy
    public void cleanup() {
        cache.clear();
        log.info("평점 캐시 정리 완료");
    }
}