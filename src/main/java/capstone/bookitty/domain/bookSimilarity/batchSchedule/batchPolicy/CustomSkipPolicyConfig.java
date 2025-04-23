package capstone.bookitty.domain.bookSimilarity.batchSchedule.batchPolicy;


import org.springframework.batch.core.step.skip.LimitCheckingItemSkipPolicy;
import org.springframework.batch.core.step.skip.SkipPolicy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.BadSqlGrammarException;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class CustomSkipPolicyConfig {

    @Bean
    public SkipPolicy customSkipPolicy() {
        Map<Class<? extends Throwable>, Boolean> skippableExceptions = new HashMap<>();

        skippableExceptions.put(IllegalArgumentException.class, true);
        skippableExceptions.put(NullPointerException.class, true);

        int skipLimit = 10;
        return new LimitCheckingItemSkipPolicy(skipLimit, skippableExceptions);
    }
}
