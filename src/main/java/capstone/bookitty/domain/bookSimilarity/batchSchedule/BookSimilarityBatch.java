package capstone.bookitty.domain.bookSimilarity.batchSchedule;

import capstone.bookitty.domain.book.api.dto.BookPair;
import capstone.bookitty.domain.book.api.dto.RatingPair;
import capstone.bookitty.domain.bookSimilarity.domain.BookSimilarity;
import capstone.bookitty.domain.bookSimilarity.repository.BookSimilarityRepository;
import capstone.bookitty.domain.star.repository.StarRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.skip.SkipPolicy;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.retry.RetryPolicy;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.*;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class BookSimilarityBatch {

    private final JobRepository jobRepository;
    @Qualifier("dataTransactionManager")
    private final PlatformTransactionManager transactionManager;
    private final StarRepository starRepository;
    private final BookSimilarityRepository bookSimilarityRepository;

    private final RetryPolicy customRetryPolicy;
    private final SkipPolicy customSkipPolicy;

    @Bean
    public Job bookSimilarityJob() {
        return new JobBuilder("bookSimilarityJob", jobRepository)
                .start(calculateBookSimilarityStep())
                .build();
    }

    @Bean
    public Step calculateBookSimilarityStep() {
        return new StepBuilder("calculateBookSimilarityStep", jobRepository)
                .<BookPair, BookSimilarity>chunk(50, transactionManager)
                .reader(bookPairReader())
                .processor(bookSimilarityProcessor())
                .writer(bookSimilarityWriter())
                .faultTolerant()
                .retryPolicy(customRetryPolicy)
                .skipPolicy(customSkipPolicy)
                .backOffPolicy(new FixedBackOffPolicy() {{
                    setBackOffPeriod(2000);
                }})
                .build();
    }

    /**
     * Reader: StarRepository의 findDistinctBookPairs() 메서드를 RepositoryItemReader를 통해 읽어옵니다.
     */
    @Bean
    public RepositoryItemReader<BookPair> bookPairReader() {
        Map<String, Sort.Direction> sorts = new HashMap<>();
        sorts.put("isbn", Sort.Direction.ASC);

        return new RepositoryItemReaderBuilder<BookPair>()
                .name("bookPairReader")
                .pageSize(100)
                .repository(starRepository)
                .methodName("findDistinctBookPairs")
                .sorts(sorts)
                .arguments(Collections.emptyList())
                .build();
    }

    /**
     * Processor: 각 ISBN 쌍에 대해, 두 도서를 동시에 평가한 사용자의 평점 데이터를 조회하여 코사인 유사도를 계산합니다.
     * 공통 평가 데이터가 없으면 null을 반환하여 해당 아이템을 필터링합니다.
     */
    @Bean
    public ItemProcessor<BookPair, BookSimilarity> bookSimilarityProcessor() {
        return bookPair -> {
            try {
                if (bookPair.isbn1() == null || bookPair.isbn2() == null)
                    throw new IllegalArgumentException("ISBN is null");
                List<RatingPair> commonRatings = starRepository.findCommonRatings(bookPair.isbn1(), bookPair.isbn2());
                if (commonRatings.isEmpty()) return null;

                double dotProduct = 0.0;
                double normA = 0.0;
                double normB = 0.0;

                for (RatingPair row : commonRatings) {
                    if (row.score1() == null || row.score2() == null)
                        throw new IllegalStateException("Rating is null");

                    dotProduct += row.score1() * row.score2();
                    normA += row.score1() * row.score1();
                    normB += row.score2() * row.score2();
                }

                double cosineSimilarity = (normA != 0 && normB != 0) ? dotProduct / (Math.sqrt(normA) * Math.sqrt(normB)) : 0.0;
                double adjustedSimilarity = Math.pow(cosineSimilarity, 4);

                return BookSimilarity.builder()
                        .isbn1(bookPair.isbn1())
                        .isbn2(bookPair.isbn2())
                        .similarity(adjustedSimilarity)
                        .build();

            } catch (IllegalArgumentException e) {
                log.warn("Invalid BookPair encountered: {}", bookPair, e);
                throw e;
            } catch (Exception e) {
                log.error("Processor failed temporarily for pair: {}", bookPair, e);
                throw new IllegalStateException("Processor failed temporarily", e);
            }
        };
    }


    /**
     * Writer: 계산된 BookSimilarity 객체들을 for문을 사용해 null 체크 후 DB에 저장합니다.
     */
    @Bean
    public ItemWriter<BookSimilarity> bookSimilarityWriter() {
        return items -> {
            List<BookSimilarity> filteredItems = new ArrayList<>();
            for (BookSimilarity similarity : items) {
                if (similarity != null) {
                    filteredItems.add(similarity);
                } else {
                    log.debug("Filtered out null similarity entry");
                }
            }

            log.info("Writing {} BookSimilarity records to DB", filteredItems.size());
            if (!filteredItems.isEmpty()) {
                bookSimilarityRepository.saveAll(filteredItems);
            }
        };
    }
}