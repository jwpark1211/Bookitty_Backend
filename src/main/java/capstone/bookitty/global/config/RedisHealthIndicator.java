package capstone.bookitty.global.config;

import capstone.bookitty.global.notification.slack.SlackNotificationClient;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisHealthIndicator implements HealthIndicator {

    private final RedisConnectionFactory redisConnectionFactory;
    private final MeterRegistry meterRegistry;
    private final SlackNotificationClient slackNotificationClient;
    private volatile boolean isHealthy = true;
    private volatile boolean lastHealthStatus = true;

    @PostConstruct
    public void init() {
        // Prometheus 메트릭 등록 - 조회될 때마다 실시간 체크
        Gauge.builder("redis.health.status", this, indicator -> indicator.checkRedisHealth() ? 1.0 : 0.0)
                .description("Redis connection health status (1=UP, 0=DOWN)")
                .register(meterRegistry);
    }

    @Override
    public Health health() {
        boolean healthy = checkRedisHealth();

        if (healthy) {
            return Health.up()
                    .withDetail("redis", "Available")
                    .build();
        } else {
            return Health.down()
                    .withDetail("redis", "Connection failed")
                    .build();
        }
    }

    /**
     * 실제 Redis 연결 상태 체크
     * Prometheus 메트릭 수집시마다 호출됨
     */
    private boolean checkRedisHealth() {
        try {
            var connection = redisConnectionFactory.getConnection();
            String pong = connection.ping();
            isHealthy = "PONG".equals(pong);

            if (!isHealthy) {
                log.warn("Redis ping returned unexpected response: {}", pong);
                sendSlackAlertIfNeeded("Unexpected ping response: " + pong);
            }
        } catch (Exception e) {
            isHealthy = false;
            log.warn("Redis health check failed: {}", e.getMessage());
            sendSlackAlertIfNeeded(e.getMessage());
        }

        // 상태 변경시에만 알림
        lastHealthStatus = isHealthy;
        return isHealthy;
    }

    private void sendSlackAlertIfNeeded(String errorMessage) {
        // 이전에 정상이었다가 실패한 경우에만 알림
        if (lastHealthStatus && !isHealthy) {
            slackNotificationClient.sendRedisFailureNotification(errorMessage);
        }
    }

}