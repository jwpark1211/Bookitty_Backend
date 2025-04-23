package capstone.bookitty.domain.bookSimilarity.batchSchedule.batchPolicy;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DataAccessException;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.RetryPolicy;

@Configuration
public class CustomRetryPolicyConfig {

    @Bean
    public RetryPolicy customRetryPolicy() {
        Map<Class<? extends Throwable>, Boolean> retryableExceptions = new HashMap<>();

        retryableExceptions.put(DataAccessException.class, true);
        retryableExceptions.put(TimeoutException.class, true);
        retryableExceptions.put(IllegalStateException.class, true);

        return new SimpleRetryPolicy(3, retryableExceptions);
    }
}
