package capstone.bookitty.domain.star.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
public class StarRatingEvent {
    
    private final String isbn;
    private final Long memberId;
    private final Double previousScore;
    private final Double currentScore;
    private final EventType eventType;
    private final LocalDateTime occurredAt;
    
    public static StarRatingEvent created(String isbn, Long memberId, Double currentScore) {
        return new StarRatingEvent(
            isbn, 
            memberId, 
            null, 
            currentScore, 
            EventType.CREATED,
            LocalDateTime.now()
        );
    }
    
    public static StarRatingEvent updated(String isbn, Long memberId, Double previousScore, Double currentScore) {
        return new StarRatingEvent(
            isbn, 
            memberId, 
            previousScore, 
            currentScore, 
            EventType.UPDATED,
            LocalDateTime.now()
        );
    }
    
    public static StarRatingEvent deleted(String isbn, Long memberId, Double previousScore) {
        return new StarRatingEvent(
            isbn, 
            memberId, 
            previousScore, 
            null, 
            EventType.DELETED,
            LocalDateTime.now()
        );
    }
    
    public enum EventType {
        CREATED, UPDATED, DELETED
    }
}