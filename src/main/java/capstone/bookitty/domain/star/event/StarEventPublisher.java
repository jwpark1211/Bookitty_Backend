package capstone.bookitty.domain.star.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class StarEventPublisher {
    
    private final ApplicationEventPublisher applicationEventPublisher;
    
    public void publishStarCreated(String isbn, Long memberId, Double score) {
        StarRatingEvent event = StarRatingEvent.created(isbn, memberId, score);
        log.info("[Event] Star rating created event published - ISBN: {}, MemberId: {}, Score: {}", 
            isbn, memberId, score);
        applicationEventPublisher.publishEvent(event);
    }
    
    public void publishStarUpdated(String isbn, Long memberId, Double previousScore, Double currentScore) {
        StarRatingEvent event = StarRatingEvent.updated(isbn, memberId, previousScore, currentScore);
        log.info("[Event] Star rating updated event published - ISBN: {}, MemberId: {}, Previous: {}, Current: {}", 
            isbn, memberId, previousScore, currentScore);
        applicationEventPublisher.publishEvent(event);
    }
    
    public void publishStarDeleted(String isbn, Long memberId, Double previousScore) {
        StarRatingEvent event = StarRatingEvent.deleted(isbn, memberId, previousScore);
        log.info("[Event] Star rating deleted event published - ISBN: {}, MemberId: {}, Previous: {}", 
            isbn, memberId, previousScore);
        applicationEventPublisher.publishEvent(event);
    }
}