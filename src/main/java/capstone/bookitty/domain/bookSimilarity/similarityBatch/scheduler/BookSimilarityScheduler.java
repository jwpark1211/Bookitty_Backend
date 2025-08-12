package capstone.bookitty.domain.bookSimilarity.similarityBatch.scheduler;

import capstone.bookitty.domain.bookSimilarity.application.BatchStateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookSimilarityScheduler {

    private final JobLauncher jobLauncher;
    private final Job bookSimilarityCalculationJob;
    private final ApplicationEventPublisher eventPublisher;
    
    private final AtomicBoolean batchExecuting = new AtomicBoolean(false);

    //@Scheduled(cron = "0 0 4 * * ?") // 매일 새벽 4시 실행
    //@Scheduled(cron = "0 * * * * ?") // 매 분 0초에 실행
    public void runBookSimilarityCalculation() {
        
        if (!acquireBatchLock()) {
            log.warn("배치 작업이 이미 실행 중입니다. 중복 실행을 방지합니다.");
            return;
        }

        try {
            log.info("스케줄러에 의한 책 간 유사도 계산 배치 실행 시작");

            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("timestamp", System.currentTimeMillis())
                    .addString("trigger", "scheduler")
                    .toJobParameters();

            jobLauncher.run(bookSimilarityCalculationJob, jobParameters);
            log.info("책 간 유사도 계산 배치 실행 성공");

        } catch (Exception e) {
            log.error("스케줄러에 의한 배치 실행 중 오류 발생", e);
            //TODO : Slack notification 등 알림 처리 (현재 프로젝트에선 X)
        } finally {
            releaseBatchLock();
        }

    }
    
    public boolean acquireBatchLock() {
        if (batchExecuting.compareAndSet(false, true)) {
            eventPublisher.publishEvent(new BatchStateService.BatchStartEvent(this));
            log.info("배치 시작 이벤트 발행");
            return true;
        }
        return false;
    }
    
    public void releaseBatchLock() {
        batchExecuting.set(false);
        eventPublisher.publishEvent(new BatchStateService.BatchCompletionEvent(this));
        log.info("배치 완료 이벤트 발행");
    }
    
    public boolean isBatchRunning() {
        return batchExecuting.get();
    }
}