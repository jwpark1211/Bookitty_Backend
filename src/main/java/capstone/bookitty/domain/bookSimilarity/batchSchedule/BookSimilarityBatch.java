package capstone.bookitty.domain.bookSimilarity.batchSchedule;

import capstone.bookitty.domain.book.dto.BookPair;
import capstone.bookitty.domain.book.dto.RatingPair;
import capstone.bookitty.domain.bookSimilarity.domain.BookSimilarity;
import capstone.bookitty.domain.bookSimilarity.repository.BookSimilarityRepository;
import capstone.bookitty.domain.star.repository.StarRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.*;

@Configuration
@RequiredArgsConstructor
public class BookSimilarityBatch {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final StarRepository starRepository;
    private final BookSimilarityRepository bookSimilarityRepository;

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
            // 두 도서를 동시에 평가한 평점 데이터를 조회 (각 행: [score1, score2])
            List<RatingPair> commonRatings = starRepository.findCommonRatings(bookPair.isbn1(), bookPair.isbn2());

            if (commonRatings.isEmpty()) return null;

            double dotProduct = 0.0;
            double normA = 0.0;
            double normB = 0.0;
            for (RatingPair row : commonRatings) {
                dotProduct += row.score1() * row.score2();
                normA += row.score1() * row.score1();
                normB += row.score2() * row.score2();
            }
            double cosineSimilarity = (normA != 0 && normB != 0)
                    ? dotProduct / (Math.sqrt(normA) * Math.sqrt(normB)) : 0.0;

            return BookSimilarity.builder()
                    .isbn1(bookPair.isbn1())
                    .isbn2(bookPair.isbn2())
                    .similarity(cosineSimilarity)
                    .build();
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
                }
            }
            if (!filteredItems.isEmpty()) {
                bookSimilarityRepository.saveAll(filteredItems);
            }
        };
    }
}