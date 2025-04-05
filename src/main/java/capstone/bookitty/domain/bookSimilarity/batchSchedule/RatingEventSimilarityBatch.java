package capstone.bookitty.domain.bookSimilarity.batchSchedule;

import capstone.bookitty.domain.book.dto.RatingPair;
import capstone.bookitty.domain.bookSimilarity.domain.BookSimilarity;
import capstone.bookitty.domain.bookSimilarity.domain.RatingEvent;
import capstone.bookitty.domain.bookSimilarity.repository.BookSimilarityRepository;
import capstone.bookitty.domain.bookSimilarity.repository.RatingEventRepository;
import capstone.bookitty.domain.star.repository.StarRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.*;
import java.util.stream.Collectors;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class RatingEventSimilarityBatch {

    private final JobRepository jobRepository;
    @Qualifier("dataTransactionManager")
    private final PlatformTransactionManager transactionManager;
    private final StarRepository starRepository;
    private final BookSimilarityRepository bookSimilarityRepository;
    private final RatingEventRepository ratingEventRepository;

    @Bean
    public Job ratingEventSimilarityJob() {
        return new JobBuilder("ratingEventSimilarityJob", jobRepository)
                .start(eventSimilarityStep())
                .build();
    }

    @Bean
    public Step eventSimilarityStep() {
        return new StepBuilder("eventSimilarityStep", jobRepository)
                .<RatingEvent, List<BookSimilarity>>chunk(50, transactionManager)
                .reader(ratingEventReader())
                .processor(ratingEventProcessor())
                .writer(ratingEventWriter())
                .build();
    }

    @Bean
    public RepositoryItemReader<RatingEvent> ratingEventReader() {
        Map<String, Sort.Direction> sorts = new HashMap<>();
        sorts.put("createdAt", Sort.Direction.ASC);

        return new RepositoryItemReaderBuilder<RatingEvent>()
                .name("ratingEventReader")
                .repository(ratingEventRepository)
                .methodName("findByStatus")
                .arguments(List.of(RatingEvent.Status.PENDING))
                .pageSize(50)
                .sorts(sorts)
                .build();
    }

    @Bean
    public ItemProcessor<RatingEvent, List<BookSimilarity>> ratingEventProcessor() {
        return event -> {
            String baseIsbn = event.getIsbn();
            log.info("[Processor] baseIsbn: {} 처리 시작", baseIsbn);

            List<String> others = starRepository.findIsbnsRatedWith(baseIsbn);
            log.info("[Processor] 관련 도서 수: {}", others.size());

            List<BookSimilarity> results = new ArrayList<>();

            for (String other : others) {
                List<RatingPair> pairs = starRepository.findCommonRatings(baseIsbn, other);
                if (pairs.isEmpty()) continue;

                double dot = 0, normA = 0, normB = 0;
                for (RatingPair p : pairs) {
                    dot += p.score1() * p.score2();
                    normA += p.score1() * p.score1();
                    normB += p.score2() * p.score2();
                }
                double cosine = (normA != 0 && normB != 0) ? dot / (Math.sqrt(normA) * Math.sqrt(normB)) : 0;

                results.add(new BookSimilarity(baseIsbn, other, cosine));
            }

            log.info("[Processor] {} 건 유사도 계산 완료", results.size());
            return results;
        };
    }

    @Bean
    public ItemWriter<List<BookSimilarity>> ratingEventWriter() {
        return items -> {
            List<BookSimilarity> flatList = new ArrayList<>();
            for (List<BookSimilarity> list : items) {
                for (BookSimilarity bs : list) {
                    if (bs != null) flatList.add(bs);
                }
            }

            log.info("[Writer] 저장 대상 유사도 건수: {}", flatList.size());
            bookSimilarityRepository.saveAll(flatList);

            Set<String> processedIsbns = flatList.stream()
                    .map(BookSimilarity::getIsbn1)
                    .collect(Collectors.toSet());

            for (String isbn : processedIsbns) {
                List<RatingEvent> events = ratingEventRepository.findByIsbnAndStatus(isbn, RatingEvent.Status.PENDING);
                for (RatingEvent event : events) {
                    event.eventDone();
                }
                log.info("[Writer] ISBN {} 관련 이벤트 {}건 DONE 처리", isbn, events.size());
            }
        };
    }
}

