package capstone.bookitty.domain.bookSimilarity.similarityBatch.item.listener;

import capstone.bookitty.domain.bookSimilarity.similarityBatch.item.BookSimilarityProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class BookSimilarityJobListener implements JobExecutionListener {

    private final BookSimilarityProcessor processor;

    @Override
    public void beforeJob(JobExecution jobExecution) {
        processor.clearCache();
    }
}

