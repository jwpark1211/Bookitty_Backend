package capstone.bookitty.domain.bookSimilarity.event;

import capstone.bookitty.domain.bookSimilarity.application.BookSimilarityService;
import capstone.bookitty.domain.star.event.StarRatingEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class BookSimilarityEventListener {
    
    private final BookSimilarityService bookSimilarityService;
    
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void handleStarRatingEvent(StarRatingEvent event) {
        bookSimilarityService.recalculateSimilarityForBook(event.getIsbn());
    }
}