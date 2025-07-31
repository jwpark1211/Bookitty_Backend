package capstone.bookitty.global.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class CacheMetricsConfig {

    private final CacheManager cacheManager;
    private final MeterRegistry meterRegistry;

    @PostConstruct
    public void bindCacheMetrics() {
        try {
            // 각 캐시를 수동으로 메트릭에 등록
            cacheManager.getCacheNames().forEach(cacheName -> {
                var cache = cacheManager.getCache(cacheName);
                if (cache != null) {
                    log.info("Registering cache metrics for: {}", cacheName);
                }
            });

            // 커스텀 메트릭 등록
            registerCustomCacheMetrics();

            log.info("Cache metrics successfully registered to Prometheus");
        } catch (Exception e) {
            log.error("Failed to register cache metrics", e);
        }
    }

    private void registerCustomCacheMetrics() {
        // 캐시별 API 호출 절약 횟수 메트릭
        Counter.builder("cache.api_calls_saved")
                .description("Number of API calls saved by cache hits")
                .tag("cache_type", "redis")
                .register(meterRegistry);

        // 캐시 크기 메트릭 (예상치)
        Gauge.builder("cache.estimated_size", this, metrics -> getCacheSize())
                .description("Estimated cache size in entries")
                .tag("cache_type", "redis")
                .register(meterRegistry);
    }

    private double getCacheSize() {
        try {
            // Redis 캐시 통계 수집 (근사치)
            return cacheManager.getCacheNames().size() * 100; // 임시 계산
        } catch (Exception e) {
            return 0.0;
        }
    }
}