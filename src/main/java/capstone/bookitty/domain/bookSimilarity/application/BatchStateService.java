package capstone.bookitty.domain.bookSimilarity.application;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEvent;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class BatchStateService {
    
    // 배치 시작 이벤트
    public static class BatchStartEvent extends ApplicationEvent {
        public BatchStartEvent(Object source) {
            super(source);
        }
    }
    
    // 배치 완료 이벤트
    public static class BatchCompletionEvent extends ApplicationEvent {
        public BatchCompletionEvent(Object source) {
            super(source);
        }
    }
}