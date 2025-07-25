package capstone.bookitty.domain.bookSimilarity.similarityBatch.item;

import capstone.bookitty.domain.bookSimilarity.application.BookSimilarityService;
import capstone.bookitty.domain.bookSimilarity.domain.BookSimilarity;
import capstone.bookitty.domain.bookSimilarity.similarityBatch.item.dto.BookPairDto;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookSimilarityProcessor implements ItemProcessor<BookPairDto, BookSimilarity> {

    private final BookSimilarityService bookSimilarityService;
    private final Map<String, Map<Long, Double>> cache = new HashMap<>();

    @Override
    public BookSimilarity process(BookPairDto bookPair) {
        return bookSimilarityService.calculateAndSaveSimilarity(
            bookPair.isbn1(), 
            bookPair.isbn2(),
            cache
        );
    }


    public void clearCache() {
        cache.clear();
        log.info("배치 시작 전 캐시 수동 정리 완료");
    }

    /**
     * 배치 작업 종료 시(SpringApplication 종료 시) 캐시를 정리합니다.
     */
    @PreDestroy
    public void cleanup() {
        cache.clear();
        log.info("평점 캐시 정리 완료");
    }

}