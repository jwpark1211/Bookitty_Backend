package capstone.bookitty.domain.star.config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;

import java.time.Duration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class StarStreamConfig {

    public static final String STAR_EVENTS_STREAM = "star-events";
    public static final String SIMILARITY_CONSUMER_GROUP = "similarity-processor";
    public static final String SIMILARITY_CONSUMER_NAME = "similarity-consumer-1";
    public static final int MAX_STREAM_LENGTH = 5000;

    @Qualifier("redisTemplate")
    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisConnectionFactory connectionFactory;
    private StreamMessageListenerContainer<String, MapRecord<String, String, String>> listenerContainer;

    @PostConstruct
    public void initializeStream() {
        try {
            if (!streamExists()) {
                createStream();
            }

            if (!consumerGroupExists()) {
                createConsumerGroup();
            }

            log.info("Redis Star Streams 초기화 완료 - Stream: {}, Consumer Group: {}",
                    STAR_EVENTS_STREAM, SIMILARITY_CONSUMER_GROUP);
        } catch (Exception e) {
            log.error("Redis Star Streams 초기화 실패", e);
        }
    }

    @Bean
    public StreamMessageListenerContainer<String, MapRecord<String, String, String>> starStreamMessageListenerContainer() {
        var options = StreamMessageListenerContainer.StreamMessageListenerContainerOptions
                .builder()
                .pollTimeout(Duration.ofMillis(1000))  // 1초 대기
                .build();

        this.listenerContainer = StreamMessageListenerContainer.create(connectionFactory, options);

        return listenerContainer;
    }

    private boolean streamExists() {
        try {
            redisTemplate.opsForStream().info(STAR_EVENTS_STREAM);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void createStream() {
        try {
            redisTemplate.opsForStream().add(STAR_EVENTS_STREAM,
                    java.util.Map.of("init", "stream initialization"));
            log.info("Redis Star Stream 생성 완료: {}", STAR_EVENTS_STREAM);
        } catch (Exception e) {
            log.error("Redis Star Stream 생성 실패: {}", STAR_EVENTS_STREAM, e);
        }
    }

    private boolean consumerGroupExists() {
        try {
            return redisTemplate.opsForStream().groups(STAR_EVENTS_STREAM)
                    .stream()
                    .anyMatch(group -> SIMILARITY_CONSUMER_GROUP.equals(group.groupName()));
        } catch (Exception e) {
            return false;
        }
    }

    private void createConsumerGroup() {
        try {
            redisTemplate.opsForStream().createGroup(STAR_EVENTS_STREAM, SIMILARITY_CONSUMER_GROUP);
            log.info("Redis Consumer Group 생성 완료: {}", SIMILARITY_CONSUMER_GROUP);
        } catch (Exception e) {
            log.error("Redis Consumer Group 생성 실패: {}", SIMILARITY_CONSUMER_GROUP, e);
        }
    }

    @PreDestroy
    public void cleanup() {
        if (listenerContainer != null && listenerContainer.isRunning()) {
            listenerContainer.stop();
            log.info("Redis Star Streams 리스너 컨테이너 정리 완료");
        }
    }
}