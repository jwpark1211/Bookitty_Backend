package capstone.bookitty.domain.bookSimilarity.similarityBatch.config;

import capstone.bookitty.domain.bookSimilarity.domain.BookSimilarity;
import capstone.bookitty.domain.bookSimilarity.similarityBatch.item.BookSimilarityProcessor;
import capstone.bookitty.domain.bookSimilarity.similarityBatch.item.BookSimilarityReader;
import capstone.bookitty.domain.bookSimilarity.similarityBatch.item.BookSimilarityWriter;
import capstone.bookitty.domain.bookSimilarity.similarityBatch.item.dto.BookPairDto;
import capstone.bookitty.domain.bookSimilarity.similarityBatch.item.listener.BookSimilarityJobListener;
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
import org.springframework.dao.DataAccessException;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.transaction.PlatformTransactionManager;

import java.sql.SQLException;

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
                .listener(BookSimilarityJobListener.class)
                .start(bookSimilarityCalculationStep)
                .build();
    }

    @Bean
    public Step bookSimilarityCalculationStep() {

        return new StepBuilder("bookSimilarityCalculationStep", jobRepository)
                .<BookPairDto, BookSimilarity>chunk(1000, dataTransactionManager)
                .reader(bookSimilarityReader)
                .processor(bookSimilarityProcessor)
                .writer(bookSimilarityWriter)
                .faultTolerant()

                .retryLimit(3)
                .retry(DataAccessException.class)
                .retry(QueryTimeoutException.class)
                .retry(SQLException.class)

                .skipLimit(10)
                .skip(IllegalArgumentException.class)
                .skip(NullPointerException.class)
                .skip(NumberFormatException.class)

                .listener(new BookSimilarityStepListener())
                .build();

    }

}
