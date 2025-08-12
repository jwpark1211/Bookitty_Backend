package capstone.bookitty.domain.bookSimilarity.event;

import capstone.bookitty.domain.bookSimilarity.application.BatchStateService;
import capstone.bookitty.domain.bookSimilarity.application.BookSimilarityService;
import capstone.bookitty.domain.star.config.StarStreamConfig;
import capstone.bookitty.domain.star.event.StarRatingEvent;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.PendingMessage;
import org.springframework.data.redis.connection.stream.PendingMessages;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookSimilarityEventStreamService {

    @Qualifier("redisTemplate")
    private final RedisTemplate<String, Object> redisTemplate;
    @Qualifier("starStreamMessageListenerContainer") 
    private final StreamMessageListenerContainer<String, MapRecord<String, String, String>> listenerContainer;
    private final BookSimilarityService bookSimilarityService;

    private volatile boolean pauseProcessing = false;

    @PostConstruct
    public void startStreamListener() {
        try {
            // 기존 pending 메시지 처리
            processPendingMessages();
            
            // Redis Streams Consumer 등록
            registerStreamListener();
            listenerContainer.start();
            log.info("Redis Streams 리스너 시작 완료 - Container Running: {}", listenerContainer.isRunning());
        } catch (Exception e) {
            log.error("Redis Streams 리스너 시작 실패", e);
            // 재시작 시도
            scheduleRestart();
        }
    }

    // EventListener에서 호출되어 Stream에 이벤트 추가
    public void addEvent(StarRatingEvent event) {
        // ISBN 검증
        if (!StringUtils.hasText(event.getIsbn())) {
            log.error("ISBN이 null 또는 비어있는 이벤트 무시 - MemberId: {}, EventType: {}", 
                event.getMemberId(), event.getEventType());
            return;
        }
        
        int retryCount = 0;
        int maxRetries = 3;
        
        while (retryCount < maxRetries) {
            try {
                Map<String, String> eventData = Map.of(
                        "isbn", event.getIsbn(),
                        "memberId", String.valueOf(event.getMemberId()),
                        "currentScore", event.getCurrentScore() != null ? String.valueOf(event.getCurrentScore()) : "",
                        "eventType", event.getEventType().name(),
                        "timestamp", String.valueOf(System.currentTimeMillis())
                );

                redisTemplate.opsForStream().add(StarStreamConfig.STAR_EVENTS_STREAM, eventData);
                log.debug("Redis Streams에 이벤트 추가: {}", event.getIsbn());
                return;

            } catch (Exception e) {
                retryCount++;
                log.warn("Redis Streams 이벤트 추가 실패 (시도 {}/{}): {}", retryCount, maxRetries, event.getIsbn(), e);
                
                if (retryCount >= maxRetries) {
                    log.error("Redis Streams 이벤트 추가 최종 실패: {}", event.getIsbn(), e);
                } else {
                    try {
                        Thread.sleep(100L * retryCount); // 점진적 백오프
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
    }

    // Stream에서 이벤트를 받아서 처리
    private void registerStreamListener() {
        StreamListener<String, MapRecord<String, String, String>> listener = message -> {
            log.info("Redis Streams 메시지 수신: {}", message.getId());
            try {
                if (pauseProcessing) {
                    log.debug("배치 실행 중 - 이벤트 처리 일시 중단: {}", message.getId());
                    return;
                }
                
                // 메시지에서 ISBN 추출하여 유사도 계산
                Map<String, String> values = message.getValue();
                String isbn = values.get("isbn");
                log.info("처리할 ISBN: {}, 전체 메시지: {}", isbn, values);
                
                if (StringUtils.hasText(isbn)) {
                    log.info("유사도 계산 시작: {}", isbn);
                    bookSimilarityService.recalculateSimilarityForBook(isbn);
                    log.info("유사도 계산 완료: {}", isbn);
                } else {
                    log.warn("ISBN이 없는 이벤트 수신: {}, values: {}", message.getId(), values);
                }
                
            } catch (Exception e) {
                log.error("Stream 이벤트 처리 실패: {}", message.getId(), e);
                // 처리 실패한 메시지도 ACK 처리하여 무한 재시도 방지
            }
        };
        
        try {
            // Consumer 등록
            Consumer consumer = Consumer.from(StarStreamConfig.SIMILARITY_CONSUMER_GROUP, StarStreamConfig.SIMILARITY_CONSUMER_NAME);
            StreamOffset<String> offset = StreamOffset.create(StarStreamConfig.STAR_EVENTS_STREAM, org.springframework.data.redis.connection.stream.ReadOffset.from(">"));
            
            listenerContainer.receive(consumer, offset, listener);
            log.info("Redis Streams Consumer 등록 완료");
        } catch (Exception e) {
            log.error("Redis Streams Consumer 등록 실패", e);
            throw e;
        }
    }

    // 배치 시작 이벤트 수신
    @EventListener
    public void onBatchStarted(BatchStateService.BatchStartEvent event) {
        pauseProcessing = true;
        log.info("배치 시작 감지 - 이벤트 처리 일시 중단");
    }

    // 배치 완료 이벤트 수신
    @EventListener
    @Async
    public void onBatchCompleted(BatchStateService.BatchCompletionEvent event) {
        pauseProcessing = false;
        log.info("배치 완료 감지 - 이벤트 처리 재개");
        // 필요 시 대기 중인 이벤트들 재처리 로직 추가
    }
    
    private void processPendingMessages() {
        try {
            Consumer consumer = Consumer.from(StarStreamConfig.SIMILARITY_CONSUMER_GROUP, StarStreamConfig.SIMILARITY_CONSUMER_NAME);
            
            // 현재 consumer의 pending 메시지 확인
            PendingMessages pendingMessages = redisTemplate.opsForStream()
                    .pending(StarStreamConfig.STAR_EVENTS_STREAM, consumer);
            
            if (pendingMessages.size() > 0) {
                log.info("처리 대기 중인 메시지 {}개 발견, 처리 시작", pendingMessages.size());
                
                // 현재 consumer의 pending 메시지들을 claim하여 처리
                RecordId[] recordIds = pendingMessages.stream()
                        .map(pm -> RecordId.of(pm.getIdAsString()))
                        .toArray(RecordId[]::new);
                        
                List<MapRecord<String, Object, Object>> claimedMessages = redisTemplate.opsForStream()
                        .claim(StarStreamConfig.STAR_EVENTS_STREAM,
                                StarStreamConfig.SIMILARITY_CONSUMER_GROUP,
                                StarStreamConfig.SIMILARITY_CONSUMER_NAME,
                                java.time.Duration.ofSeconds(0), // 즉시 claim
                                recordIds);
                
                for (MapRecord<String, Object, Object> message : claimedMessages) {
                    try {
                        Object isbnObj = message.getValue().get("isbn");
                        String isbn = isbnObj != null ? isbnObj.toString() : null;
                        if (StringUtils.hasText(isbn)) {
                            log.info("Pending 메시지 처리 시작: {}, ISBN: {}", message.getId(), isbn);
                            bookSimilarityService.recalculateSimilarityForBook(isbn);
                            log.info("Pending 메시지 처리 완료: {}", message.getId());
                        }
                        
                        // 처리 완료된 메시지 ACK
                        redisTemplate.opsForStream().acknowledge(
                                StarStreamConfig.STAR_EVENTS_STREAM,
                                StarStreamConfig.SIMILARITY_CONSUMER_GROUP,
                                message.getId());
                        
                    } catch (Exception e) {
                        log.error("Pending 메시지 처리 실패: {}", message.getId(), e);
                        // 실패한 메시지도 ACK 처리 (무한 재시도 방지)
                        redisTemplate.opsForStream().acknowledge(
                                StarStreamConfig.STAR_EVENTS_STREAM,
                                StarStreamConfig.SIMILARITY_CONSUMER_GROUP,
                                message.getId());
                    }
                }
                
                log.info("Pending 메시지 처리 완료: {}개", claimedMessages.size());
            } else {
                log.info("처리 대기 중인 메시지 없음");
            }
            
        } catch (Exception e) {
            log.error("Pending 메시지 처리 중 오류", e);
        }
    }
    
    private void scheduleRestart() {
        // 간단한 재시작 로직 - 실제 환경에서는 더 정교한 백오프 전략 사용
        try {
            Thread.sleep(5000); // 5초 대기
            if (!listenerContainer.isRunning()) {
                startStreamListener();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Redis Streams 재시작 중단됨");
        }
    }
}