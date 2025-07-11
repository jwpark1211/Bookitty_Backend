package capstone.bookitty.global.config;

import org.springframework.batch.core.step.skip.LimitCheckingItemSkipPolicy;
import org.springframework.batch.core.step.skip.SkipPolicy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DataAccessException;
import org.springframework.retry.RetryPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

@Configuration
public class BatchPolicyConfig {

    @Bean
    public RetryPolicy customRetryPolicy() {
        Map<Class<? extends Throwable>, Boolean> retryableExceptions = new HashMap<>();

        retryableExceptions.put(DataAccessException.class, true);
        retryableExceptions.put(TimeoutException.class, true);
        retryableExceptions.put(IllegalStateException.class, true);

        return new SimpleRetryPolicy(3, retryableExceptions);
    }

    @Bean
    public SkipPolicy customSkipPolicy() {
        Map<Class<? extends Throwable>, Boolean> skippableExceptions = new HashMap<>();

        skippableExceptions.put(IllegalArgumentException.class, true);
        skippableExceptions.put(NullPointerException.class, true);

        int skipLimit = 10;
        return new LimitCheckingItemSkipPolicy(skipLimit, skippableExceptions);
    }

}
