package capstone.bookitty.domain.bookSimilarity.similarityBatch.config;

import capstone.bookitty.domain.bookSimilarity.domain.BookSimilarity;
import capstone.bookitty.domain.bookSimilarity.similarityBatch.item.BookSimilarityProcessor;
import capstone.bookitty.domain.bookSimilarity.similarityBatch.item.BookSimilarityReader;
import capstone.bookitty.domain.bookSimilarity.similarityBatch.item.BookSimilarityWriter;
import capstone.bookitty.domain.bookSimilarity.similarityBatch.item.dto.BookPairDto;
import capstone.bookitty.domain.bookSimilarity.similarityBatch.item.listener.BookSimilarityStepListener;
import capstone.bookitty.global.config.DataDBConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import java.sql.SQLException;
import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class BookSimilarityBatchConfig {

    @Qualifier(DataDBConfig.DATA_TRANSACTION_MANAGER)
    private final PlatformTransactionManager dataTransactionManager;
    private final JobRepository jobRepository;
    private final BookSimilarityReader bookSimilarityReader;
    private final BookSimilarityProcessor bookSimilarityProcessor;
    private final BookSimilarityWriter bookSimilarityWriter;

    @Bean
    public Job bookSimilarityCalculationJob(Step bookSimilarityCalculationStep) {
        return new JobBuilder("bookSimilarityCalculationJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(bookSimilarityCalculationStep)
                .build();
    }

    @Bean
    public Step bookSimilarityCalculationStep() {

        return new StepBuilder("bookSimilarityCalculationStep", jobRepository)
                .<BookPairDto, BookSimilarity>chunk(100, dataTransactionManager)
                .reader(bookSimilarityReader)
                .processor(bookSimilarityProcessor)
                .writer(bookSimilarityWriter)

                // 멀티스레딩 설정 추가
                .taskExecutor(bookSimilarityTaskExecutor())

                .faultTolerant()
                .retryLimit(3)
                .retry(DataAccessException.class)
                .retry(QueryTimeoutException.class)
                .retry(SQLException.class)
                .retry(OptimisticLockingFailureException.class)

                .skipLimit(10)
                .skip(IllegalArgumentException.class)
                .skip(NullPointerException.class)
                .skip(NumberFormatException.class)

                .listener(new BookSimilarityStepListener())
                .build();

    }

    /**
     * 책 유사도 계산 전용 TaskExecutor
     * CPU 집약적 작업에 최적화된 설정
     */
    @Bean
    public TaskExecutor bookSimilarityTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 스레드 수를 정확히 4개로 제한
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(4);

        // 대기열 크기 (chunk 크기의 2-3배)
        executor.setQueueCapacity(150);

        // 스레드 이름 prefix (디버깅/모니터링 용이)
        executor.setThreadNamePrefix("book-similarity-");

        // 스레드 유지 시간 (idle 스레드 정리)
        executor.setKeepAliveSeconds(60);

        // 애플리케이션 종료 시 대기
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);

        // 거부된 작업 처리 정책 (CallerRunsPolicy: 호출 스레드에서 실행)
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        executor.initialize();

        log.info("BookSimilarity TaskExecutor 초기화 완료 - Core Pool: 4, Max Pool: 4");

        return executor;
    }

}
