package capstone.bookitty.domain.bookSimilarity.event;

import capstone.bookitty.domain.star.event.StarRatingEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookSimilarityEventListener {
    
    private final CacheManager cacheManager;
    private final BookSimilarityEventStreamService streamService;
    
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleStarRatingEvent(StarRatingEvent event) {
        // 1. 즉시 캐시 무효화
        evictBookRatingsCache(event.getIsbn());
        
        // 2. Redis Streams에 이벤트 큐잉 (배치 상태와 무관하게)
        streamService.addEvent(event);
    }
    
    private void evictBookRatingsCache(String isbn) {
        try {
            cacheManager.getCache("book-ratings").evict(isbn);
            log.debug("평점 캐시 무효화 완료: {}", isbn);
        } catch (Exception e) {
            log.warn("평점 캐시 무효화 실패: {}", isbn, e);
        }
    }
}